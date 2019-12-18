package keksovmen.android.com.Implementation.Audio;

import android.media.MediaPlayer;
import android.net.Uri;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.Output.AbstractAudioPlayer;
import com.Abstraction.Audio.Output.AbstractCallNotificator;
import com.Abstraction.Util.Resources;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import keksovmen.android.com.BaseApplication;

public class AndroidAudioPlayer extends AbstractAudioPlayer {

    private final ExecutorService executorService;
//
    public AndroidAudioPlayer() {
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    protected AbstractCallNotificator createCallNotificator() {
        return new AndroidCallNotificator();
    }

    @Override
    public void playMessage() {
//        playMessageSound(1);
        int track = ThreadLocalRandom.current().nextInt(0, Resources.getInstance().getNotificationTracks().size());
        playMessage(track);
    }

    @Override
    public void playMessage(int track) {
        if (Resources.getInstance().getNotificationTracks().size() <= track || track < 0) {
            playMessage(); //Play random one
            return;
        }
        executorService.execute(() -> AudioSupplier.getInstance().playResourceFile(outputMixerId, track));
    }

    @Override
    public void playMessage(int track, int delay) {
        executorService.execute(() -> {
            try {
                Thread.sleep(delay);
                playMessage(track);
//                AudioSupplier.getInstance().playResourceFile(outputMixerId, track);
            } catch (InterruptedException ignored) {
            }
        });
    }

//    private void playMessageSound(int uniqueResourceIndex) {
//        MediaPlayer player = MediaPlayer.create(BaseApplication.getContext(), uniqueResourceIndex);
////        MediaPlayer player = new MediaPlayer();
//        player.setOnCompletionListener(MediaPlayer::release);
//        player.start();
//    }

//    @Override
//    public synchronized void playSound(int who, byte[] data) {
//        executorService.submit(() -> super.playSound(who, data));
//    }
}
