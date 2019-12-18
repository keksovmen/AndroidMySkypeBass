package keksovmen.android.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Model.Updater;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Util.Interfaces.Registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import keksovmen.android.com.Implementation.AndroidApplicationFactory;

public class BaseApplication extends Application implements CompositeComponent {

    public static final List<ACTIONS> actionsMapping = Arrays.asList(ACTIONS.values());
    private static Context context;

    private Handler handler;

    private List<ButtonsHandler> listeners;
    private MutableLiveData<UnEditableModel> liveData;


    private volatile LogicObserver state;



    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.context = getApplicationContext();
        handler = new Handler(Looper.getMainLooper(), this::handlerActions);

        listeners = new ArrayList<>();

        liveData = new MutableLiveData<>();

        AndroidApplicationFactory factory = new AndroidApplicationFactory(this);
        com.Abstraction.Application application = new com.Abstraction.Application(factory);
        application.start();
//        Main.getInstance().init();
//        Main.getInstance().attachFrame(this);
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        switch (buttons) {
            //do your action as proxy
        }
//        Main.getInstance().handleRequest(buttons, objects);
        listeners.forEach(buttonsHandler -> buttonsHandler.handleRequest(buttons, objects));
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        //your actions
        //put on UI thread
        System.out.println("OBSERVATION - " + actions);
        int i = actionsMapping.indexOf(actions);
        handler.obtainMessage(i, objects).sendToTarget();

    }

    @Override
    public void update(UnEditableModel unEditableModel) {
        liveData.postValue(unEditableModel);
    }

    @Override
    public void attach(ButtonsHandler buttonsHandler) {
        listeners.add(buttonsHandler);
    }

    @Override
    public void detach(ButtonsHandler buttonsHandler) {
        listeners.remove(buttonsHandler);
    }

    public LiveData<UnEditableModel> getLiveData() {
        return liveData;
    }

    public synchronized void setState(LogicObserver holder) {
        System.out.println("STATE CHANGED TO - " + holder);
        state = holder;
//        if (lastSignificantAction != null)
//            state.getObserver().observe(lastSignificantAction, null);
        notify();
    }

//    public synchronized void setLastSignificantAction(ACTIONS action) {
//        lastSignificantAction = action;
//    }

//    public void showCallDialog(boolean isIncoming, BaseUser who, String dudes){
//        callDialog.showDialog(isIncoming, who, dudes, state.getManager());
//    }

    private boolean handlerActions(Message message) {
        ACTIONS actions = actionsMapping.get(message.what);
        Object[] data = (Object[]) message.obj;
//        switch (actions){
//            case OUT_CALL:{
//                showCallDialog(false, (BaseUser) data[0], null);
//                return true;
//            }
//            case INCOMING_CALL:{
//                showCallDialog(true, (BaseUser) data[0], (String) data[1]);
//                return true;
//            }
//            case CALL_DENIED:{
//                showDenyDialog((BaseUser)data[0]);
//                return true;
//
//            }
//            case CALL_CANCELLED: {
//                showCancelDialog((BaseUser)data[0]);
//                return true;
//            }
//            case CALL_ACCEPTED:{
//                onCallAccept();
//                break;
////                return true;
//            }
//        }


        //Think about this mess seems like a fucking trap
        while (state == null) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
        }

        state.observe(actions, data);

        return true;
    }

    public static void showDialog(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.show();
    }

    public static Context getContext() {
        return context;
    }
}
