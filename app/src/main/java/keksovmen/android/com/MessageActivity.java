package keksovmen.android.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;

public class MessageActivity extends AppCompatActivity implements LogicObserver, StateHolder {

    private BaseApplication application;
    private EditText textPane;
    private TextView label;
    private LinearLayout messagePane;
    private TextView editText;

    private BaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        application = (BaseApplication) getApplication();
        textPane = findViewById(R.id.text_producer);
        label = findViewById(R.id.dudes_name);
        messagePane = findViewById(R.id.message_pane);
        editText = findViewById(R.id.text_place);


        Intent intent = getIntent();
        user = BaseUser.parse(intent.getStringExtra(BUTTONS.SEND_MESSAGE.name()));
        String stringExtra = intent.getStringExtra(MultiplePurposeActivity.FIRST_MESSAGE);
        if (stringExtra != null) {
            displayMessage(stringExtra, false);
        }
        label.setText(user.toString());


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
    protected void onDestroy() {
        super.onDestroy();
//        if (isFinishing()){
//            application.setState(null);
//        }
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case INCOMING_MESSAGE:
                onIncomingMessage(objects);
                break;
            case DISCONNECTED:
                onDisconnect();
                break;
        }
    }

    public void sendMessage(View view) {
        String message = textPane.getText().toString();
        textPane.getText().clear();
        application.handleRequest(BUTTONS.SEND_MESSAGE, new Object[]{message, user.getId()});
//        TextView textView = new TextView(this);
        displayMessage(message, true);

//        textView.setText("Me (" + FormatWorker.getTime() + " ): " + message);
//        messagePane.addView(textView);
    }

    public void close(View view) {
        finish();
    }

    private void onIncomingMessage(Object[] data) {
        //check data[2] for conversation
        displayMessage((String) data[1], false);
    }

    private void onDisconnect() {
//        application.setLastSignificantAction(ACTIONS.DISCONNECTED);
        finish();
    }

    private void displayMessage(String message, boolean isMe) {
        String who = isMe ? "Me" : user.getName();
        editText.setText(editText.getText().toString() + who + " (" + FormatWorker.getTime() + " ): " + message + "\n");
    }
}
