package keksovmen.android.com.Implementation.Audio;

import android.media.MediaPlayer;

import com.Abstraction.Audio.Output.AbstractCallNotificator;

import java.io.IOException;

import keksovmen.android.com.Implementation.BaseApplication;

public class AndroidCallNotificator extends AbstractCallNotificator {

    private MediaPlayer player;

    @Override
    public boolean start(String name) {
        if (isWorking)
            return false;
        isWorking = true;
        player = new MediaPlayer();
        try {
            player.setDataSource(BaseApplication.getActiveContext().getAssets().openFd("sounds/call/Call.WAV"));
            player.prepare();
            player.setLooping(true);
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
            isWorking = false;
        }
        return true;
    }

    @Override
    public void close() {
        if (isWorking) {
            player.stop();
            player.release();
            isWorking = false;
        }
    }

    @Override
    protected void playCallSound() {

    }
}
