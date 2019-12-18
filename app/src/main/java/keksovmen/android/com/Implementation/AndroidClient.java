package keksovmen.android.com.Implementation;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Pipeline.BUTTONS;

public class AndroidClient extends AbstractClient {


    public AndroidClient(ChangeableModel model) {
        super(model);
    }

    @Override
    protected void additionalCases(BUTTONS buttons, Object[] objects) {

    }

    @Override
    protected String createDefaultName() {
        return "Android";
    }
}
