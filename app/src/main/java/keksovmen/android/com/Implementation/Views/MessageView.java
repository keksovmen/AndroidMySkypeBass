package keksovmen.android.com.Implementation.Views;

import android.content.Context;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;

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

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        super.observe(actions, objects);
        switch (actions){
            case CALLED_BUT_BUSY:
                showMessage(objects[0].toString(), "I called you but you had been calling already, so call me later");
                break;
        }
    }
}
