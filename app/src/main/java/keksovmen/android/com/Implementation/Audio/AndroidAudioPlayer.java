package keksovmen.android.com.Implementation.Audio;

import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.BasicAudioPlayer;

public class AndroidAudioPlayer extends BasicAudioPlayer {

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new AndroidCallNotificator();
    }

}
