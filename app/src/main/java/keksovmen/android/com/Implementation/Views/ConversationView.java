package keksovmen.android.com.Implementation.Views;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.ModelObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import keksovmen.android.com.Implementation.Views.SmallParts.ControlPane;
import keksovmen.android.com.Implementation.Views.SmallParts.SettingsEntry;

public class ConversationView extends BaseMessagingView implements ModelObserver {

    private final ControlPane controlPane;
    private final LinearLayout audioSettingPane;
    private final Map<User, SettingsEntry> settingsEntryMap;


    public ConversationView(Context context, ButtonsHandler helpHandler, Runnable closeAction) {
        super(context, helpHandler, closeAction);

        controlPane = new ControlPane(context, helpHandler);
        audioSettingPane = new LinearLayout(context);

        settingsEntryMap = new HashMap<>();

        audioSettingPane.setId(View.generateViewId());

        audioSettingPane.setOrientation(LinearLayout.VERTICAL);

        closeButton.setText("Exit conference");

        build(context);

    }

    @Override
    public void modelObservation(UnEditableModel unEditableModel) {
        Set<User> conversation = unEditableModel.getConversation();

        Map<User, SettingsEntry> tmp = new HashMap<>();

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
        switch (actions) {
            case EXITED_CONVERSATION: {
                clearAllData();
                break;
            }
        }
        controlPane.observe(actions, objects);
    }

    @Override
    protected ConstraintLayout.LayoutParams createMessageParam() {
        ConstraintLayout.LayoutParams messageParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        messageParam.topToBottom = controlPane.getMainLayout().getId();
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
        messageScroll.setScrollbarFadingEnabled(false);
        messageScroll.setOnTouchListener(new TouchListener(context, messageScroll));

        linearLayout.addView(settingScroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.5f));
        linearLayout.addView(messageScroll, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        return linearLayout;
    }

    @Override
    protected boolean shouldDisplay(User from, int toConversation) {
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

    @Override
    protected void build(Context context) {
        super.build(context);
        layout.addView(controlPane.getMainLayout(), createControlParams());
    }

    protected ConstraintLayout.LayoutParams createControlParams() {
        ConstraintLayout.LayoutParams controlParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        controlParams.topToTop = layout.getId();
        controlParams.startToStart = layout.getId();
        controlParams.endToEnd = layout.getId();
        return controlParams;
    }

    private void addUserSetting(User user) {
        SettingsEntry entry = new SettingsEntry(user, context, this);
        settingsEntryMap.put(user, entry);
        audioSettingPane.addView(entry.getPane());
    }

    private void removeUserSetting(User user) {
        SettingsEntry remove = settingsEntryMap.remove(user);
        if (remove != null)
            audioSettingPane.removeView(remove.getPane());
    }

    private void clearAllData() {
        messageDisplay.setText("");
    }

}
