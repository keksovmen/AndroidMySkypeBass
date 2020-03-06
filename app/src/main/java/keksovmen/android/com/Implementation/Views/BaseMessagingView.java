package keksovmen.android.com.Implementation.Views;

import android.content.Context;
import android.text.InputType;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Collection.Track;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources.Resources;

import java.util.Map;

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


        textInput.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setOnClickListener(v -> sendMessage());
        closeButton.setOnClickListener(v -> onCloseButtonClick());

        textInput.setTooltipText("Type here");

//        messageDisplay.setOnTouchListener(new TouchListener());
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
                displayMessage((User) objects[0], (String) objects[1], (int) objects[2]);
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
        scrollView.setScrollbarFadingEnabled(false);
        scrollView.addView(messageDisplay);
        scrollView.setOnTouchListener(new TouchListener(context, scrollView));
        return scrollView;
    }

    protected abstract boolean shouldDisplay(User from, int toConversation);

    protected void showMessage(String from, String message) {
        messageDisplay.setText(messageDisplay.getText() +
                from + " (" + FormatWorker.getTime() + ") - " + message + "\n");
    }

    protected void showMyMessage(String message) {
        messageDisplay.setText(messageDisplay.getText() +
                "Me" + " (" + FormatWorker.getTime() + ") - " + message + "\n");
    }

    protected abstract int getReceiverId();

    private void sendMessage() {
        String message = textInput.getText().toString();
        if (message.length() == 0)
            return;
        textInput.getText().clear();
        handleRequest(BUTTONS.SEND_MESSAGE, new Object[]{message, getReceiverId()});
        showMyMessage(message);
    }

    private void displayMessage(User from, String message, int toConversation) {
        if (shouldDisplay(from, toConversation))
            showMessage(from.toString(), message);
    }

    protected abstract void onClose();

    private void onCloseButtonClick() {
        closeAction.run();
        onClose();
    }

    protected class TouchListener implements View.OnTouchListener {

        private final PopupMenu popupMenu;

        private float x1;
        private float y1;
        private long time1;

        public TouchListener(Context context, View anchor) {
            popupMenu = new PopupMenu(context, anchor);
            fillMenu();
        }

        private void fillMenu(){
            Menu menu = popupMenu.getMenu();
            Map<Integer, Track> tracks = Resources.getInstance().getNotificationTracks();
            tracks.forEach((integer, track) -> {
                menu.add(track.description).setOnMenuItemClickListener(item -> {
                    textInput.append(FormatWorker.asMessageMeta(integer));
                    return true;
                });
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    time1 = System.currentTimeMillis();
                    return true;
                case MotionEvent.ACTION_UP:
                    float x2 = event.getX();
                    float y2 = event.getY();
                    long time2 = System.currentTimeMillis();
                    if (x1 == x2 && y1 == y2 && time2 - time1 > 300){
                        //show dialog
                        popupMenu.show();
                    }
                    return true;

            }
            return false;
        }
    }
}
