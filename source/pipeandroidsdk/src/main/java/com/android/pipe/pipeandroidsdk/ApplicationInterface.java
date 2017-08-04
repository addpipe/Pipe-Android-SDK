package com.android.pipe.pipeandroidsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.hardware.camera2.DngCreator;
import android.location.Location;
import android.media.CamcorderProfile;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;

public interface ApplicationInterface {
	final int VIDEOMETHOD_FILE = 0; // video will be saved to a file
	final int VIDEOMETHOD_SAF = 1; // video will be saved using Android 5's Storage Access Framework
	final int VIDEOMETHOD_URI = 2; // video will be written to the supplied Uri
	
	Context getContext(); // get the application context
	boolean useCamera2(); // should Android 5's Camera 2 API be used?
	int createOutputVideoMethod(); // return a VIDEOMETHOD_* value to specify how to create a video file
	File createOutputVideoFile() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_FILE
	Uri createOutputVideoSAF() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_SAF
	Uri createOutputVideoUri() throws IOException; // will be called if createOutputVideoUsingSAF() returns VIDEOMETHOD_URI
	int getCameraIdPref(); // camera to use, from 0 to getCameraControllerManager().getNumberOfCameras()
	String getFlashPref(); // flash_off, flash_auto, flash_on, flash_torch, flash_red_eye
	boolean isVideoPref(); // start up in video mode?
	String getSceneModePref(); // "auto" for default (strings correspond to Android's scene mode constants in android.hardware.Camera.Parameters)
	Pair<Integer, Integer> getCameraResolutionPref(); // return null to let Preview choose size
	String getVideoQualityPref(); // should be one of Preview.getSupportedVideoQuality() (use Preview.getCamcorderProfile() or Preview.getCamcorderProfileDescription() for details); or return "" to let Preview choose quality
	boolean getVideoStabilizationPref(); // whether to use video stabilization for video
	boolean getForce4KPref(); // whether to force 4K mode - experimental, only really available for some devices that allow 4K recording but don't return it as an available resolution - not recommended for most uses
	String getVideoBitratePref(); // return "default" to let Preview choose
	String getVideoFPSPref(); // return "default" to let Preview choose
	long getVideoMaxDurationPref(); // time in ms after which to automatically stop video recording (return 0 for off)
	int getVideoRestartTimesPref(); // number of times to restart video recording after hitting max duration (return 0 for never auto-restarting)
	long getVideoMaxFileSizePref(); // maximum file size in bytes for video (return 0 for device default)
	boolean getVideoRestartMaxFileSizePref(); // whether to restart on hitting max file size
	boolean getVideoFlashPref(); // option to switch flash on/off while recording video (should be false in most cases!)
	String getPreviewSizePref(); // "preference_preview_size_wysiwyg" is recommended (preview matches aspect ratio of photo resolution as close as possible), but can also be "preference_preview_size_display" to maximise the preview size
	boolean getShowToastsPref();
	boolean getStartupFocusPref(); // whether to do autofocus on startup
	long getTimerPref(); // time in ms for timer (so 0 for off)
	String getRepeatPref(); // return number of times to repeat photo in a row (as a string), so "1" for default; return "unlimited" for unlimited

	long getExposureTimePref(); // only called if getISOPref() is not "default"
	void cameraSetup(); // called when the camera is (re-)set up - should update UI elements/parameters that depend on camera settings
	void touchEvent(MotionEvent event);
	void startingVideo(); // called just before video recording starts
	void stoppingVideo(); // called just before video recording stops
	void stoppedVideo(final int video_method, final Uri uri, final String filename); // called after video recording stopped (uri/filename will be null if video is corrupt or not created)
	void onFailedStartPreview(); // called if failed to start camera preview
	void onVideoInfo(int what, int extra); // callback for info when recording video (see MediaRecorder.OnInfoListener)
	void onVideoError(int what, int extra); // callback for errors when recording video (see MediaRecorder.OnErrorListener)
	void onVideoRecordStartError(CamcorderProfile profile); // callback for video recording failing to start
	void onVideoRecordStopError(CamcorderProfile profile); // callback for video recording being corrupted
	void onFailedReconnectError(); // failed to reconnect camera after stopping video recording
	void onFailedCreateVideoFileError(); // callback if unable to create file for recording video
	void cameraInOperation(boolean in_operation); // called when the camera starts/stops being operation (taking photos or recording video), use to disable GUI elements during camera operation
	void cameraClosed();


	void setCameraIdPref(int cameraId);
	void setFlashPref(String flash_value);
	void setVideoPref(boolean is_video);
	void setSceneModePref(String scene_mode);
	void clearSceneModePref();

	void setCameraResolutionPref(int width, int height);
	void setVideoQualityPref(String video_quality);

	void onDrawPreview(Canvas canvas);
	void onContinuousFocusMove(boolean start); // called when focusing starts/stop in continuous picture mode (in photo mode only)

	String getColorEffectPref();
	void setColorEffectPref(String color_effect);
	String getFocusPref(boolean is_video);
	void setFocusPref(String focus_value, boolean is_video);
	String getPreviewRotationPref(); // return "0" for default; use "180" to rotate the preview 180 degrees


}
