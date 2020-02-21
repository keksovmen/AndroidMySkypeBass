package keksovmen.android.com.Implementation.Util;

import com.Abstraction.Util.Collection.Pair;
import com.Abstraction.Util.Collection.Track;
import com.Abstraction.Util.Resources.AbstractResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import keksovmen.android.com.Implementation.BaseApplication;

public class AndroidResources extends AbstractResources {

    private final Map<Integer, Track> notificationTracks;


    public AndroidResources() {
        notificationTracks = new HashMap<>();
        List<Pair<String, String>> pairs = null;
        try {
            pairs = XMLWorker.parseDocument(BaseApplication.getContext().getAssets().open("sounds/Notifications.xml"));
        } catch (IOException e) {
            e.printStackTrace();
            pairs = new ArrayList<>();
        }
        int id = 0;
        for (Pair<String, String> pair : pairs) {
            notificationTracks.put(id, new Track(pair.getFirst(), pair.getSecond()));
            id++;
        }
    }

    @Override
    protected Properties initialisation(Properties properties) {
        return properties;
    }

    @Override
    public Map<Integer, Track> getNotificationTracks() {
        return Collections.unmodifiableMap(notificationTracks);
    }
}
