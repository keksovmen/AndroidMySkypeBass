package keksovmen.android.com.Implementation.Views.SmallParts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

public class ControlPane implements ButtonsHandler, LogicObserver {

    private final ButtonsHandler helpHandlerPredecessor;

    private final ConstraintLayout mainLayout;
    private final ConstraintLayout firstStepLayout;
    private final TextView bassInfo;
    private final Switch muteSwitch;
    private final SeekBar bassLevelBar;

    public ControlPane(Context context, ButtonsHandler helpHandlerPredecessor) {
        this.helpHandlerPredecessor = helpHandlerPredecessor;

        mainLayout = new ConstraintLayout(context);
        firstStepLayout = new ConstraintLayout(context);
        bassInfo = new TextView(context);
        muteSwitch = new Switch(context);
        bassLevelBar = new SeekBar(context);

        mainLayout.setId(View.generateViewId());
        firstStepLayout.setId(View.generateViewId());
        bassInfo.setId(View.generateViewId());
        muteSwitch.setId(View.generateViewId());
        bassLevelBar.setId(View.generateViewId());

        firstStepLayout.addView(bassInfo, createParamsForTextInfo());
        firstStepLayout.addView(muteSwitch, createParamsForSwitch());
        mainLayout.addView(firstStepLayout, createParamsForFirstPane());
        mainLayout.addView(bassLevelBar, createParamsForBar());

        muteSwitch.setOnClickListener(createSwitchListener());
        bassLevelBar.setOnSeekBarChangeListener(createBarListener());

        bassInfo.setText("Bass level - " + bassLevelBar.getProgress());

        muteSwitch.setText("Mute");
        muteSwitch.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
    }

    @Override
    public void handleRequest(BUTTONS buttons, Object[] objects) {
        helpHandlerPredecessor.handleRequest(buttons, objects);
    }

    @Override
    public void observe(ACTIONS actions, Object[] objects) {
        switch (actions){
            case EXITED_CONVERSATION:{
                muteSwitch.setChecked(false);
                break;
            }
        }
    }

    public ConstraintLayout getMainLayout() {
        return mainLayout;
    }

    protected ConstraintLayout.LayoutParams createParamsForTextInfo(){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = firstStepLayout.getId();
        params.leftToLeft = firstStepLayout.getId();
        params.rightToLeft = muteSwitch.getId();
        params.bottomToBottom = firstStepLayout.getId();
        return params;
    }

    protected ConstraintLayout.LayoutParams createParamsForSwitch(){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = firstStepLayout.getId();
        params.rightToRight = firstStepLayout.getId();
        params.bottomToBottom = firstStepLayout.getId();
        return params;
    }

    protected ConstraintLayout.LayoutParams createParamsForFirstPane(){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = mainLayout.getId();
        params.leftToLeft = mainLayout.getId();
        params.rightToRight = mainLayout.getId();
        return params;
    }

    protected ConstraintLayout.LayoutParams createParamsForBar(){
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToBottom = firstStepLayout.getId();
        params.leftToLeft = mainLayout.getId();
        params.rightToRight = mainLayout.getId();
        params.bottomToBottom = mainLayout.getId();
        return params;
    }

    protected SeekBar.OnSeekBarChangeListener createBarListener(){
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bassInfo.setText("Bass level - " + progress);
                helpHandlerPredecessor.handleRequest(BUTTONS.INCREASE_BASS, new Object[]{progress});
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    protected View.OnClickListener createSwitchListener(){
        return v -> helpHandlerPredecessor.handleRequest(BUTTONS.MUTE, null);
    }

}
