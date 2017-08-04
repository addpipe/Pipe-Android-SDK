package com.android.pipe.pipeandroidsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MyApplicationInterfaceForCustomUI implements ApplicationInterface {

	private RecordVideoCustomUIActivity main_activity = null;
	private StorageUtils storageUtils = null;
	private DrawPreviewForCustomUI drawPreview = null;
	
	private int cameraId = 0;

	MyApplicationInterfaceForCustomUI(RecordVideoCustomUIActivity main_activity, Bundle savedInstanceState) {

		this.main_activity = main_activity;

		this.storageUtils = new StorageUtils(main_activity);

		this.drawPreview = new DrawPreviewForCustomUI(main_activity, this);

        if( savedInstanceState != null ) {
    		cameraId = savedInstanceState.getInt("cameraId", 0);
        }
	}

	void onSaveInstanceState(Bundle state) {
    	state.putInt("cameraId", cameraId);
	}

	StorageUtils getStorageUtils() {
		return storageUtils;
	}

    @Override
	public Context getContext() {
    	return main_activity;
    }

    @Override
	public boolean useCamera2() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if( main_activity.supportsCamera2() ) {
    		return sharedPreferences.getBoolean(PreferenceKeys.getUseCamera2PreferenceKey(), false);
        }
        return false;
    }

	@Override
	public int createOutputVideoMethod() {
        String action = main_activity.getIntent().getAction();
        if( MediaStore.ACTION_VIDEO_CAPTURE.equals(action) ) {
	        Bundle myExtras = main_activity.getIntent().getExtras();
	        if (myExtras != null) {
	        	Uri intent_uri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
	        	if( intent_uri != null ) {
	        		return VIDEOMETHOD_URI;
	        	}
	        }

			return VIDEOMETHOD_FILE;
        }
        boolean using_saf = storageUtils.isUsingSAF();
		return using_saf ? VIDEOMETHOD_SAF : VIDEOMETHOD_FILE;
	}

	@Override
	public File createOutputVideoFile() throws IOException {
		return storageUtils.createOutputMediaFile(StorageUtils.MEDIA_TYPE_VIDEO, "mp4");
	}

	@Override
	public Uri createOutputVideoSAF() throws IOException {
		return storageUtils.createOutputMediaFileSAF(StorageUtils.MEDIA_TYPE_VIDEO, "mp4");
	}

	@Override
	public Uri createOutputVideoUri() throws IOException {
        String action = main_activity.getIntent().getAction();
        if( MediaStore.ACTION_VIDEO_CAPTURE.equals(action) ) {

	        Bundle myExtras = main_activity.getIntent().getExtras();
	        if (myExtras != null) {
	        	Uri intent_uri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
	        	if( intent_uri != null ) {

	    			return intent_uri;
	        	}
	        }
        }
        throw new RuntimeException();
	}

	@Override
	public int getCameraIdPref() {
		return cameraId;
	}
	
    @Override
	public String getFlashPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getFlashPreferenceKey(cameraId), "");
    }

    @Override
	public boolean isVideoPref() {
        String action = main_activity.getIntent().getAction();
        if( MediaStore.INTENT_ACTION_VIDEO_CAMERA.equals(action) || MediaStore.ACTION_VIDEO_CAPTURE.equals(action) ) {
    		return true;
		}
        else if( MediaStore.ACTION_IMAGE_CAPTURE.equals(action) || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(action) || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action) || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action) ) {
    		return false;
		}
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(PreferenceKeys.getIsVideoPreferenceKey(), true);
    }

    @Override
	public String getSceneModePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String value = sharedPreferences.getString(PreferenceKeys.getSceneModePreferenceKey(), "auto");
		return value;
    }

    @Override
	public Pair<Integer, Integer> getCameraResolutionPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String resolution_value = sharedPreferences.getString(PreferenceKeys.getResolutionPreferenceKey(cameraId), "");

		if( resolution_value.length() > 0 ) {
			int index = resolution_value.indexOf(' ');
			if( index == -1 ) {

			}
			else {
				String resolution_w_s = resolution_value.substring(0, index);
				String resolution_h_s = resolution_value.substring(index+1);
				try {
					int resolution_w = Integer.parseInt(resolution_w_s);
					int resolution_h = Integer.parseInt(resolution_h_s);
					return new Pair<Integer, Integer>(resolution_w, resolution_h);
				}
				catch(NumberFormatException exception) {

				}
			}
		}
		return null;
    }
    
	@Override
	public String getVideoQualityPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), "");
	}
	
    @Override
	public boolean getVideoStabilizationPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getBoolean(PreferenceKeys.getVideoStabilizationPreferenceKey(), false);
    }
    
    @Override
	public boolean getForce4KPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		if( cameraId == 0 && sharedPreferences.getBoolean(PreferenceKeys.getForceVideo4KPreferenceKey(), false)) {
			return true;
		}
		return false;
    }
    
    @Override
    public String getVideoBitratePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getString(PreferenceKeys.getVideoBitratePreferenceKey(), "default");
    }

    @Override
    public String getVideoFPSPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getString(PreferenceKeys.getVideoFPSPreferenceKey(), "default");
    }
    
    @Override
    public long getVideoMaxDurationPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String video_max_duration_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxDurationPreferenceKey(), "0");
		long video_max_duration = 0;
		try {
			video_max_duration = (long)Integer.parseInt(video_max_duration_value) * 1000;
		}
        catch(NumberFormatException e) {
    		e.printStackTrace();
    		video_max_duration = 0;
        }
		return video_max_duration;
    }

    @Override
    public int getVideoRestartTimesPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String restart_value = sharedPreferences.getString(PreferenceKeys.getVideoRestartPreferenceKey(), "0");
		int remaining_restart_video = 0;
		try {
			remaining_restart_video = Integer.parseInt(restart_value);
		}
        catch(NumberFormatException e) {
    		e.printStackTrace();
    		remaining_restart_video = 0;
        }
		return remaining_restart_video;
    }

    @Override
	public long getVideoMaxFileSizePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String video_max_filesize_value = sharedPreferences.getString(PreferenceKeys.getVideoMaxFileSizePreferenceKey(), "0");
		long video_max_filesize = 0;
		try {
			video_max_filesize = Integer.parseInt(video_max_filesize_value);
		}
        catch(NumberFormatException e) {
    		e.printStackTrace();
    		video_max_filesize = 0;
        }
		return video_max_filesize;
	}

    @Override
	public boolean getVideoRestartMaxFileSizePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getBoolean(PreferenceKeys.getVideoRestartMaxFileSizePreferenceKey(), true);
	}

    @Override
    public boolean getVideoFlashPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getBoolean(PreferenceKeys.getVideoFlashPreferenceKey(), false);
    }
    
    @Override
	public String getPreviewSizePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getPreviewSizePreferenceKey(), "preference_preview_size_wysiwyg");
    }

    @Override
	public boolean getShowToastsPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getBoolean(PreferenceKeys.getShowToastsPreferenceKey(), true);
    }

    @Override
	public boolean getStartupFocusPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getBoolean(PreferenceKeys.getStartupFocusPreferenceKey(), true);
    }

    @Override
    public long getTimerPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		String timer_value = sharedPreferences.getString(PreferenceKeys.getTimerPreferenceKey(), "0");
		long timer_delay = 0;
		try {
			timer_delay = (long)Integer.parseInt(timer_value) * 1000;
		}
        catch(NumberFormatException e) {

    		e.printStackTrace();
    		timer_delay = 0;
        }
		return timer_delay;
    }
    
    @Override
    public String getRepeatPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getString(PreferenceKeys.getBurstModePreferenceKey(), "1");
    }

    @Override
    public long getExposureTimePref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getLong(PreferenceKeys.getExposureTimePreferenceKey(), CameraController.EXPOSURE_TIME_DEFAULT);
    }

	@Override
	public void stoppedVideo(final int video_method, final Uri uri, final String filename) {
		boolean done = false;
		if( video_method == VIDEOMETHOD_FILE ) {
			if( filename != null ) {
				File file = new File(filename);
				storageUtils.broadcastFile(file, false, true, true);
				done = true;
			}
		}
		else {
			if( uri != null ) {

	    	    File real_file = storageUtils.getFileFromDocumentUriSAF(uri);
                if( real_file != null ) {
	            	storageUtils.broadcastFile(real_file, false, true, true);
                }
                else {
	    		    storageUtils.announceUri(uri, false, true);
                }
			    done = true;
			}
		}

		String action = main_activity.getIntent().getAction();
        if( MediaStore.ACTION_VIDEO_CAPTURE.equals(action) ) {
    		if( done && video_method == VIDEOMETHOD_FILE ) {
    			// do nothing here - we end the activity from storageUtils.broadcastFile after the file has been scanned, as it seems caller apps seem to prefer the content:// Uri rather than one based on a File
    		}
    		else {
    			Intent output = null;
    			if( done ) {
    				if( video_method == VIDEOMETHOD_SAF ) {
    					output = new Intent();
    					output.setData(uri);
    				}
    			}
            	main_activity.setResult(done ? Activity.RESULT_OK : Activity.RESULT_CANCELED, output);
            	main_activity.finish();
    		}
        }
        else if( done ) {
		    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			try {
				if( video_method == VIDEOMETHOD_FILE ) {
					File file = new File(filename);
					retriever.setDataSource(file.getPath());
				}
				else {
					ParcelFileDescriptor pfd_saf = getContext().getContentResolver().openFileDescriptor(uri, "r");
					retriever.setDataSource(pfd_saf.getFileDescriptor());
				}
			}
		    catch(FileNotFoundException e) {
		    	e.printStackTrace();
		    }
		    catch(IllegalArgumentException e) {
		    	e.printStackTrace();
		    }
		    catch(RuntimeException e) {
		    	e.printStackTrace();
		    }
		    finally {
		    	try {
		    		retriever.release();
		    	}
		    	catch(RuntimeException ex) {
		    	}
		    }
		}
	}

	@Override
	public void cameraSetup() {
		main_activity.cameraSetup();
		drawPreview.clearContinuousFocusMove();
	}

	@Override
	public void onContinuousFocusMove(boolean start) {

		drawPreview.onContinuousFocusMove(start);
	}

	@Override
	public void touchEvent(MotionEvent event) {

		main_activity.getMainUIForCustomUI().closePopup();
		if( main_activity.usingKitKatImmersiveMode() ) {
			main_activity.setImmersiveMode(false);
		}
	}
	
	@Override
	public void startingVideo() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		if( sharedPreferences.getBoolean(PreferenceKeys.getLockVideoPreferenceKey(), false) ) {

		}
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo_ForCustomUI);
		view.setImageResource(R.drawable.take_video_recording);
		view.setContentDescription( getContext().getResources().getString(R.string.stop_video) );
		view.setTag(R.drawable.take_video_recording); // for testing
	}

	@Override
	public void stoppingVideo() {

		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo_ForCustomUI);
		view.setImageResource(R.drawable.take_video_selector);
		view.setContentDescription( getContext().getResources().getString(R.string.start_video) );
		view.setTag(R.drawable.take_video_selector); // for testing
	}

	@Override
	public void onVideoInfo(int what, int extra) {
		if( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED ) {
			int message_id = 0;
			if( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ) {
				message_id = R.string.video_max_duration;
			}
			else {
				message_id = R.string.video_max_filesize;
			}
			if( message_id != 0 )
				main_activity.getPreview().showToast(null, message_id);
			String debug_value = "info_" + what + "_" + extra;
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("last_video_error", debug_value);
			editor.apply();
		}
	}

	@Override
	public void onFailedStartPreview() {
		main_activity.getPreview().showToast(null, R.string.failed_to_start_camera_preview);
	}

	@Override
	public void onVideoError(int what, int extra) {
		int message_id = R.string.video_error_unknown;
		if( what == MediaRecorder.MEDIA_ERROR_SERVER_DIED  ) {

			message_id = R.string.video_error_server_died;
		}
		main_activity.getPreview().showToast(null, message_id);
		String debug_value = "error_" + what + "_" + extra;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("last_video_error", debug_value);
		editor.apply();
	}
	
	@Override
	public void onVideoRecordStartError(CamcorderProfile profile) {

		String error_message = "";
		String features = main_activity.getPreview().getErrorFeatures(profile);
		if( features.length() > 0 ) {
			error_message = getContext().getResources().getString(R.string.sorry) + ", " + features + " " + getContext().getResources().getString(R.string.not_supported);
		}
		else {
			error_message = getContext().getResources().getString(R.string.failed_to_record_video);
		}
		main_activity.getPreview().showToast(null, error_message);
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo_ForCustomUI);
		view.setImageResource(R.drawable.take_video_selector);
		view.setContentDescription(getContext().getResources().getString(R.string.start_video));
		view.setTag(R.drawable.take_video_selector); // for testing
	}

	@Override
	public void onVideoRecordStopError(CamcorderProfile profile) {
		return;
	}
	
	@Override
	public void onFailedReconnectError() {
		main_activity.getPreview().showToast(null, R.string.failed_to_reconnect_camera);
	}
	
	@Override
	public void onFailedCreateVideoFileError() {
		main_activity.getPreview().showToast(null, R.string.failed_to_save_video);
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo_ForCustomUI);
		view.setImageResource(R.drawable.take_video_selector);
		view.setContentDescription( getContext().getResources().getString(R.string.start_video) );
		view.setTag(R.drawable.take_video_selector); // for testing
	}
	
    @Override
    public void cameraInOperation(boolean in_operation) {
    	drawPreview.cameraInOperation(in_operation);
    	main_activity.getMainUIForCustomUI().showGUI(!in_operation);
    }

	@Override
	public void cameraClosed() {
		main_activity.getMainUIForCustomUI().destroyPopup(); // need to close popup - and when camera reopened, it may have different settings
		drawPreview.clearContinuousFocusMove();
	}

	@Override
	public void setCameraIdPref(int cameraId) {
		this.cameraId = cameraId;
	}

    @Override
    public void setFlashPref(String flash_value) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getFlashPreferenceKey(cameraId), flash_value);
		editor.apply();
    }

    @Override
	public void setVideoPref(boolean is_video) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(PreferenceKeys.getIsVideoPreferenceKey(), is_video);
		editor.apply();
    }

    @Override
    public void setSceneModePref(String scene_mode) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getSceneModePreferenceKey(), scene_mode);
		editor.apply();
    }
    
    @Override
	public void clearSceneModePref() {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(PreferenceKeys.getSceneModePreferenceKey());
		editor.apply();
    }
	
    @Override
	public void setCameraResolutionPref(int width, int height) {
		String resolution_value = width + " " + height;

    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getResolutionPreferenceKey(cameraId), resolution_value);
		editor.apply();
    }
    
    @Override
    public void setVideoQualityPref(String video_quality) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getVideoQualityPreferenceKey(cameraId), video_quality);
		editor.apply();
    }

    private int getStampFontColor() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String color = sharedPreferences.getString(PreferenceKeys.getStampFontColorPreferenceKey(), "#ffffff");
		return Color.parseColor(color);
    }

    @Override
    public void onDrawPreview(Canvas canvas) {
    	drawPreview.onDrawPreview(canvas);
    }

	@Override
	public String getColorEffectPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getColorEffectPreferenceKey(), "none");
	}

	@Override
	public void setColorEffectPref(String color_effect) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getColorEffectPreferenceKey(), color_effect);
		editor.apply();
	}

	@Override
	public String getFocusPref(boolean is_video) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), "");
	}

	@Override
	public void setFocusPref(String focus_value, boolean is_video) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PreferenceKeys.getFocusPreferenceKey(cameraId, is_video), focus_value);
		editor.apply();

	}

	@Override
	public String getPreviewRotationPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		return sharedPreferences.getString(PreferenceKeys.getRotatePreviewPreferenceKey(), "0");
	}

}
