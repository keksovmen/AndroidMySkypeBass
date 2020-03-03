package keksovmen.android.com.Implementation.Views;

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
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.SimpleComponent;

import java.util.function.Consumer;

import keksovmen.android.com.Implementation.BaseApplication;


public class UsersView implements SimpleComponent {

    private static final String SEND_MESSAGE = "Send message";
    private static final String CALL = "call";

    private final ConstraintLayout mainLayout;
    private final LinearLayout usersPlace;
    private final LinearLayout buttonHolder;
    private final Button disconnectButton;
    private final Button refreshButton;

    private final Context context;
    private final ButtonsHandler helpHandlerPredecessor;
    private final Consumer<User> openMessagePaneCommand;


    public UsersView(Context context, ButtonsHandler helpHandlerPredecessor, Consumer<User> openMessagePaneCommand) {
        mainLayout = new ConstraintLayout(context);
        usersPlace = new LinearLayout(context);
        buttonHolder = new LinearLayout(context);
        disconnectButton = new Button(context);
        refreshButton = new Button(context);

        this.context = context;
        this.helpHandlerPredecessor = helpHandlerPredecessor;
        this.openMessagePaneCommand = openMessagePaneCommand;

        usersPlace.setOrientation(LinearLayout.VERTICAL);
        buttonHolder.setOrientation(LinearLayout.HORIZONTAL);

        mainLayout.setId(View.generateViewId());
        buttonHolder.setId(View.generateViewId());

        mainLayout.addView(createUsersDisplay(context), createDisplayParams());
        mainLayout.addView(createButtonLayout(), createButtonsParams());


        disconnectButton.setOnClickListener(v -> onDisconnectPress());
        refreshButton.setOnClickListener(v -> onRefreshPressed());

        refreshButton.setText("Refresh");
        disconnectButton.setText("Disconnect");

    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        helpHandlerPredecessor.handleRequest(buttons, objects);

    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {

    }

    @Override
    public void modelObservation(UnEditableModel unEditableModel) {
        usersPlace.removeAllViews();
        unEditableModel.getUserMap().values().forEach(this::addUser);

    }

    public ConstraintLayout getMainLayout() {
        return mainLayout;
    }


    private void addUser(User user) {
        usersPlace.addView(createTextViewForUser(user));
    }

    private TextView createTextViewForUser(User user) {
        TextView textView = new TextView(context);
        textView.setText(user.toString());
        textView.setTextSize(BaseApplication.TEXT_SIZE);

        PopupMenu popupMenu = createPopUpMenuForUser(textView);
        popupMenu.setOnMenuItemClickListener(new PopupUserListener(user, textView));

        textView.setOnClickListener(v -> popupMenu.show());

        return textView;
    }

    private PopupMenu createPopUpMenuForUser(View anchor) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        Menu menu = popupMenu.getMenu();
        menu.add(SEND_MESSAGE);
        menu.add(CALL);
        return popupMenu;
    }

    private ScrollView createUsersDisplay(Context context) {
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(usersPlace);
        return scrollView;
    }

    private ConstraintLayout.LayoutParams createDisplayParams() {
        ConstraintLayout.LayoutParams layoutParamsDisplay = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        layoutParamsDisplay.topToTop = mainLayout.getId();
        layoutParamsDisplay.bottomToTop = buttonHolder.getId();
        layoutParamsDisplay.leftToLeft = mainLayout.getId();
        layoutParamsDisplay.rightToRight = mainLayout.getId();
        return layoutParamsDisplay;
    }

    private LinearLayout createButtonLayout() {
        buttonHolder.addView(refreshButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        buttonHolder.addView(disconnectButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        return buttonHolder;
    }

    private ConstraintLayout.LayoutParams createButtonsParams() {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 0);
        layoutParams.bottomToBottom = mainLayout.getId();
        layoutParams.leftToLeft = mainLayout.getId();
        layoutParams.rightToRight = mainLayout.getId();
        return layoutParams;
    }

    private void onDisconnectPress() {
        handleRequest(BUTTONS.DISCONNECT, null);
    }

    private void onRefreshPressed() {
        handleRequest(BUTTONS.ASC_FOR_USERS, null);
    }


    private class PopupUserListener implements PopupMenu.OnMenuItemClickListener {

        private final User user;
        private final View textView;

        public PopupUserListener(User user, View textView) {
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
                    openMessagePaneCommand.accept(user);
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
