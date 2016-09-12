# Pipe-Android-SDK

[Pipe](https://addpipe.com/) handles recording video from web and mobile, all the different file formats, ensures secure storage & delivery and has the JS and Webhook APIs needed for a seamless integration.

# SDK Version
MinSDKVersion:  API 17 (Jelly Bean)

MaxSDKVersion: API Level 24 (Android 6.X)

# How to setup
* Create an android project
* Add Pipe android SDK in your project
  1. File/New Module
  2. Select Import .Jar/.AAR package
  3. Fill File name field and Subproject name field by click import button

# Initializing the Pipe recorder

## Required Permissions

```
//AndroidManifest.xml
//… …
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
//… …
```

## Initialization with default options

```
import com.android.pipe.pipeandroidsdk.PipeRecorder;
PipeRecorder recorder = new PipeRecorder(this, “YOUR_PIPE_ACCOUNT_HASH”);
recorder.show();
```

Your Pipe Account Hash can be found in your pipe account setting page.
The above lines of code will initialize the Pipe recorder with the default options
* Default recording
* 5 seconds of maximum allowed recording
* Empty custom data payload
But all of these can be customized and more.

# Changing the options

All of the following options must be specified before show pipe recorder. i.e before this line:

```
recorder.show()
```

## Changing the default maximum recording duration

```
recorder.maxDuration = 60; //or any duration in seconds
```

## Changing the video quality

```
recorder.recordHD = true;
```

## Adding a data payload

```
recorder.payload = “My custom data payload”;
```

This payload corresponds to the payload that will be sent back via Webhooks.

## Customizing the UI

```
//MainActivity.java
Recorder.showsCameraControls = false;
LayoutInflater inflater;
Inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
View controlLayout = (View)inflater.inflate(R.layout.control_layout, null);
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
```

```
//control_layout.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:id = "@+id/controlsLayout"
android:orientation="horizontal"
android:gravity="center_vertical|center_horizontal">
  <Button
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:text="record"
  android:id="@+id/button_record"
  android:layout_weight="1" />
  <Button
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:text="stop"
  android:id="@+id/button_stop"
  android:layout_weight="1" />
  <Button
  android:layout_width="wrap_content"
  android:layout_height="wrap_content"
  android:text="cancel"
  android:id="@+id/button_cancel"
  android:layout_weight="1" />
</LinearLayout>
```

# Select an existing recording

If you wish to select an already made video instead of recording a new one simply add the following line of code after creating PipeRecorder instance: 

```
recorder.useExistingVideo();
```

# Listener

In order to get notified in your app of video uploading success or failure you need to use PipeRecorderListener like so:

```
public class MainActivity extends AppCompatActivity implements View.OnClickListener, PipeRecorder.OnUploadListener {
//... …
@Override
protected void onCreate(Bundle savedInstanceState) {
setContentView(R.layout.activity_main);
//…
button_recordVideo = (Button)findViewById(R.id.button_recordVideo);
button_recordVideo.setOnClickListener(this);
//…
}
@Override
  private void onClick(View v) {
  if (v.getId().equals(“R.id.button_recordVideo”)) {
  PipeRecorder recorder = new PipeRecorder(this, “your account Hash”);
  recorder.setOnUploadedListener(this);
  recorder.show();
  }
}
  @Override
  public void onUploadSucceed() {
  //…
  }
  @Override
  public void onUploadFailed() {
  //…
  }
}
```
