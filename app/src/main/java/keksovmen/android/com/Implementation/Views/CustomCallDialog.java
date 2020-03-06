package keksovmen.android.com.Implementation.Views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

import keksovmen.android.com.Implementation.BaseApplication;

public class CustomCallDialog extends DialogFragment implements ButtonsHandler, LogicObserver {

    private final ButtonsHandler helpHandlerPredecessor;

    private boolean isIncoming;
    private User user;
    private String dudes;

    private Runnable lastAction;

    public CustomCallDialog(ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (isIncoming) {
            builder.setTitle("Incoming call from");
            if (dudes != null && dudes.length() > 0) {
                builder.setMessage(user.toString() + "\nDudes in conversation with:\n" + dudes);
            } else {
                builder.setMessage(user.toString());
            }
            builder.setPositiveButton("Accept", this::onAccept);
            builder.setNegativeButton("Deny", this::onDeny);
        } else {
            builder.setTitle("Out going call to");
            builder.setMessage(user.toString());
            builder.setNegativeButton("Cancel", this::onCancel);
        }

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastAction != null) {
            lastAction.run();
            lastAction = null;
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (isIncoming) {
            onDeny(dialog, -1);
        } else {
            onCancel(dialog, -1);
        }
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        switch (buttons) {
            case CALL_ACCEPTED:
            case CALL_DENIED:
            case CALL_CANCELLED:
                dismiss();
                break;
        }
        helpHandlerPredecessor.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions) {
            case CALL_ACCEPTED:
            case CALL_DENIED:
            case CALL_CANCELLED:
                if (BaseApplication.isVisible()) {
                    dismiss();
                } else {
                    lastAction = this::dismiss;
                }
                break;
        }
    }

    public void showIncomingDialog(User caller, String hisDudes, FragmentManager manager) {
        isIncoming = true;
        user = caller;
        dudes = hisDudes;
        show(manager, "call_dialog");
    }

    public void showOutcomingDialog(User whoYouCall, FragmentManager manager) {
        isIncoming = false;
        user = whoYouCall;
        dudes = null;
        show(manager, "call_dialog");
    }

    private void onCancel(DialogInterface dialog, int which) {
        handleRequest(BUTTONS.CALL_CANCELLED, new Object[]{user, null});
    }

    private void onAccept(DialogInterface dialog, int which) {
        handleRequest(BUTTONS.CALL_ACCEPTED, new Object[]{user, dudes});
    }

    private void onDeny(DialogInterface dialog, int which) {
        handleRequest(BUTTONS.CALL_DENIED, new Object[]{user, dudes});
    }
}
