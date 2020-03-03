package keksovmen.android.com.Implementation.Views.SmallParts;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.Abstraction.Client.ButtonsHandler;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Pipeline.BUTTONS;

public class SettingsEntry {

    private final LinearLayout layout;
    private final TextView nameLabel;
    private final SeekBar volumeBar;

    private final User user;
    private final ButtonsHandler helpHandlerPredecessor;

    public SettingsEntry(User user, Context context, ButtonsHandler helpHandlerPredecessor) {
        this.user = user;
        this.helpHandlerPredecessor = helpHandlerPredecessor;

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        nameLabel = new TextView(context);
        nameLabel.setText(user.toString());

        volumeBar = new SeekBar(context);
        volumeBar.setOnSeekBarChangeListener(new VolumeBarListener());
        volumeBar.setProgress(100);


        layout.addView(nameLabel);
        layout.addView(volumeBar);

    }

    public View getPane() {
        return layout;
    }


    private class VolumeBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            helpHandlerPredecessor.handleRequest(BUTTONS.VOLUME_CHANGED, new Object[]{user.getId(), progress});
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}
