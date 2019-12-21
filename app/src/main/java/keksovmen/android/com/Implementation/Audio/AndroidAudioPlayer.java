package keksovmen.android.com.Implementation.Audio;

import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Audio.Output.AudioPlayer;

public class AndroidAudioPlayer extends AudioPlayer {

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new AndroidCallNotificator();
    }

}
