package keksovmen.android.com.Implementation;

import com.Abstraction.AbstractApplicationFactory;
import com.Abstraction.Audio.BaseAudio;
import com.Abstraction.Audio.Factory.AudioFactory;
import com.Abstraction.Audio.Helper.AudioHelper;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.AbstractResources;

import keksovmen.android.com.Implementation.Audio.AndroidAudioFactory;
import keksovmen.android.com.Implementation.Audio.AndroidAudioHelper;
import keksovmen.android.com.Implementation.Client.AndroidClient;
import keksovmen.android.com.Implementation.Util.AndroidResources;

public class AndroidApplicationFactory extends AbstractApplicationFactory {

    private final CompositeComponent frame;

    public AndroidApplicationFactory(CompositeComponent frame) {
        this.frame = frame;
    }


    @Override
    public AudioHelper createAudioHelper() {
        return new AndroidAudioHelper();
    }

    @Override
    public AbstractResources createResources() {
        return new AndroidResources();
    }

    @Override
    public AbstractClient createClient(ChangeableModel changeableModel) {
        return new AndroidClient(changeableModel);
    }

    @Override
    public CompositeComponent createGUI() {
        return frame;
    }

    @Override
    public SimpleComponent createAudio(ButtonsHandler buttonsHandler, AudioFactory audioFactory) {
        return new BaseAudio(buttonsHandler, audioFactory);
    }

    @Override
    public AudioFactory createAudioFactory() {
        return new AndroidAudioFactory();
    }
}
