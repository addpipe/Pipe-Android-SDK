package com.android.pipe.pipeandroidsdk;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PipeRecorder {

    public int                  maxDuration = 5;
    public String               payload = "Test payload for recording custom";
    public boolean              showCameraControls = true;
    public static View          controlsLayout;
    public static OnVideoCaptureListener videoCaptureListener = null;
    public boolean              recordHD = false;
    protected static  boolean   recordHDValue = false;
    protected static String     accountHash = "";
    protected static String     videoFileName = "";

    private Context             mContext;
    protected static Activity            mActivity;
    protected static File     videoDir;
    public static OnUploadListener    mOnUploadedListener = null;

    public PipeRecorder(@NonNull final Context context, String accountHash) {
        this.mContext   = context;
        this.accountHash = accountHash;
        this.mActivity  = (Activity)(context);
        videoFileName = String.valueOf(new Date().getTime());

        boolean ret = false;
        videoDir = new File(Environment.getExternalStorageDirectory().getPath() + "/PipeSDKVideoDir");
        if (!videoDir.exists()) {
            ret = videoDir.mkdir();
        }

    }

    @NonNull
    protected static String getVideoFilePath() {
        File file;

        file = new File(videoDir, videoFileName + ".mp4");

        return  file.getPath();
    }

    public void useExistingVideo() {

        Intent intent = new Intent(mActivity, UseExistingVideoActivity.class);
        intent.putExtra("maxDuration", maxDuration);
        intent.putExtra("payload", payload);

        mActivity.startActivity(intent);

    }

    public void show() {

        recordHDValue = recordHD;

        if (showCameraControls) {
            Intent intent = new Intent(mActivity, RecordVideoActivity.class);
            intent.putExtra("maxDuration", maxDuration);
            intent.putExtra("payload", payload);

            mActivity.startActivity(intent);
        } else {
            Intent intent = new Intent(mActivity, RecordVideoCustomUIActivity.class);
            intent.putExtra("maxDuration", maxDuration);
            intent.putExtra("payload", payload);

            mActivity.startActivity(intent);
        }

    }

    public void hide() {
        Intent intent = new Intent(mActivity, mActivity.getClass());
        mActivity.startActivity(intent);
    }

    public void startVideoCapture() {
        videoCaptureListener.onStartVideoCapture();
    }

    public void stopVideoCapture() {
        videoCaptureListener.onStopVideoCapture();
    }

    //Listener
    public void setOnUploadedListener(@Nullable OnUploadListener listener) {
        if (listener != null)
            mOnUploadedListener = listener;
    }

    public interface OnUploadListener {
        void onUploadSucceed(String result);
        void onUploadFailed(String result);
    }

    public interface OnVideoCaptureListener {
        void onStartVideoCapture();
        void onStopVideoCapture();
    }

    public static void setOnVideoCaptureListener(@Nullable OnVideoCaptureListener listener) {
        if (listener != null)
            videoCaptureListener = listener;
    }

    public static int getAPIVersion() {
        return Build.VERSION.SDK_INT;
    }

}
