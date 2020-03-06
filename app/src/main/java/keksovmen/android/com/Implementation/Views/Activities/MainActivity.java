package keksovmen.android.com.Implementation.Views.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;

import java.util.Map;

import keksovmen.android.com.Implementation.BaseApplication;
import keksovmen.android.com.Implementation.Util.PageHolder;
import keksovmen.android.com.Implementation.Util.ProxyMapForPageHolder;
import keksovmen.android.com.Implementation.Views.ConversationView;
import keksovmen.android.com.Implementation.Views.CustomCallDialog;
import keksovmen.android.com.Implementation.Views.MessageView;
import keksovmen.android.com.Implementation.Views.UsersView;
import keksovmen.android.com.R;

public class MainActivity extends AppCompatActivity implements SimpleComponent {

    private static final String MULTIPLE_PURPOSE_PANE_NAME = "View";
    private static final String CONVERSATION_TAB_NAME = "Conference";
    private static final String NOTIFICATION_TAG = "INCOMING";
    private static final int NOTIFICATION_ID = 1;

    private BaseApplication application;

    private TabHost tabHost;

    private UsersView usersView;
    private ConversationView conversationView;
    private CustomCallDialog callDialog;

    private Map<String, PageHolder> openTabs;

    private Runnable lockedCall;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (BaseApplication) getApplication();

        usersView = new UsersView(this, this, this::openMessageTabCommand);
        conversationView = new ConversationView(this, this, () -> closeTabAction(CONVERSATION_TAB_NAME));
        callDialog = new CustomCallDialog(this);
        openTabs = new ProxyMapForPageHolder();


        tabHost = findViewById(R.id.tabHost);

        setupInitialStateForTabHost();

        application.getLiveData().observe(this, this::modelObservation);

        tabHost.setOnTabChangedListener(createTabChangeListener());

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    private void setupInitialStateForTabHost() {
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setIndicator(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setContent(tag -> usersView.getMainLayout());

        tabHost.addTab(spec);

        openTabs.put(MULTIPLE_PURPOSE_PANE_NAME, new PageHolder(spec, MULTIPLE_PURPOSE_PANE_NAME, usersView));
    }

    @Override
    protected void onStart() {
        super.onStart();
        application.setState(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            handleRequest(BUTTONS.DISCONNECT, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseApplication.setVisible();
        if (lockedCall != null) {
            lockedCall.run();
            clearLastCall();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BaseApplication.setInvisible();
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        application.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case INCOMING_MESSAGE:
                if ((Integer) objects[2] == 0) {
                    openMessagePane((User) objects[0]);
                } else {
                    animateAnReadMessages(openTabs.get(CONVERSATION_TAB_NAME));
                }
                break;
            case INCOMING_CALL:
                handleCall(objects);
                break;
            case OUT_CALL:
                callDialog.showOutcomingDialog((User) objects[0], getSupportFragmentManager());
                break;
            case CALL_ACCEPTED:
                onCallAccept();
                break;
            case CALL_DENIED:
                BaseApplication.showDialog(this, "call was denied by - " + objects[0]);
                break;
            case CALL_CANCELLED:
                BaseApplication.showDialog(this, "call was cancelled by - " + objects[0]);
                clearLastCall();
                break;
            case EXITED_CONVERSATION:
                closeTabAction(CONVERSATION_TAB_NAME);
                break;
            case CONNECTION_TO_SERVER_FAILED:
                onConnectionFailure();
                break;
            case DISCONNECTED:
                finish();
                break;
            case CALLED_BUT_BUSY:
                openMessagePane((User) objects[0]);
                break;

        }

        callDialog.observe(actions, objects);
        openTabs.forEach((s, pageHolder) -> pageHolder.observe(actions, objects));
    }

    @Override
    public void modelObservation(UnEditableModel unEditableModel) {
        usersView.modelObservation(unEditableModel);
        conversationView.modelObservation(unEditableModel);
    }

    private void onConnectionFailure() {
        Intent result = new Intent();
        result.putExtra(EntranceActivity.STATUS_CODE, EntranceActivity.ResultCodes.STATUS_NETWORK_FAILURE.toString());
        setResult(RESULT_OK, result);
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
        finish();
    }


    private void openMessagePane(User user) {
        PageHolder holder = openTabs.get(user.toString());
        if (holder == null) {
            MessageView messageView = new MessageView(this, this, createCloseAction(user), user);
            holder = addMessagePane(user.toString(), messageView);
        }
        if (tabHost.getCurrentTab() != holder.getIndexOnPane())
            animateAnReadMessages(holder);
    }

    private void openMessageTabCommand(User forWho) {
        String dudeAsString = forWho.toString();
        PageHolder holder = openTabs.get(dudeAsString);
        if (holder == null) {
            MessageView messageView = new MessageView(this, this, createCloseAction(forWho), forWho);
            tabHost.setCurrentTab(addMessagePane(dudeAsString, messageView).getIndexOnPane());
        } else {
            tabHost.setCurrentTab(holder.getIndexOnPane());
        }
    }


    private PageHolder addMessagePane(String pageName, MessageView messageView) {
        return addTab(pageName, messageView.getLayout(), messageView);
    }

    private PageHolder addTab(String tabName, View view, LogicObserver observer) {
        TabHost.TabSpec spec = tabHost.newTabSpec(tabName);
        spec.setIndicator(tabName);
        spec.setContent(tag -> view);

        PageHolder holder = new PageHolder(spec, tabName, observer);
        openTabs.put(tabName, holder);

        tabHost.addTab(spec);
        return holder;
    }

    private Runnable createCloseAction(User user) {
        return () -> closeTabAction(user.toString());
    }

    private void closeTabAction(String paneName) {
        PageHolder remove = openTabs.remove(paneName);
        if (remove != null) {
            clearTabHostBeforeFilling(tabHost);
            openTabs.values().forEach(this::fillTabHostAfterClearing);
        }
    }

    private void handleCall(Object[] data) {
        User who = (User) data[0];
        String dudes = (String) data[1];

        if (BaseApplication.isVisible()) {
            callDialog.showIncomingDialog(who, dudes, getSupportFragmentManager());
        } else {
            lockedCall = () -> callDialog.showIncomingDialog(who, dudes, getSupportFragmentManager());
            showNotification("Incoming call", "From " + who.toString());
        }
    }

    private void onCallAccept() {
        tabHost.setCurrentTab(addTab(CONVERSATION_TAB_NAME, conversationView.getLayout(), conversationView).getIndexOnPane());
    }

    private void animateAnReadMessages(PageHolder holder) {
        if (holder.getIndexOnPane() == -1)
            throw new IllegalArgumentException("-1 indicate that some one didn't set index field on PageHolder");
        if (tabHost.getCurrentTab() != holder.getIndexOnPane()) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
            tabHost.getTabWidget().getChildTabViewAt(holder.getIndexOnPane()).startAnimation(animation);
            holder.setDisplayUnreadMessage(true);
        }
    }

    private void fillTabHostAfterClearing(PageHolder holder) {
        tabHost.addTab(holder.getContent());
        if (holder.isDisplayUnreadMessage()) {
            animateAnReadMessages(holder);
        }
    }

    private TabHost.OnTabChangeListener createTabChangeListener() {
        InputMethodManager systemService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return tabId -> {
            if (tabId.equals(MULTIPLE_PURPOSE_PANE_NAME)) {
                systemService.hideSoftInputFromWindow(tabHost.getCurrentTabView().getWindowToken(), 0);
            }
            PageHolder holder = openTabs.get(tabId);
            if (holder != null) {
                tabHost.getTabWidget().getChildTabViewAt(holder.getIndexOnPane()).clearAnimation();
                holder.setDisplayUnreadMessage(false);
            }
        };
    }

    private void clearLastCall() {
        lockedCall = null;
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_TAG)
                .setSmallIcon(R.drawable.ricardo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(fullScreenPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_TAG,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(NOTIFICATION_TAG);
        }
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, builder.build());
    }

    private static void clearTabHostBeforeFilling(TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getTabCount(); i++) {
            // if you wound't do this then animations on this tabs will fuck up so much that you will hate your existence
            tabHost.getTabWidget().getChildTabViewAt(i).clearAnimation();
        }
        tabHost.clearAllTabs();
    }

}
