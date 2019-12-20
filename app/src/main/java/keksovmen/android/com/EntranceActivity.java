package keksovmen.android.com;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.Abstraction.Client.LogicObserver;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;

import keksovmen.android.com.Implementation.NumberRangeFilter;

public class EntranceActivity extends AppCompatActivity implements LogicObserver {

    private static final int AUDIO_CODE = 1;
    public static final int RETURN_CODE = 2;
    public static final String STATUS_CODE = "STATUS_CODE";

    public enum ResultCodes {STATUS_OK, STATUS_NETWORK_FAILURE}


    private BaseApplication application;

    private Button connectButton;
    private EditText nameField;
    private EditText hostNameField;
    private EditText portField;
    private ConstraintLayout progressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        application = (BaseApplication) getApplication();
        connectButton = findViewById(R.id.connect_button);

        nameField = findViewById(R.id.name_field);
        hostNameField = findViewById(R.id.host_name_field);
        portField = findViewById(R.id.port_field);
        progressLayout = findViewById(R.id.progress_circular);
        portField.setFilters(new InputFilter[]{new NumberRangeFilter(0, 0xffff)});
    }


    @Override
    protected void onStart() {
        super.onStart();
        application.setState(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_CODE);
        }
    }

    public void connect(View view) {


        Object[] data = new String[3];
        data[0] = nameField.getText().toString();
        data[1] = hostNameField.getText().toString();
        data[2] = portField.getText().toString();
        //add format checks
        animationOnConnectInProgress(connectButton);
        application.handleRequest(BUTTONS.CONNECT, data);
    }

    private void animationOnConnectInProgress(View view) {
        view.setEnabled(false);
        animationOnOrOff(connectButton, true, false, () -> {
            progressLayout.setVisibility(View.VISIBLE);
        });
    }

    private void animationOnConnect(View view) {
        animationOnOrOff(connectButton, false, true, () -> {
            progressLayout.setVisibility(View.GONE);
            view.setEnabled(true);
        });
    }


    @Override
    public void observe(ACTIONS actions, Object[] data) {
        switch (actions) {
            case CONNECT_SUCCEEDED:
            case ALREADY_CONNECTED_TO_SERVER:
                onConnect();
                break;
            case WRONG_HOST_NAME_FORMAT:
                BaseApplication.showDialog(this, "Wrong host name format - " + data[0] +
                        "\nMust be xxx.xxx.xxx.xxx where xxx is number from 0 to 255");
                break;
            case WRONG_PORT_FORMAT:
                BaseApplication.showDialog(this, "Wrong port format - " + data[0]);
                break;
            case PORT_OUT_OF_RANGE:
                BaseApplication.showDialog(this, "Port is out of range, must be in "
                        + data[0] + ". But yours is " + data[1]);
                break;
            case AUDIO_FORMAT_NOT_ACCEPTED:
                BaseApplication.showDialog(this, "Not connected, because your " +
                        "audio system can't handle this format {" + " " + data[0] + " }");
                break;
            case CONNECT_FAILED:
                animationOnConnect(connectButton);
                BaseApplication.showDialog(this, "Connection failed because server didn't answer");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectButton.setEnabled(true);
            } else {
                connectButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RETURN_CODE) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(STATUS_CODE);
                switch (ResultCodes.valueOf(result)) {
                    case STATUS_OK:
                        break;
                    case STATUS_NETWORK_FAILURE:
                        BaseApplication.showDialog(this, "Connection between you and server has failed, check internet connection or server status");
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onConnect() {
        Intent intentWithResultCode = new Intent(this, MainActivity.class);
        startActivityForResult(intentWithResultCode, RETURN_CODE);
        animationOnConnect(connectButton);
    }


    private static void animationOnOrOff(View view, boolean isShrinking, boolean resultVisibility, Runnable actionOnEndAnimation) {
        final int state = resultVisibility ? View.VISIBLE : View.GONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getWidth() / 2;
            int cy = view.getHeight() / 2;

            float startRadios = 0;
            float endRadios = 0;
            if (isShrinking) {
                startRadios = (float) Math.hypot(cx, cy);
            } else {
                endRadios = (float) Math.hypot(cx, cy);
            }

            Animator animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadios, endRadios);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(state);
                    actionOnEndAnimation.run();
                }
            });
            if (resultVisibility) {
                view.setVisibility(state);
            }

            animator.start();
        } else {
            view.setVisibility(state);
        }
    }
}
