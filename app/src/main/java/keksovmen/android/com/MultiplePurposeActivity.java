package keksovmen.android.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Model.Updater;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

public class MultiplePurposeActivity extends AppCompatActivity implements Updater, LogicObserver, StateHolder {

    private static final String SEND_MESSAGE = "Send message";
    private static final String CALL = "call";
    public static final String FIRST_MESSAGE = "FIRST_MESSAGE";

    private BaseApplication application;
    private LinearLayout userList;
//    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("CREATING MSF");
        setContentView(R.layout.activity_multiple_purpose);

        application = (BaseApplication) getApplication();

        userList = findViewById(R.id.user_list);

//        handler = new Handler(Looper.getMainLooper(), this::handleLoop);

        application.getLiveData().observe(this, this::update);

    }

    @Override
    public LogicObserver getObserver() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public FragmentManager getManager() {
        return getSupportFragmentManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        application.setState(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            application.handleRequest(BUTTONS.DISCONNECT, null);
//            application.setState(null);
        }
    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        //put on ui thread
//        handler.obtainMessage(BaseApplication.UPDATE_ACTION, unEditableModel).sendToTarget();
        userList.removeAllViews();
        for (BaseUser user : unEditableModel.getUserMap().values()) {
            TextView textView = new TextView(this);
            textView.setText(user.toString());

            PopupMenu popupMenu = new PopupMenu(this, textView);
            Menu menu = popupMenu.getMenu();
            menu.add(SEND_MESSAGE);
            menu.add(CALL);
            popupMenu.setOnMenuItemClickListener(new PopupListener(user, textView));

            textView.setOnClickListener(v -> popupMenu.show());
            userList.addView(textView);
        }
    }

    @Override
    public void observe(ACTIONS actions, Object[] data) {
        switch (actions) {
            case INCOMING_MESSAGE:
                onSendMessage((BaseUser) data[0], (String) data[1]);
                break;
            case DISCONNECTED:
                onDisconnect();
                break;
//            case OUT_CALL:{
//                application.showCallDialog(false, (BaseUser) data[0], null);
//                break;
//            }
//            case INCOMING_CALL:{
//                application.showCallDialog(true, (BaseUser) data[0], (String) data[1]);
//                break;
//            }
//            case CALL_DENIED:{
//                application.showDenyDialog((BaseUser) data[0], this);
//                break;
//
//            }
//            case CALL_CANCELLED: {
//                application.showCancelDialog((BaseUser) data[0], this);
//                break;
//            }
        }
    }

    public void disconnect(View view) {
        finish();
    }

    public void refresh(View view) {
        application.handleRequest(BUTTONS.ASC_FOR_USERS, null);
    }

    private void onSendMessage(BaseUser user, String incomingMessage) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(BUTTONS.SEND_MESSAGE.name(), user.toString());
        if (incomingMessage != null)
            intent.putExtra(FIRST_MESSAGE, incomingMessage);
        startActivity(intent);
    }

    private void onCall(BaseUser user) {
        application.handleRequest(BUTTONS.CALL, new Object[]{user, null});
    }

    private void onDisconnect() {
        finish();
    }


    private class PopupListener implements PopupMenu.OnMenuItemClickListener {

        private final BaseUser user;
        private final View textView;


        public PopupListener(BaseUser user, View textView) {
            this.user = user;
            this.textView = textView;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
//            System.out.println(item.getTitle() + " - " + user);
            if (!textView.isShown())
                return true;
            String title = item.getTitle().toString();
            switch (title) {
                case SEND_MESSAGE: {
                    onSendMessage(user, null);
                    break;
                }
                case CALL: {
                    onCall(user);
                    break;
                }
            }
            return true;
        }
    }
}
