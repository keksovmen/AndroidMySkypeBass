package keksovmen.android.com.Views;

import android.content.Context;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Networking.Utility.Users.BaseUser;

public class MessageView extends BaseMessagingView {

    private final BaseUser dude;

    public MessageView(Context context, ButtonsHandler helpHandler, Runnable closeAction, BaseUser dude) {
        super(context, helpHandler, closeAction);
        this.dude = dude;
        build(context);
        //open keyboard
        closeButton.setText("Close");

    }

    @Override
    protected boolean shouldDisplay(BaseUser from, int toConversation) {
        return toConversation == 0 && from.getId() == dude.getId();
    }

    @Override
    protected int getReceiverId() {
        return dude.getId();
    }

    @Override
    protected void onClose() {

    }

}
