package keksovmen.android.com.Implementation.Audio;

import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Audio.Input.ChangeableInput;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.ChangeableOutput;
import com.Abstraction.Audio.Settings.BaseAudioSettings;
import com.Abstraction.Client.ButtonsHandler;

public class AndroidAudioFactory extends AudioFactory {

    @Override
    public AbstractAudioPlayer createPlayer() {
        return new AndroidAudioPlayer();
    }

    @Override
    public AbstractMicrophone createMicrophone(ButtonsHandler buttonsHandler) {
        return new AndroidMicrophone(buttonsHandler);
    }

    @Override
    public BaseAudioSettings createSettings(ChangeableInput changeableInput, ChangeableOutput changeableOutput) {
        return new BaseAudioSettings(changeableInput, changeableOutput);
    }
}
