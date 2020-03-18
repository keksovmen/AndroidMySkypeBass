package keksovmen.android.com.Implementation.Util.Logging;

import com.Abstraction.Util.Logging.LogManagerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import keksovmen.android.com.Implementation.BaseApplication;

public class AndroidLogManagerHelper extends LogManagerHelper {

    @Override
    protected boolean prepareFiles() {
        return true;
    }

    @Override
    protected InputStream getPropertiesStream() throws IOException {
        return BaseApplication.getActiveContext().getAssets().open("properties/logging.properties");
    }

    @Override
    public void init() {
        if (!prepareFiles())
            return;
        try {
            LogManager.getLogManager().readConfiguration(getPropertiesStream());
            clientLogger.init();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Logger manager is failed due to properties reading");
        }
    }
}
