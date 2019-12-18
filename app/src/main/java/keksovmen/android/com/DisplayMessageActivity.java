package keksovmen.android.com;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;

public class DisplayMessageActivity extends AppCompatActivity {

    private static final int AUDIO_CODE = 1;
//        private MediaPlayer player;
    private volatile boolean isWorking;
    private byte[] buffer = new byte[1024*120];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        player = MediaPlayer.create(this, R.raw.punish);
//        player.start();

//        runAudioReader();

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
//                PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    AUDIO_CODE);
//        }else {
////            new Thread(() -> {
////                writeRecord();
////                readRecord();
////            }).start();
//        }

        // Получаем сообщение из объекта intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(EntranceActivity.EXTRA_MESSAGE);


        LinearLayout linearLayout = new LinearLayout(this);

        // Создаем текстовое поле
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);

        Button button = new Button(this);
        button.setText("Return");

        button.setOnClickListener((view) -> runAudioReader());


        linearLayout.addView(textView);
        linearLayout.addView(button);


        // Устанавливаем текстовое поле в системе компоновки activity
        setContentView(linearLayout);
    }

    @Override
    protected void onStop() {
        isWorking = false;
        super.onStop();
    }

    @Override
    protected synchronized void onDestroy() {
//        player.release();
        this.notify();
        super.onDestroy();
    }

    @Override
    protected synchronized void onResume() {
        isWorking = true;
        this.notify();
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case AUDIO_CODE:{
                new Thread(() -> {
                    writeRecord();
                    readRecord();
                }).start();
                break;
            }
        }
    }

    private void runAudioReader() {
        isWorking = true;
        new Thread(() -> {
            AudioTrack.Builder builder = new AudioTrack.Builder();
            builder.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            builder.setAudioFormat(new AudioFormat.Builder()
                    .setSampleRate(44100)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build());
            builder.setBufferSizeInBytes(44100);
            AudioTrack track = builder.build();

            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(getResources().openRawResource(R.raw.punish))) {
                track.play();
                int count = 0;
                byte[] buffer = new byte[1024];
                while (isWorking && (count = bufferedInputStream.read(buffer)) != -1) {
                    track.write(buffer, 0, count);
                    if (!isWorking){
                        synchronized (this){
                            this.wait();
                        }
                    }

                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, "Audio Reader").start();
    }

    private void writeRecord(){
        AudioRecord.Builder builder = new AudioRecord.Builder();
        builder.setAudioFormat(new AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(44100)
        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
        .build());
        builder.setAudioSource(MediaRecorder.AudioSource.MIC);
        builder.setBufferSizeInBytes(44100);
        AudioRecord record = builder.build();
        record.startRecording();
        record.read(buffer, 0, buffer.length);
        record.release();
    }

    private void readRecord(){
        AudioTrack.Builder builder = new AudioTrack.Builder();
        builder.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        builder.setAudioFormat(new AudioFormat.Builder()
                .setSampleRate(44100)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build());
        builder.setBufferSizeInBytes(44100);
        AudioTrack track = builder.build();
        track.play();
        track.write(buffer, 0, buffer.length);
        track.release();
    }
}
