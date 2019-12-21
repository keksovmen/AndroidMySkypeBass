package keksovmen.android.com.Implementation;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Model.UnEditableModel;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Pipeline.CompositeComponent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class BaseApplication extends Application implements CompositeComponent {

    public static final int TEXT_SIZE = 20;
    public static final List<ACTIONS> actionsMapping = Collections.unmodifiableList(Arrays.asList(ACTIONS.values()));
    private static Context context;

    private Handler handler;

    private List<ButtonsHandler> listeners;
    private MutableLiveData<UnEditableModel> liveData;

    private Queue<Message> messageQueue;


    private volatile LogicObserver state;


    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.context = getApplicationContext();
        handler = new Handler(Looper.getMainLooper(), this::handlerActions);

        listeners = new ArrayList<>();

        liveData = new MutableLiveData<>();

        messageQueue = new ArrayDeque<>(8);

        AndroidApplicationFactory factory = new AndroidApplicationFactory(this);
        com.Abstraction.Application application = new com.Abstraction.Application(factory);
        application.start();
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        switch (buttons) {
            //do your action as proxy
        }
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

    public void setState(LogicObserver holder) {
        System.out.println("STATE CHANGED TO - " + holder);
        state = holder;
        Message m;
        while ((m = messageQueue.poll()) != null) {
            processMessage(m);
        }
    }


    private boolean handlerActions(Message message) {
        if (state == null) {
            messageQueue.add(message);
            return true;
        }
        processMessage(message);
        return true;
    }

    private void processMessage(Message message) {
        ACTIONS actions = actionsMapping.get(message.what);
        Object[] data = (Object[]) message.obj;

        state.observe(actions, data);
    }

    public static void showDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.show();
    }

    public static Context getContext() {
        return context;
    }
}
