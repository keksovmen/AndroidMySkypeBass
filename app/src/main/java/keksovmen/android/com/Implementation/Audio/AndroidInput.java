package keksovmen.android.com.Implementation.Audio;

import android.media.AudioRecord;

import com.Abstraction.Audio.Input.AudioInputLine;
import com.Abstraction.Audio.Misc.AbstractAudioFormat;

public class AndroidInput implements AudioInputLine {

    private final AudioRecord record;
    private final AbstractAudioFormat format;

    public AndroidInput(AudioRecord record, AbstractAudioFormat format) {
        this.record = record;
        this.format = format;
    }

    @Override
    public int readNonBlocking(byte[] bytes, int i, int i1) {
        return record.read(bytes, i, i1, AudioRecord.READ_NON_BLOCKING);
    }

    @Override
    public int readBlocking(byte[] bytes, int i, int i1) {
        return record.read(bytes, i, i1);
    }

    @Override
    public AbstractAudioFormat getFormat() {
        return format;
    }

    @Override
    public void close() {
        record.release();
    }
}
