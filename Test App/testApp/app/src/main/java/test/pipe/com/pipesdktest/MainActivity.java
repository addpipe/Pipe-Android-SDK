package test.pipe.com.pipesdktest;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.pipe.pipeandroidsdk.PipeRecorder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button      button_recordVideoCustomUI;
    private Button      button_recordVideo;
    private Button      button_useExistingVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_recordVideoCustomUI = (Button)findViewById(R.id.button_recordVideoCustomUI);
        button_recordVideo         = (Button)findViewById(R.id.button_recordVideo);
        button_useExistingVideo    = (Button)findViewById(R.id.button_useExistingVideo);

        button_recordVideoCustomUI.setOnClickListener(this);
        button_recordVideo.setOnClickListener(this);
        button_useExistingVideo.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);

            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        2);
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.RECORD_AUDIO}, 3);

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }

                return;
            }

            case 2: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Permission denied to use Camera", Toast.LENGTH_SHORT).show();
                }

                return;
            }

            case 3: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
                }

                return;
            }
        }
    }

    public void onClick(View v) {

        final PipeRecorder recorder = new PipeRecorder(this, "YOUR_PIPE_ACCOUNTHASH");

        recorder.setOnUploadedListener(new PipeRecorder.OnUploadListener() {
            @Override
            public void onUploadSucceed(String result) {
                Toast toast = Toast.makeText(getActivity(), "Success", Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onUploadFailed(String result) {
                Toast toast = Toast.makeText(getActivity(), "Network Connection Error", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        switch (v.getId()) {
            case R.id.button_recordVideoCustomUI:
                recorder.maxDuration = 5;
                recorder.recordHD = false;
                recorder.payload = "Test payload for recording with customUI";
                recorder.showCameraControls = false;

                LayoutInflater inflater;
                inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View controlLayout = (View)inflater.inflate(R.layout.control_layout, null);

                Button button_record = (Button)controlLayout.findViewById(R.id.button_record);
                Button button_stop = (Button)controlLayout.findViewById(R.id.button_stop);
                Button button_cancel = (Button)controlLayout.findViewById(R.id.button_cancel);

                button_record.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recorder.startVideoCapture();
                    }
                });

                button_stop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recorder.stopVideoCapture();
                    }
                });

                button_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recorder.hide();
                    }
                });

                recorder.controlsLayout = controlLayout;

                recorder.show();

                break;

            case R.id.button_recordVideo:
                recorder.maxDuration = 5;
                recorder.recordHD = false;
                recorder.payload = "Test payload for recording with normalUI";
                recorder.showCameraControls = true;

                recorder.show();

                break;
            case R.id.button_useExistingVideo:
                recorder.payload = "Test payload with existingVideo";
                recorder.useExistingVideo();

                break;
            default:
                break;
        }

    }

    public Activity getActivity() {
        return this;
    }

}

