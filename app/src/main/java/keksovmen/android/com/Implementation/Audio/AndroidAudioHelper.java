package keksovmen.android.com.Implementation.Audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Misc.AudioLineException;
import com.Abstraction.Audio.Output.AudioOutputLine;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources;

import java.io.IOException;
import java.util.Map;

import keksovmen.android.com.Implementation.BaseApplication;

public class AndroidAudioHelper extends AudioHelper {

    private static final String ROOT_TO_NOTIFICATIONS = "sounds/notifications/";

    private AbstractAudioFormat format;
    private int MIC_CAPTURE_SIZE;


    @Override
    public AudioOutputLine getOutput(int i, AbstractAudioFormat abstractAudioFormat) throws AudioLineException {
        AudioTrack.Builder builder = new AudioTrack.Builder();

        AudioFormat.Builder formatBuilder = new AudioFormat.Builder();
        formatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
        formatBuilder.setSampleRate(abstractAudioFormat.getSampleRate());
        formatBuilder.setChannelMask(AudioFormat.CHANNEL_OUT_MONO);

        builder.setAudioFormat(formatBuilder.build());
        builder.setAudioAttributes(new AudioAttributes.Builder().
                setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).
                setUsage(AudioAttributes.USAGE_MEDIA).
                build());

        int bufferSize = MIC_CAPTURE_SIZE * 2;// if too high there is a huge delay before start playing if too small there are glitches, play with it
        builder.setBufferSizeInBytes(bufferSize);

        AudioTrack track = builder.build();
        track.play();
        return new AndroidOutput(track, abstractAudioFormat);
    }

    @Override
    public AudioOutputLine getOutput(int i) throws AudioLineException {
        return getOutput(i, format);
    }

    @Override
    public void playResourceFile(int idOfMixer, int trackId) {
        MediaPlayer player = new MediaPlayer();
        player.setOnCompletionListener(MediaPlayer::release);
        try {
            player.setDataSource(BaseApplication.getContext().getAssets().openFd(ROOT_TO_NOTIFICATIONS +
                    Resources.getInstance().getNotificationTracks().get(trackId).name));
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
            player.release();
            return;
        }
    }

    @Override
    public int getDefaultForOutput() {
        return 0;
    }

    @Override
    public int getDefaultForInput() {
        return 0;
    }

    @Override
    public AudioInputLine getInput(int i, AbstractAudioFormat abstractAudioFormat) throws AudioLineException {
        AudioRecord.Builder builder = new AudioRecord.Builder();

        AudioFormat.Builder formatBuilder = new AudioFormat.Builder();
        formatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
        formatBuilder.setSampleRate(abstractAudioFormat.getSampleRate());
        formatBuilder.setChannelMask(AudioFormat.CHANNEL_IN_MONO);

        builder.setAudioFormat(formatBuilder.build());
        builder.setAudioSource(MediaRecorder.AudioSource.MIC);
        builder.setBufferSizeInBytes(MIC_CAPTURE_SIZE * 4);

        AudioRecord record = builder.build();
        record.startRecording();
        return new AndroidInput(record, abstractAudioFormat);
    }

    @Override
    public AudioInputLine getInput(int i) throws AudioLineException {
        return getInput(i, format);
    }

    @Override
    public int getMicCaptureSize() {
        return MIC_CAPTURE_SIZE;
    }

    @Override
    public Map<Integer, String> getOutputLines() {
        return null;
    }

    @Override
    public Map<Integer, String> getInputLines() {
        return null;
    }

    @Override
    public AbstractAudioFormat getAudioFormat() {
        return format;
    }

    @Override
    public boolean isFormatSupported(String s) {
        AbstractAudioFormat abstractFormat = FormatWorker.parseAudioFormat(s);
        if (abstractFormat.getSampleSizeInBits() / 8 != 2)
            return false;
        int micSize = FormatWorker.parseMicCaptureSize(s);
        format = abstractFormat;
        MIC_CAPTURE_SIZE = micSize;
        return true;
    }
}
