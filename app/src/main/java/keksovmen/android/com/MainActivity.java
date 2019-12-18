package keksovmen.android.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import keksovmen.android.com.Views.ConversationView;
import keksovmen.android.com.Views.MessageView;
import keksovmen.android.com.Views.UsersView;

public class MainActivity extends AppCompatActivity implements SimpleComponent {

    private static final String MULTIPLE_PURPOSE_PANE_NAME = "View";
    private static final String CONVERSATION_TAB_NAME = "Conference";

    private BaseApplication application;

    private TabHost tabHost;

    private UsersView usersView;
    private Map<String, LogicObserver> messageViewSet;
    private Map<String, TabHost.TabSpec> openTabs;
    private ConversationView conversationView;
    private CustomCallDialog callDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        application = (BaseApplication) getApplication();

        usersView = new UsersView(this, this, this::openMessageTabAction);
        messageViewSet = new HashMap<>();
        openTabs = new LinkedHashMap<>();
        conversationView = new ConversationView(this, this, () -> closeTabAction(CONVERSATION_TAB_NAME));
        callDialog = new CustomCallDialog(this);


        tabHost = findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setIndicator(MULTIPLE_PURPOSE_PANE_NAME);
        spec.setContent(tag -> usersView.getLayout());

        tabHost.setCurrentTab(0);
        tabHost.addTab(spec);


        openTabs.put(MULTIPLE_PURPOSE_PANE_NAME, spec);

        application.getLiveData().observe(this, this::update);

        InputMethodManager systemService = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        tabHost.setOnTabChangedListener(tabId -> {
            if (tabId.equals(MULTIPLE_PURPOSE_PANE_NAME))
                systemService.hideSoftInputFromWindow(tabHost.getCurrentTabView().getWindowToken(), 0);
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        application.setState(this);
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        application.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case INCOMING_MESSAGE:
                if ((Integer) objects[2] == 0)
                    openMessagePane((BaseUser) objects[0]);
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
            case DISCONNECTED:
                finish();
                break;

        }

        callDialog.observe(actions, objects);
        conversationView.observe(actions, objects);
        if (messageViewSet.size() != 0)
            messageViewSet.forEach((string, logicObserver) -> logicObserver.observe(actions, objects));
    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        usersView.update(unEditableModel);
        conversationView.update(unEditableModel);
    }

    private void openMessagePane(BaseUser user) {
        TabHost.TabSpec index = openTabs.get(user.toString());
        if (index == null) {
            //Create new pane
            //And you should register it as observer
            MessageView messageView = new MessageView(this, this, createCloseAction(user), user);
            addMessagePane(user.toString(), messageView);
        }
        //put some type of indicator by color or whatever
//        tabHost.getTabWidget().getChildTabViewAt().startAnimation(null);
//        tabHost.getTabWidget().getChildTabViewAt(tabHost.getTabWidget().getChildTabViewAt().)
    }

    private void openMessageTabAction(BaseUser forWho) {
        String dudeAsString = forWho.toString();
        TabHost.TabSpec index = openTabs.get(dudeAsString);
        if (index == null) {
            MessageView messageView = new MessageView(this, this, createCloseAction(forWho), forWho);
            tabHost.setCurrentTabByTag(addMessagePane(dudeAsString, messageView).getTag());
//            messageView.onShow();
        } else {
            tabHost.setCurrentTabByTag(index.getTag());
        }
    }


    private TabHost.TabSpec addMessagePane(String pageName, MessageView messageView) {
        messageViewSet.put(pageName, messageView);
        return addTab(pageName, messageView.getLayout());
    }

    private TabHost.TabSpec addTab(String tabName, View view) {
        if (openTabs.containsKey(tabName))
            return openTabs.get(tabName);
        TabHost.TabSpec spec = tabHost.newTabSpec(tabName);
        spec.setIndicator(tabName);
        spec.setContent(tag -> view);

        openTabs.put(tabName, spec);

        tabHost.addTab(spec);
        return spec;
    }

    private Runnable createCloseAction(BaseUser user) {
        return () -> {
            messageViewSet.remove(user.toString());
            closeTabAction(user.toString());
        };
    }

    private void closeTabAction(String paneName) {
        TabHost.TabSpec remove = openTabs.remove(paneName);
        if (remove != null) {
            tabHost.clearAllTabs();
            openTabs.values().forEach(tabSpec -> tabHost.addTab(tabSpec));
        }
    }

    private void handleCall(Object[] data) {
        BaseUser who = (BaseUser) data[0];
        String dudes = (String) data[1];
        callDialog.showIncomingDialog(who, dudes, getSupportFragmentManager());
    }

    private void onCallAccept() {
        tabHost.setCurrentTabByTag(addTab(CONVERSATION_TAB_NAME, conversationView.getLayout()).getTag());
    }

}
