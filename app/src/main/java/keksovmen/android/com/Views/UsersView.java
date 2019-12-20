package keksovmen.android.com.Views;

import android.animation.LayoutTransition;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;

import java.util.function.Consumer;


public class UsersView implements SimpleComponent {

    private static final String SEND_MESSAGE = "Send message";
    private static final String CALL = "call";

    private final ConstraintLayout layout;
    private final LinearLayout usersPlace;
    private final LinearLayout buttonHolder;
    private final Button disconnectButton;
    private final Button refreshButton;

    private final Context context;
    private final ButtonsHandler helpHandlerPredecessor;
    private final Consumer<BaseUser> openMessagePane;

    public UsersView(Context context, ButtonsHandler helpHandlerPredecessor, Consumer<BaseUser> openMessagePane) {
        layout = new ConstraintLayout(context);
        usersPlace = new LinearLayout(context);
        buttonHolder = new LinearLayout(context);
        disconnectButton = new Button(context);
        refreshButton = new Button(context);

        this.context = context;
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        this.openMessagePane = openMessagePane;

        layout.setId(View.generateViewId());
        buttonHolder.setId(View.generateViewId());


        ConstraintLayout.LayoutParams layoutParamsDisplay = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        layoutParamsDisplay.topToTop = layout.getId();
        layoutParamsDisplay.bottomToTop = buttonHolder.getId();
        layout.addView(createUsersDisplay(context), layoutParamsDisplay);


        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0);
        layoutParams.bottomToBottom = layout.getId();
        layout.addView(createButtonLayout(), layoutParams);


        disconnectButton.setOnClickListener(v -> onDisconnectPress());
        refreshButton.setOnClickListener(v -> onRefreshPressed());

//        usersPlace.setLayoutTransition(new LayoutTransition());

    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        helpHandlerPredecessor.handleRequest(buttons, objects);

    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {

    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        usersPlace.removeAllViews();
        unEditableModel.getUserMap().values().forEach(this::addUser);

    }

    private void addUser(BaseUser user) {
        usersPlace.addView(createTextViewForUser(user));
    }

    private TextView createTextViewForUser(BaseUser user) {
        TextView textView = new TextView(context);
        textView.setText(user.toString());
        textView.setTextSize(20);

        PopupMenu popupMenu = new PopupMenu(context, textView);
        Menu menu = popupMenu.getMenu();
        menu.add(SEND_MESSAGE);
        menu.add(CALL);
        popupMenu.setOnMenuItemClickListener(new PopupUserListener(user, textView));

        textView.setOnClickListener(v -> popupMenu.show());

        return textView;
    }

    private ScrollView createUsersDisplay(Context context) {
        ScrollView scrollView = new ScrollView(context);
        usersPlace.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(usersPlace);

        return scrollView;
    }

    private LinearLayout createButtonLayout() {
        refreshButton.setText("Refresh");

        disconnectButton.setText("Disconnect");

        buttonHolder.setOrientation(LinearLayout.HORIZONTAL);

        buttonHolder.addView(refreshButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        buttonHolder.addView(disconnectButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return buttonHolder;
    }

    private void onDisconnectPress() {
        handleRequest(BUTTONS.DISCONNECT, null);
    }

    private void onRefreshPressed() {
        handleRequest(BUTTONS.ASC_FOR_USERS, null);
    }

    public ConstraintLayout getLayout() {
        return layout;
    }


    private class PopupUserListener implements PopupMenu.OnMenuItemClickListener {

        private final BaseUser user;
        private final View textView;

        public PopupUserListener(BaseUser user, View textView) {
            this.user = user;
            this.textView = textView;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (!textView.isShown())
                return true;
            String title = item.getTitle().toString();
            switch (title) {
                case SEND_MESSAGE: {
                    openMessagePane.accept(user);
                    break;
                }
                case CALL: {
                    handleRequest(BUTTONS.CALL, new Object[]{user});
                    break;
                }
            }
            return true;
        }
    }
}
