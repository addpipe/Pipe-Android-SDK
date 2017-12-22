package com.android.pipe.pipeandroidsdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.PipedReader;

public class NetworkUtil {

    private final String TAG = "NetworkUtil";
    public Context context;
    public ProgressDialog progressDialog;
    public String res;

    public NetworkUtil(Context context) {
        this.context = context;
    }

    public void upload(String accountHash, String payload, String capability, String filePath) {

        String deviceInfo = "Device:" + Build.MODEL + "," + " OS: Android "  + Build.VERSION.RELEASE;

        String boundaryString = "Boundary-" + Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        OkHttpClient client = new OkHttpClient();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast toast = Toast.makeText(getActivity(), "File not exist", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            String contentType = file.toURL().openConnection().getContentType();
            RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("accountHash", accountHash)
                    .addFormDataPart("payload", payload)
                    .addFormDataPart("capabilities", deviceInfo)
                    .addFormDataPart("FileInput", filePath, fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url("https://s3b.addpipe.com/upload")
                    .post(requestBody)
                    .addHeader("boundary", boundaryString)
                    .build();

            progressDialog = ProgressDialog.show(getActivity(), "", "Uploading...", true);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.d(TAG, "UploadingError");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (PipeRecorder.mOnUploadedListener != null) {
                                PipeRecorder.mOnUploadedListener.onUploadFailed(res);
                            }

                            gotoOriginalActivity();
                        }
                    });
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    Log.d(TAG, "UploadingSuccess");
                    res = response.body().string();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            if (PipeRecorder.mOnUploadedListener != null) {
                                PipeRecorder.mOnUploadedListener.onUploadSucceed(res);
                            }

                            gotoOriginalActivity();

                        }
                    });
                }
            });

        } catch (Exception e) {

        }

    }

    public Activity getActivity() {
        return (Activity)(context);
    }

    public void gotoOriginalActivity() {
        String originalActivityClassName = getActivity().getClass().toString();
        if (originalActivityClassName.equals("class com.android.pipe.pipeandroidsdk.RecordVideoCustomUIActivity"))
            return;

        if (originalActivityClassName.equals("class com.android.pipe.pipeandroidsdk.RecordVideoChooseActivity")) {
//            getActivity().finish();
            Intent intent = new Intent(getActivity(), PipeRecorder.mActivity.getClass());
            getActivity().startActivity(intent);
        }

        if (originalActivityClassName.equals("class com.android.pipe.pipeandroidsdk.UseExistingVideoActivity")) {
//            String payload = ((UseExistingVideoActivity)(getActivity())).getPayloadString();
//            Log.d("Payload", payload);
//            getActivity().finish();
//            Intent intent = new Intent(PipeRecorder.mActivity, RecordVideoActivity.class);
//            intent.putExtra("payload", payload);
//            PipeRecorder.mActivity.startActivity(intent);
            Intent intent = new Intent(getActivity(), PipeRecorder.mActivity.getClass());
            getActivity().startActivity(intent);
        }
    }
}
