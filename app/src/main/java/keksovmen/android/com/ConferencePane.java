package keksovmen.android.com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Model.Updater;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConferencePane extends AppCompatActivity implements StateHolder, Updater, LogicObserver {

    private BaseApplication application;

    private Switch muteSwitch;
    private LinearLayout audioSettingsPane;
    private TextView messagePane;
    private EditText textInput;
    private Button leaveButton;

    private Map<BaseUser, SettingsEntry> settingsEntryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_pane);
        application = (BaseApplication) getApplication();
        muteSwitch = findViewById(R.id.mute_switch);
        audioSettingsPane = findViewById(R.id.audio_settings_pane);
        messagePane = findViewById(R.id.conference_text_pane);
        textInput = findViewById(R.id.conversation_message_input);
        leaveButton = findViewById(R.id.leave_conversation);

        settingsEntryMap = new HashMap<>();

        application.getLiveData().observe(this, this::update);

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
            onLeaveButtonClick();
        }
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case EXITED_CONVERSATION: {
                finish();
                return;
            }
            case DISCONNECTED: {
//                application.setLastSignificantAction(ACTIONS.DISCONNECTED);
                finish();
                return;
            }
            case INCOMING_MESSAGE:{
                if ((int) objects[2] == 1){
                    showMessage((BaseUser) objects[0], (String) objects[1]);
                }else {
                    //default open message activity
                }
            }
        }
    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        Set<BaseUser> conversation = unEditableModel.getConversation();

        Map<BaseUser, SettingsEntry> tmp = new HashMap<>();

        settingsEntryMap.forEach((user, userSettings) -> {
            if (!conversation.contains(user))
                tmp.put(user, userSettings);
        });

        tmp.keySet().forEach(this::removeUserSetting);
        tmp.clear();

        conversation.forEach(user -> {
            if (!settingsEntryMap.containsKey(user))
                addUserSetting(user);
        });
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

    private void addUserSetting(BaseUser user) {
        SettingsEntry entry = new SettingsEntry(user, this, application);
        settingsEntryMap.put(user, entry);
        audioSettingsPane.addView(entry.getPane());
    }

    private void removeUserSetting(BaseUser user) {
        SettingsEntry remove = settingsEntryMap.remove(user);
        if (remove != null)
            audioSettingsPane.removeView(remove.getPane());
    }

    private void onLeaveButtonClick() {
        application.handleRequest(BUTTONS.EXIT_CONFERENCE, null);
    }

    public void exitConf(View view) {
        onLeaveButtonClick();
        finish();
    }

    public void mute(View view) {
        application.handleRequest(BUTTONS.MUTE, null);
    }

    public void sendMessageConference(View view) {
        String message = textInput.getText().toString();
        textInput.getText().clear();
        application.handleRequest(BUTTONS.SEND_MESSAGE, new Object[]{message, WHO.CONFERENCE.getCode()});
        showMessage(null, message);

    }

    private void showMessage(BaseUser from, String message) {
        String who = (from == null) ? "Me" : from.toString();
        messagePane.setText(messagePane.getText().toString() + who + " (" + FormatWorker.getTime() + " ): " + message + "\n");
    }
}
