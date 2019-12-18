package keksovmen.android.com.Views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Model.Updater;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import keksovmen.android.com.SettingsEntry;

public class ConversationView extends BaseMessagingView implements Updater {

    private final Switch muteSwitch;
    private final LinearLayout audioSettingPane;
    private final Map<BaseUser, SettingsEntry> settingsEntryMap;


    public ConversationView(Context context, ButtonsHandler helpHandler, Runnable closeAction) {
        super(context, helpHandler, closeAction);
        muteSwitch = new Switch(context);
        audioSettingPane = new LinearLayout(context);

        settingsEntryMap = new HashMap<>();

        muteSwitch.setId(View.generateViewId());
        audioSettingPane.setId(View.generateViewId());
        audioSettingPane.setOrientation(LinearLayout.VERTICAL);

        ConstraintLayout.LayoutParams switchParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        switchParam.rightToRight = layout.getId();
        switchParam.topToTop = layout.getId();

        layout.addView(muteSwitch, switchParam);

        closeButton.setText("Exit conference");
        muteSwitch.setText("Mute");
        muteSwitch.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        muteSwitch.setOnClickListener(v -> handleRequest(BUTTONS.MUTE, null));

        build(context);

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
    public void observe(ACTIONS actions, Object[] objects) {
        super.observe(actions, objects);
        switch (actions){
            case EXITED_CONVERSATION:{
                clearAllData();
                break;
            }
        }
    }

    @Override
    protected ConstraintLayout.LayoutParams createMessageParam() {
        ConstraintLayout.LayoutParams messageParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        messageParam.topToBottom = muteSwitch.getId();
        messageParam.bottomToTop = textInput.getId();
        return messageParam;
    }

    @Override
    protected View createMessagePane(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ScrollView settingScroll = new ScrollView(context);
        settingScroll.addView(audioSettingPane);

        ScrollView messageScroll = new ScrollView(context);
        messageScroll.addView(messageDisplay);


        linearLayout.addView(settingScroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        linearLayout.addView(messageScroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        return linearLayout;
    }

    @Override
    protected boolean shouldDisplay(BaseUser from, int toConversation) {
        return toConversation == 1;
    }

    @Override
    protected int getReceiverId() {
        return WHO.CONFERENCE.getCode();
    }

    @Override
    protected void onClose() {
        handleRequest(BUTTONS.EXIT_CONFERENCE, null);
    }

    private void addUserSetting(BaseUser user) {
        SettingsEntry entry = new SettingsEntry(user, context, this);
        settingsEntryMap.put(user, entry);
        audioSettingPane.addView(entry.getPane());
    }

    private void removeUserSetting(BaseUser user) {
        SettingsEntry remove = settingsEntryMap.remove(user);
        if (remove != null)
            audioSettingPane.removeView(remove.getPane());
    }

    private void clearAllData(){
        muteSwitch.setChecked(false);
        messageDisplay.setText("");
    }

}
