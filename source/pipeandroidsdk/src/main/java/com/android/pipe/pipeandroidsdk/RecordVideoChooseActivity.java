package com.android.pipe.pipeandroidsdk;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class RecordVideoChooseActivity extends Activity {

    private TextView button_retake;
    private TextView button_useVideo;
    private VideoView videoView;
    private PipeMediaController mediaController;
    private String payload = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(4);

        payload = getIntent().getStringExtra("payload");

        setContentView(R.layout.activity_record_video_choose);

        button_retake = (TextView) findViewById(R.id.button_chooseVideo_retake);
        button_useVideo = (TextView) findViewById(R.id.button_chooseVideo_UseVideo);

        button_retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        button_useVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        mediaController = new PipeMediaController(this);
        videoView = (VideoView) findViewById(R.id.videoView2);

        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        File file = new File(PipeRecorder.getVideoFilePath());

        videoView.setVideoURI(Uri.parse(file.getPath()));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.pause();
                mediaController.show();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(0);
                mediaController.show(0);
            }
        });

    }

    public void onResume() {
        super.onResume();

        payload = getIntent().getStringExtra("payload");
    }

    public void upload() {

        NetworkUtil nUtil = new NetworkUtil(getActivity());
        nUtil.upload(PipeRecorder.accountHash, payload,
                "", PipeRecorder.getVideoFilePath());

    }

    public Activity getActivity() {
        return this;
    }

}
