package keksovmen.android.com.Implementation.Audio;

import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Client.ButtonsHandler;

public class AndroidMicrophone extends AbstractMicrophone {

    public AndroidMicrophone(ButtonsHandler helpHandlerPredecessor) {
        super(helpHandlerPredecessor);
    }

    @Override
    protected byte[] bassBoost(byte[] bytes) {
        return bytes;
    }

    @Override
    public void IncreaseBass(int i) {

    }
}
