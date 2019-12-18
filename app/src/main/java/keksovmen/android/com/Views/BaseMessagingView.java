package keksovmen.android.com.Views;

import android.content.Context;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;

public abstract class BaseMessagingView implements LogicObserver, ButtonsHandler {

    protected final ConstraintLayout layout;
    protected final TextView messageDisplay;
    protected final EditText textInput;
    protected final Button closeButton;

    protected final Context context;

    protected final ButtonsHandler helpHandlerPredecessor;
    protected final Runnable closeAction;

    public BaseMessagingView(Context context, ButtonsHandler helpHandlerPredecessor, Runnable closeAction) {
        layout = new ConstraintLayout(context);
        messageDisplay = new TextView(context);
        textInput = new EditText(context);
        closeButton = new Button(context);
        this.context = context;

        this.helpHandlerPredecessor = helpHandlerPredecessor;
        this.closeAction = closeAction;

        layout.setId(View.generateViewId());
        messageDisplay.setId(View.generateViewId());
        textInput.setId(View.generateViewId());
        closeButton.setId(View.generateViewId());


//        textInput.setOnKeyListener((v, keyCode, event) -> {
//            if (keyCode == KeyEvent.KEYCODE_ENTER)
//                sendMessage();
//            return true;
//        });
        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setOnClickListener(v -> sendMessage());
        closeButton.setOnClickListener(v -> onCloseButtonClick());
    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        helpHandlerPredecessor.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case INCOMING_MESSAGE:
                displayMessage((BaseUser) objects[0], (String) objects[1], (int) objects[2]);
                break;

        }
    }

    protected void build(Context context) {
        layout.addView(createMessagePane(context), createMessageParam());
        layout.addView(textInput, createInputParam());
        layout.addView(closeButton, createCloseParam());
    }

    protected ConstraintLayout.LayoutParams createMessageParam() {
        ConstraintLayout.LayoutParams messageParam = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        messageParam.topToTop = layout.getId();
        messageParam.bottomToTop = textInput.getId();
        return messageParam;
    }

    protected ConstraintLayout.LayoutParams createInputParam() {
        ConstraintLayout.LayoutParams inputParam = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        inputParam.bottomToTop = closeButton.getId();
        return inputParam;
    }

    protected ConstraintLayout.LayoutParams createCloseParam() {
        ConstraintLayout.LayoutParams closeParam = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        closeParam.bottomToBottom = layout.getId();
        return closeParam;
    }

    protected View createMessagePane(Context context) {
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(messageDisplay);
        return scrollView;
    }

    protected abstract boolean shouldDisplay(BaseUser from, int toConversation);

    private void showMessage(String from, String message) {
        messageDisplay.setText(messageDisplay.getText() +
                from + " (" + FormatWorker.getTime() + ") - " + message + "\n");
    }

    private void displayMyMessage(String message){
        messageDisplay.setText(messageDisplay.getText() +
                "Me" + " (" + FormatWorker.getTime() + ") - " + message + "\n");
    }

    protected abstract int getReceiverId();

    private void sendMessage(){
        String message = textInput.getText().toString();
        if (message.length() == 0)
            return;
        textInput.getText().clear();
        handleRequest(BUTTONS.SEND_MESSAGE, new Object[]{message, getReceiverId()});
        displayMyMessage(message);
    }

    private void displayMessage(BaseUser from, String message, int toConversation) {
        if (shouldDisplay(from, toConversation))
            showMessage(from.toString(), message);
    }

    protected abstract void onClose();

    private void onCloseButtonClick(){
        InputMethodManager systemService = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        systemService.hideSoftInputFromWindow(textInput.getWindowToken(), 0);
        closeAction.run();
        onClose();
    }
}
