package com.android.pipe.pipeandroidsdk;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RecordVideoActivity extends Activity  {
	private static final String TAG = "RecordVideoActivity";
	private MainUI mainUI = null;
	private MyApplicationInterface applicationInterface = null;
	private Preview preview = null;
	private boolean supports_camera2 = false;
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable<Integer, Bitmap>();

	private ToastBoxer switch_camera_toast = new ToastBoxer();
	private ToastBoxer switch_video_toast = new ToastBoxer();
    private ToastBoxer changed_auto_stabilise_toast = new ToastBoxer();

	private boolean block_startup_toast = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record_video);

		maxDuration = getIntent().getIntExtra("maxDuration", 5);
		payload = getIntent().getStringExtra("payload");
		textView_duration = (TextView)findViewById(R.id.textview_recordVideo_duration);

		button_cancel = (Button)findViewById(R.id.button_recordVideo_cancel);
		button_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		button_record = (ImageButton)findViewById(R.id.take_photo);
		button_record.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (recordClicked == 2) {
					recordClicked = 1;
					timer.cancel();
					timer = null;
					gotoChooseActivity();
				} else if(recordClicked == 1) {
					recordClicked = 2;
					textView_duration.setText("00:00:00");
					duration = 0;
					timer = new Timer();
					timer.schedule(new RecordTask(), 0, 1000);
				}

				((RecordVideoActivity)getActivity()).takePicture();
			}
		});

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		mainUI = new MainUI(this);
		applicationInterface = new MyApplicationInterface(this, savedInstanceState);

		initCamera2Support();

        preview = new Preview(applicationInterface, savedInstanceState, ((ViewGroup) this.findViewById(R.id.preview)));

	    View switchCameraButton = (View) findViewById(R.id.switch_camera);
	    switchCameraButton.setVisibility(preview.getCameraControllerManager().getNumberOfCameras() > 1 ? View.VISIBLE : View.GONE);


        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            	if( !usingKitKatImmersiveMode() )
            		return;

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
            		mainUI.setImmersiveMode(false);
                	setImmersiveTimer();
                }
                else {
                    // The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
            		mainUI.setImmersiveMode(true);
                }
            }
        });

        preloadIcons(R.array.flash_icons);
        preloadIcons(R.array.focus_mode_icons);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void initCamera2Support() {

    	supports_camera2 = false;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
        	CameraControllerManager2 manager2 = new CameraControllerManager2(this);
        	supports_camera2 = true;
        	if( manager2.getNumberOfCameras() == 0 ) {
            	supports_camera2 = false;
        	}
        	for(int i=0;i<manager2.getNumberOfCameras() && supports_camera2;i++) {
        		if( !manager2.allowCamera2Support(i) ) {
                	supports_camera2 = false;
        		}
        	}
        }
	}
	
	private void preloadIcons(int icons_id) {
    	String [] icons = getResources().getStringArray(icons_id);
    	for(int i=0;i<icons.length;i++) {
    		int resource = getResources().getIdentifier(icons[i], null, this.getApplicationContext().getPackageName());

    		Bitmap bm = BitmapFactory.decodeResource(getResources(), resource);
    		this.preloaded_bitmap_resources.put(resource, bm);
    	}
	}
	
	@Override
	protected void onDestroy() {
		// Need to recycle to avoid out of memory when running tests - probably good practice to do anyway
		for(Map.Entry<Integer, Bitmap> entry : preloaded_bitmap_resources.entrySet()) {
			entry.getValue().recycle();
		}
		preloaded_bitmap_resources.clear();

	    super.onDestroy();
	}

	@Override
    protected void onResume() {
		textView_duration.setText("00:00:00");
		duration = 0;

        super.onResume();

        getWindow().getDecorView().getRootView().setBackgroundColor(Color.BLACK);

		preview.onResume();

    }
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		super.onWindowFocusChanged(hasFocus);

	}

    @Override
    protected void onPause() {
		if (timer != null) {
			timer.cancel();
		}

        super.onPause();
        mainUI.destroyPopup();

		preview.onPause();

    }

	private int					recordClicked = 1;
	private int                 maxDuration;
	private String              payload;
	private int                 duration;
	public 	Timer 				timer;
	private TextView 			textView_duration;
	private Button 				button_cancel;
	private ImageButton 		button_record;

	public Activity getActivity() {
		return  this;
	}

	public void gotoChooseActivity() {

		Intent intent = new Intent(this, RecordVideoChooseActivity.class);
		intent.putExtra("payload", payload);
		startActivity(intent);

	}

    public int getNextCameraId() {
		int cameraId = preview.getCameraId();

		if( this.preview.canSwitchCamera() ) {
			int n_cameras = preview.getCameraControllerManager().getNumberOfCameras();
			cameraId = (cameraId+1) % n_cameras;
		}

		return cameraId;
    }

	public boolean is_front_camera = false;

    public void clickedSwitchCamera(View view) {

		this.closePopup();
		if( this.preview.canSwitchCamera() ) {
			int cameraId = getNextCameraId();
		    if( preview.getCameraControllerManager().isFrontFacing( cameraId ) ) {
		    	preview.showToast(switch_camera_toast, R.string.front_camera);
				is_front_camera = true;
		    }
		    else {
		    	preview.showToast(switch_camera_toast, R.string.back_camera);
				is_front_camera = false;
		    }
		    View switchCameraButton = (View) findViewById(R.id.switch_camera);
		    switchCameraButton.setEnabled(false); // prevent slowdown if user repeatedly clicks
			this.preview.setCamera(cameraId);
		    switchCameraButton.setEnabled(true);
			mainUI.setSwitchCameraContentDescription();
		}
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)

    public boolean popupIsOpen() {
    	return mainUI.popupIsOpen();
    }
    
    public void closePopup() {
		textView_duration.setVisibility(View.VISIBLE);
    	mainUI.closePopup();
    }

    public Bitmap getPreloadedBitmap(int resource) {
		Bitmap bm = this.preloaded_bitmap_resources.get(resource);
		return bm;
    }

    public void clickedPopupSettings(View view) {
		if (is_front_camera)
			return;;

		textView_duration.setVisibility(View.GONE);
		mainUI.togglePopupSettings();
    }
    
    MyPreferenceFragment getPreferenceFragment() {
        MyPreferenceFragment fragment = (MyPreferenceFragment)getFragmentManager().findFragmentByTag("PREFERENCE_FRAGMENT");
        return fragment;
    }
    
    @Override
    public void onBackPressed() {
        final MyPreferenceFragment fragment = getPreferenceFragment();

        if( fragment == null ) {
			if( popupIsOpen() ) {
				closePopup();
				return;
			}
        }

        super.onBackPressed();        
    }
    
    public boolean usingKitKatImmersiveMode() {
    	// whether we are using a Kit Kat style immersive mode (either hiding GUI, or everything)
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    		String immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
    		if( immersive_mode.equals("immersive_mode_gui") || immersive_mode.equals("immersive_mode_everything") )
    			return true;
		}
		return false;
    }
    
    private Handler immersive_timer_handler = null;
    private Runnable immersive_timer_runnable = null;
    
    private void setImmersiveTimer() {
    	if( immersive_timer_handler != null && immersive_timer_runnable != null ) {
    		immersive_timer_handler.removeCallbacks(immersive_timer_runnable);
    	}
    	immersive_timer_handler = new Handler();
    	immersive_timer_handler.postDelayed(immersive_timer_runnable = new Runnable(){
    		@Override
    	    public void run(){
    			if(!popupIsOpen() && usingKitKatImmersiveMode() )
    				setImmersiveMode(true);
    	    }
    	}, 5000);
    }

    public void initImmersiveMode() {
        if( !usingKitKatImmersiveMode() ) {
			setImmersiveMode(true);
		}
        else {
        	setImmersiveTimer();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
	void setImmersiveMode(boolean on) {
    	if( on ) {
    		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && usingKitKatImmersiveMode() ) {
        		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    		}
    		else {
        		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        		String immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
        		if( immersive_mode.equals("immersive_mode_low_profile") )
        			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        		else
            		getWindow().getDecorView().setSystemUiVisibility(0);
    		}
    	}
    	else
    		getWindow().getDecorView().setSystemUiVisibility(0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

//        if( requestCode == 42 && resultCode == RESULT_OK && resultData != null ) {
//            Uri treeUri = resultData.getData();
//
//    		// from https://developer.android.com/guide/topics/providers/document-provider.html#permissions :
//    		final int takeFlags = resultData.getFlags()
//    	            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
//    	            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//	    	// Check for the freshest data.
//	    	getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
//			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//			SharedPreferences.Editor editor = sharedPreferences.edit();
//			editor.putString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), treeUri.toString());
//			editor.apply();
//			String filename = applicationInterface.getStorageUtils().getImageFolderNameSAF();
//			if( filename != null ) {
//				preview.showToast(null, getResources().getString(R.string.changed_save_location) + "\n" + filename);
//			}
//        }
//        else if( requestCode == 42 ) {
//
//        	// cancelled - if the user had yet to set a save location, make sure we switch SAF back off
//			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//    		String uri = sharedPreferences.getString(PreferenceKeys.getSaveLocationSAFPreferenceKey(), "");
//    		if( uri.length() == 0 ) {
//    			SharedPreferences.Editor editor = sharedPreferences.edit();
//    			editor.putBoolean(PreferenceKeys.getUsingSAFPreferenceKey(), false);
//    			editor.apply();
//    			preview.showToast(null, R.string.saf_cancelled);
//    		}
//        }
    }

    private void takePicture() {
		closePopup();
    	this.preview.takePicturePressed();
    }

	@Override
	protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);
	    if( this.preview != null ) {
	    	preview.onSaveInstanceState(state);
	    }

	    if( this.applicationInterface != null ) {
	    	applicationInterface.onSaveInstanceState(state);
	    }
	}

    void cameraSetup() {
//		if( this.supportsForceVideo4K() && preview.usingCamera2API() ) {
//			this.disableForceVideo4K();
//		}
//		if( this.supportsForceVideo4K() && preview.getSupportedVideoSizes() != null ) {
//			for(CameraController.Size size : preview.getSupportedVideoSizes()) {
//				if( size.width >= 3840 && size.height >= 2160 ) {
//					this.disableForceVideo4K();
//				}
//			}
//		}

	    mainUI.setPopupIcon(); // needed so that the icon is set right even if no flash mode is set when starting up camera (e.g., switching to front camera with no flash)

		mainUI.setTakePhotoIcon();
		mainUI.setSwitchCameraContentDescription();

		if( !block_startup_toast ) {
			this.showPhotoVideoToast(false);
		}
    }
    
//    public boolean supportsAutoStabilise() {
//    	return this.supports_auto_stabilise;
//    }
//
//    public boolean supportsForceVideo4K() {
//    	return this.supports_force_video_4k;
//    }

    public boolean supportsCamera2() {
    	return this.supports_camera2;
    }
    
//    void disableForceVideo4K() {
//    	this.supports_force_video_4k = false;
//    }

    public Preview getPreview() {
    	return this.preview;
    }
    
    public MainUI getMainUI() {
    	return this.mainUI;
    }
    
    public MyApplicationInterface getApplicationInterface() {
    	return this.applicationInterface;
    }
    
    public StorageUtils getStorageUtils() {
    	return this.applicationInterface.getStorageUtils();
    }

    public ToastBoxer getChangedAutoStabiliseToastBoxer() {
    	return changed_auto_stabilise_toast;
    }

	private void showPhotoVideoToast(boolean switch_video) {

		CameraController camera_controller = preview.getCameraController();
		if( camera_controller == null)
			return;
		String toast_string = "";
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean simple = true;
		if( preview.isVideo() ) {
			CamcorderProfile profile = preview.getCamcorderProfile();
			String bitrate_string = "";
			if( profile.videoBitRate >= 10000000 )
				bitrate_string = profile.videoBitRate/1000000 + "Mbps";
			else if( profile.videoBitRate >= 10000 )
				bitrate_string = profile.videoBitRate/1000 + "Kbps";
			else
				bitrate_string = profile.videoBitRate + "bps";

			toast_string = getResources().getString(R.string.video) + ": " + profile.videoFrameWidth + "x" + profile.videoFrameHeight + ", " + profile.videoFrameRate + "fps, " + bitrate_string;
			boolean record_audio = sharedPreferences.getBoolean(PreferenceKeys.getRecordAudioPreferenceKey(), true);
			if( !record_audio ) {
				toast_string += "\n" + getResources().getString(R.string.audio_disabled);
				simple = false;
			}
			String max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0");
			if( max_duration_value.length() > 0 && !max_duration_value.equals("0") ) {
				String [] entries_array = getResources().getStringArray(R.array.preference_video_max_duration_entries);
				String [] values_array = getResources().getStringArray(R.array.preference_video_max_duration_values);
				int index = Arrays.asList(values_array).indexOf(max_duration_value);
				if( index != -1 ) { // just in case!
					String entry = entries_array[index];
					toast_string += "\n" + getResources().getString(R.string.max_duration) +": " + entry;
					simple = false;
				}
			}

			long max_filesize = applicationInterface.getVideoMaxFileSizePref();
			if( max_filesize != 0 ) {
				long max_filesize_mb = max_filesize/(1024*1024);
				toast_string += "\n" + getResources().getString(R.string.max_filesize) +": " + max_filesize_mb + getResources().getString(R.string.mb_abbreviation);
				simple = false;
			}
			if( sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false) && preview.supportsFlash() ) {
				toast_string += "\n" + getResources().getString(R.string.preference_video_flash);
				simple = false;
			}
		}
		else {
			toast_string = getResources().getString(R.string.photo);
			CameraController.Size current_size = preview.getCurrentPictureSize();
			toast_string += " " + current_size.width + "x" + current_size.height;
		}

		String scene_mode = camera_controller.getSceneMode();
    	if( scene_mode != null && !scene_mode.equals(camera_controller.getDefaultSceneMode()) ) {
    		toast_string += "\n" + getResources().getString(R.string.scene_mode) + ": " + scene_mode;
			simple = false;
    	}
		String white_balance = camera_controller.getWhiteBalance();
    	if( white_balance != null && !white_balance.equals(camera_controller.getDefaultWhiteBalance()) ) {
    		toast_string += "\n" + getResources().getString(R.string.white_balance) + ": " + white_balance;
			simple = false;
    	}
		String color_effect = camera_controller.getColorEffect();
    	if( color_effect != null && !color_effect.equals(camera_controller.getDefaultColorEffect()) ) {
    		toast_string += "\n" + getResources().getString(R.string.color_effect) + ": " + color_effect;
			simple = false;
    	}
		String lock_orientation = sharedPreferences.getString(PreferenceKeys.getLockOrientationPreferenceKey(), "none");
		if( !lock_orientation.equals("none") ) {
			String [] entries_array = getResources().getStringArray(R.array.preference_lock_orientation_entries);
			String [] values_array = getResources().getStringArray(R.array.preference_lock_orientation_values);
			int index = Arrays.asList(values_array).indexOf(lock_orientation);
			if( index != -1 ) { // just in case!
				String entry = entries_array[index];
				toast_string += "\n" + entry;
				simple = false;
			}
		}
		String timer = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0");
		if( !timer.equals("0") ) {
			String [] entries_array = getResources().getStringArray(R.array.preference_timer_entries);
			String [] values_array = getResources().getStringArray(R.array.preference_timer_values);
			int index = Arrays.asList(values_array).indexOf(timer);
			if( index != -1 ) { // just in case!
				String entry = entries_array[index];
				toast_string += "\n" + getResources().getString(R.string.preference_timer) + ": " + entry;
				simple = false;
			}
		}
		String repeat = applicationInterface.getRepeatPref();
		if( !repeat.equals("1") ) {
			String [] entries_array = getResources().getStringArray(R.array.preference_burst_mode_entries);
			String [] values_array = getResources().getStringArray(R.array.preference_burst_mode_values);
			int index = Arrays.asList(values_array).indexOf(repeat);
			if( index != -1 ) { // just in case!
				String entry = entries_array[index];
				toast_string += "\n" + getResources().getString(R.string.preference_burst_mode) + ": " + entry;
				simple = false;
			}
		}

		if( !simple || switch_video )
			preview.showToast(switch_video_toast, toast_string);
	}

	class RecordTask extends TimerTask {

		@Override
		public void run() {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					if (duration > maxDuration) {

						duration = 0;
						timer.cancel();
						timer = null;

						recordClicked = 1;

						new AlertDialog.Builder(getActivity())
								.setTitle("Video Recording Stopped")
								.setMessage("The maximum length for this video has been reached")
								.setCancelable(false)
								.setPositiveButton("ok", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										textView_duration.setText("00:00:00");
										gotoChooseActivity();

										return;
									}
								}).create().show();

						preview.takePicturePressed();

					} else {
						if (duration == 0) {
							textView_duration.setText("00:00:00");
						} else {
							textView_duration.setText(String.format("%02d:%02d:%02d", 0, (duration) / 60, (duration) % 60));
						}
					}

					duration = duration + 1;

				}
			});
		}
	}

}
