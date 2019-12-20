package keksovmen.android.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;

import java.util.Map;

import keksovmen.android.com.Util.PageHolder;
import keksovmen.android.com.Util.ProxyMapForPageHolder;
import keksovmen.android.com.Views.ConversationView;
import keksovmen.android.com.Views.MessageView;
import keksovmen.android.com.Views.UsersView;

public class MainActivity extends AppCompatActivity implements SimpleComponent {

    private static final String MULTIPLE_PURPOSE_PANE_NAME = "View";
    private static final String CONVERSATION_TAB_NAME = "Conference";

    private BaseApplication application;

    private TabHost tabHost;

    private UsersView usersView;
    private Map<String, PageHolder> openTabs;
    private ConversationView conversationView;
    private CustomCallDialog callDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (BaseApplication) getApplication();

        usersView = new UsersView(this, this, this::openMessageTabAction);
        openTabs = new ProxyMapForPageHolder();
        conversationView = new ConversationView(this, this, () -> closeTabAction(CONVERSATION_TAB_NAME));
        callDialog = new CustomCallDialog(this);


        tabHost = findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setIndicator(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setContent(tag -> usersView.getLayout());

        tabHost.addTab(spec);



        openTabs.put(MULTIPLE_PURPOSE_PANE_NAME, new PageHolder(spec, MULTIPLE_PURPOSE_PANE_NAME, usersView));

        application.getLiveData().observe(this, this::update);

        InputMethodManager systemService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        tabHost.setOnTabChangedListener(tabId -> {
            if (tabId.equals(MULTIPLE_PURPOSE_PANE_NAME))
                systemService.hideSoftInputFromWindow(tabHost.getCurrentTabView().getWindowToken(), 0);
            PageHolder holder = openTabs.get(tabId);
            if (holder != null) {
                tabHost.getTabWidget().getChildTabViewAt(holder.getIndexOnPane()).clearAnimation();
                holder.setDisplayUnreadMessage(false);
            }
        });


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
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        application.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case INCOMING_MESSAGE:
                if ((Integer) objects[2] == 0) {
                    openMessagePane((BaseUser) objects[0]);
                } else {
                    animateAnReadMessages(openTabs.get(CONVERSATION_TAB_NAME));
                }
                break;
            case INCOMING_CALL:
                handleCall(objects);
                break;
            case OUT_CALL:
                callDialog.showOutcomingDialog((BaseUser) objects[0], getSupportFragmentManager());
                break;
            case CALL_ACCEPTED:
                onCallAccept();
                break;
            case CALL_DENIED:
                BaseApplication.showDialog(this, "call was denied by - " + objects[0]);
                break;
            case CALL_CANCELLED:
                BaseApplication.showDialog(this, "call was cancelled by - " + objects[0]);
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
                openMessagePane((BaseUser) objects[0]);
                break;

        }

        callDialog.observe(actions, objects);
        openTabs.forEach((s, pageHolder) -> pageHolder.observe(actions, objects));
    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        usersView.update(unEditableModel);
        conversationView.update(unEditableModel);
    }

    private void onConnectionFailure() {
        Intent result = new Intent();
        result.putExtra(EntranceActivity.STATUS_CODE, EntranceActivity.ResultCodes.STATUS_NETWORK_FAILURE.toString());
        setResult(RESULT_OK, result);
        finish();
    }


    private void openMessagePane(BaseUser user) {
        PageHolder holder = openTabs.get(user.toString());
        if (holder == null) {
            //Create new pane
            //And you should register it as observer
            MessageView messageView = new MessageView(this, this, createCloseAction(user), user);
            holder = addMessagePane(user.toString(), messageView);
            //put some type of indicator by color or whatever
        }
        if (tabHost.getCurrentTab() != holder.getIndexOnPane())
            animateAnReadMessages(holder);
    }

    private void openMessageTabAction(BaseUser forWho) {
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

    private Runnable createCloseAction(BaseUser user) {
        return () -> closeTabAction(user.toString());
    }

    private void closeTabAction(String paneName) {
        PageHolder remove = openTabs.remove(paneName);
        if (remove != null) {
            for (int i = 0; i < tabHost.getTabWidget().getTabCount(); i++) { // if you wound't do this then animations on this tabs will fuck up so much that you will hate your existence
                tabHost.getTabWidget().getChildTabViewAt(i).clearAnimation();
            }
            tabHost.clearAllTabs();
            openTabs.values().forEach(this::fillTabHostAfterClearing);
        }
    }

    private void handleCall(Object[] data) {
        BaseUser who = (BaseUser) data[0];
        String dudes = (String) data[1];
        callDialog.showIncomingDialog(who, dudes, getSupportFragmentManager());
    }

    private void onCallAccept() {
        tabHost.setCurrentTab(addTab(CONVERSATION_TAB_NAME, conversationView.getLayout(), conversationView).getIndexOnPane());
    }

    private void animateAnReadMessages(PageHolder holder) {
        if (holder.getIndexOnPane() == -1)
            throw new IllegalArgumentException("-1 indicate that some one didn't set index field on PageHolder");
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade);
        tabHost.getTabWidget().getChildTabViewAt(holder.getIndexOnPane()).startAnimation(animation);
        holder.setDisplayUnreadMessage(true);
    }

    private void fillTabHostAfterClearing(PageHolder holder) {
        tabHost.addTab(holder.getContent());
        if (holder.isDisplayUnreadMessage() && tabHost.getCurrentTab() != holder.getIndexOnPane()) {
            animateAnReadMessages(holder);
        }
    }

}
