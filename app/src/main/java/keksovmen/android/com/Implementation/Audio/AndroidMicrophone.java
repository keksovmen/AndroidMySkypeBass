package keksovmen.android.com.Implementation.Audio;

import com.Abstraction.Audio.Input.AbstractMicrophone;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Util.Algorithms;

public class AndroidMicrophone extends AbstractMicrophone {

    private final static int MIN_LVL = 1;
    private final static int MAX_LVL = 20;

    private volatile float currentLvl = 1f;

    public AndroidMicrophone(ButtonsHandler helpHandlerPredecessor) {
        super(helpHandlerPredecessor);
    }

    @Override
    protected byte[] bassBoost(byte[] bytes) {
        if (currentLvl == MIN_LVL)
            return bytes;
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] * currentLvl);
        }
        return bytes;
    }

    @Override
    public void changeBassLevel(int i) {
        currentLvl = Algorithms.findPercentage(MIN_LVL, MAX_LVL, i);
    }

    @Override
    public synchronized void close() {
        super.close();
        currentLvl = 1f;
    }
}
