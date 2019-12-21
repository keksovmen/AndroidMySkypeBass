package keksovmen.android.com.Implementation.Audio;

import android.media.AudioTrack;

import com.Abstraction.Audio.Misc.AbstractAudioFormat;
import com.Abstraction.Audio.Output.AudioOutputLine;

public class AndroidOutput implements AudioOutputLine {

    private final AudioTrack output;
    private final AbstractAudioFormat format;

    public AndroidOutput(AudioTrack output, AbstractAudioFormat format) {
        this.output = output;
        this.format = format;
    }

    @Override
    public boolean isVolumeChangeSupport() {
        return true;
    }

    @Override
    public void setVolume(int i) {
        output.setVolume(i / 100f);
    }


    @Override
    public int writeNonBlocking(byte[] bytes, int i, int i1) {
        return output.write(bytes, i, i1, AudioTrack.WRITE_NON_BLOCKING);
    }

    @Override
    public int writeBlocking(byte[] bytes, int i, int i1) {
        return output.write(bytes, i, i1, AudioTrack.WRITE_BLOCKING);
    }

    @Override
    public AbstractAudioFormat getFormat() {
        return format;
    }

    @Override
    public void close() {
        output.release();
    }

}
