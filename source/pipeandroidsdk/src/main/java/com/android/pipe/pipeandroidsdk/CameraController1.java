package com.android.pipe.pipeandroidsdk;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("deprecation")
public class CameraController1 extends CameraController {

	private Camera camera = null;
    private int display_orientation = 0;
    private Camera.CameraInfo camera_info = new Camera.CameraInfo();
	private String iso_key = null;

	public CameraController1(int cameraId) throws CameraControllerException {
		super(cameraId);

		try {
			camera = Camera.open(cameraId);
		}
		catch(RuntimeException e) {

			e.printStackTrace();
			throw new CameraControllerException();
		}
		if( camera == null ) {
			throw new CameraControllerException();
		}
		try {
			Camera.getCameraInfo(cameraId, camera_info);
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			this.release();
			throw new CameraControllerException();
		}
		
		camera.setErrorCallback(new CameraErrorCallback());
	}
	
	private static class CameraErrorCallback implements Camera.ErrorCallback {
		@Override
		public void onError(int error, Camera camera) {

		}
	}

	public void release() {
		camera.release();
		camera = null;
	}

	public Camera getCamera() {
		return camera;
	}

	private Camera.Parameters getParameters() {
		return camera.getParameters();
	}

	private void setCameraParameters(Camera.Parameters parameters) {

	    try {
			camera.setParameters(parameters);

	    }
	    catch(RuntimeException e) {


    		e.printStackTrace();
    		count_camera_parameters_exception++;
	    }
	}

	private List<String> convertFlashModesToValues(List<String> supported_flash_modes) {

		List<String> output_modes = new Vector<String>();
		if( supported_flash_modes != null ) {
			// also resort as well as converting
			if( supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_OFF) ) {
				output_modes.add("flash_off");

			}
			if( supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_AUTO) ) {
				output_modes.add("flash_auto");

			}
			if( supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_ON) ) {
				output_modes.add("flash_on");

			}
			if( supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_TORCH) ) {
				output_modes.add("flash_torch");

			}
			if( supported_flash_modes.contains(Camera.Parameters.FLASH_MODE_RED_EYE) ) {
				output_modes.add("flash_red_eye");

			}
		}
		return output_modes;
	}

	private List<String> convertFocusModesToValues(List<String> supported_focus_modes) {

		List<String> output_modes = new Vector<String>();
		if( supported_focus_modes != null ) {
			// also resort as well as converting
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ) {
				output_modes.add("focus_mode_auto");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY) ) {
				output_modes.add("focus_mode_infinity");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_MACRO) ) {
				output_modes.add("focus_mode_macro");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_AUTO) ) {
				output_modes.add("focus_mode_locked");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_FIXED) ) {
				output_modes.add("focus_mode_fixed");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_EDOF) ) {
				output_modes.add("focus_mode_edof");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ) {
				output_modes.add("focus_mode_continuous_picture");

			}
			if( supported_focus_modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ) {
				output_modes.add("focus_mode_continuous_video");

			}
		}
		return output_modes;
	}

	public String getAPI() {
		return "Camera";
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public CameraFeatures getCameraFeatures() {

	    Camera.Parameters parameters = this.getParameters();
	    CameraFeatures camera_features = new CameraFeatures();
		camera_features.is_zoom_supported = parameters.isZoomSupported();
		if( camera_features.is_zoom_supported ) {
			camera_features.max_zoom = parameters.getMaxZoom();
			try {
				camera_features.zoom_ratios = parameters.getZoomRatios();
			}
			catch(NumberFormatException e) {
        		e.printStackTrace();
				camera_features.is_zoom_supported = false;
				camera_features.max_zoom = 0;
				camera_features.zoom_ratios = null;
			}
		}

		camera_features.supports_face_detection = parameters.getMaxNumDetectedFaces() > 0;

		// get available sizes
		List<Camera.Size> camera_picture_sizes = parameters.getSupportedPictureSizes();
		camera_features.picture_sizes = new ArrayList<Size>();
		//camera_features.picture_sizes.add(new CameraController.Size(1920, 1080)); // test
		for(Camera.Size camera_size : camera_picture_sizes) {
			camera_features.picture_sizes.add(new CameraController.Size(camera_size.width, camera_size.height));
		}

        //camera_features.supported_flash_modes = parameters.getSupportedFlashModes(); // Android format
        List<String> supported_flash_modes = parameters.getSupportedFlashModes(); // Android format
		camera_features.supported_flash_values = convertFlashModesToValues(supported_flash_modes); // convert to our format (also resorts)

        List<String> supported_focus_modes = parameters.getSupportedFocusModes(); // Android format
		camera_features.supported_focus_values = convertFocusModesToValues(supported_focus_modes); // convert to our format (also resorts)
		camera_features.max_num_focus_areas = parameters.getMaxNumFocusAreas();

        camera_features.is_exposure_lock_supported = parameters.isAutoExposureLockSupported();

        camera_features.is_video_stabilization_supported = parameters.isVideoStabilizationSupported();

        camera_features.min_exposure = parameters.getMinExposureCompensation();
        camera_features.max_exposure = parameters.getMaxExposureCompensation();
        try {
        	camera_features.exposure_step = parameters.getExposureCompensationStep();
        }
        catch(Exception e) {
        	// received a NullPointerException from StringToReal.parseFloat() beneath getExposureCompensationStep() on Google Play!

        	e.printStackTrace();
        	camera_features.exposure_step = 1.0f/3.0f; // make up a typical example
        }

		List<Camera.Size> camera_video_sizes = parameters.getSupportedVideoSizes();
    	if( camera_video_sizes == null ) {
    		// if null, we should use the preview sizes - see http://stackoverflow.com/questions/14263521/android-getsupportedvideosizes-allways-returns-null

    		camera_video_sizes = parameters.getSupportedPreviewSizes();
    	}
		camera_features.video_sizes = new ArrayList<Size>();
		//camera_features.video_sizes.add(new CameraController.Size(1920, 1080)); // test
		for(Camera.Size camera_size : camera_video_sizes) {
			camera_features.video_sizes.add(new CameraController.Size(camera_size.width, camera_size.height));
		}

		List<Camera.Size> camera_preview_sizes = parameters.getSupportedPreviewSizes();
		camera_features.preview_sizes = new ArrayList<Size>();
		for(Camera.Size camera_size : camera_preview_sizes) {
			camera_features.preview_sizes.add(new CameraController.Size(camera_size.width, camera_size.height));
		}

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
        	// Camera.canDisableShutterSound requires JELLY_BEAN_MR1 or greater
        	camera_features.can_disable_shutter_sound = camera_info.canDisableShutterSound;
        }
        else {
        	camera_features.can_disable_shutter_sound = false;
        }

		return camera_features;
	}

	public long getDefaultExposureTime() {
		// not supported for CameraController1
		return 0l;
	}

	public SupportedValues setSceneMode(String value) {
		String default_value = getDefaultSceneMode();
    	Camera.Parameters parameters = this.getParameters();
		List<String> values = parameters.getSupportedSceneModes();

		SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
		if( supported_values != null ) {
			if( !parameters.getSceneMode().equals(supported_values.selected_value) ) {
	        	parameters.setSceneMode(supported_values.selected_value);
	        	setCameraParameters(parameters);
			}
		}
		return supported_values;
	}

	public String getSceneMode() {
    	Camera.Parameters parameters = this.getParameters();
    	return parameters.getSceneMode();
	}

	public SupportedValues setColorEffect(String value) {
		String default_value = getDefaultColorEffect();
    	Camera.Parameters parameters = this.getParameters();
		List<String> values = parameters.getSupportedColorEffects();
		SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
		if( supported_values != null ) {
			if( !parameters.getColorEffect().equals(supported_values.selected_value) ) {
	        	parameters.setColorEffect(supported_values.selected_value);
	        	setCameraParameters(parameters);
			}
		}
		return supported_values;
	}

	public String getColorEffect() {
    	Camera.Parameters parameters = this.getParameters();
    	return parameters.getColorEffect();
	}

	public SupportedValues setWhiteBalance(String value) {
		String default_value = getDefaultWhiteBalance();
    	Camera.Parameters parameters = this.getParameters();
		List<String> values = parameters.getSupportedWhiteBalance();
		SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
		if( supported_values != null ) {
			if( !parameters.getWhiteBalance().equals(supported_values.selected_value) ) {
	        	parameters.setWhiteBalance(supported_values.selected_value);
	        	setCameraParameters(parameters);
			}
		}
		return supported_values;
	}

	public String getWhiteBalance() {
    	Camera.Parameters parameters = this.getParameters();
    	return parameters.getWhiteBalance();
	}

	@Override
	public SupportedValues setISO(String value) {
		String default_value = getDefaultISO();
    	Camera.Parameters parameters = this.getParameters();
		// get available isos - no standard value for this, see http://stackoverflow.com/questions/2978095/android-camera-api-iso-setting
		String iso_values = parameters.get("iso-values");
		if( iso_values == null ) {
			iso_values = parameters.get("iso-mode-values"); // Galaxy Nexus
			if( iso_values == null ) {
				iso_values = parameters.get("iso-speed-values"); // Micromax A101
				if( iso_values == null )
					iso_values = parameters.get("nv-picture-iso-values"); // LG dual P990
			}
		}
		List<String> values = null;
		if( iso_values != null && iso_values.length() > 0 ) {

			String [] isos_array = iso_values.split(",");
			// split shouldn't return null
			if( isos_array.length > 0 ) {
				values = new ArrayList<String>();
				for(int i=0;i< isos_array.length;i++) {
					values.add(isos_array[i]);
				}
			}
		}

		iso_key = "iso";
		if( parameters.get(iso_key) == null ) {
			iso_key = "iso-speed"; // Micromax A101
			if( parameters.get(iso_key) == null ) {
				iso_key = "nv-picture-iso"; // LG dual P990
				if( parameters.get(iso_key) == null )
					iso_key = null; // not supported
			}
		}

		if( iso_key != null ){
			if( values == null ) {

				values = new ArrayList<String>();
				values.add("auto");
				values.add("100");
				values.add("200");
				values.add("400");
				values.add("800");
				values.add("1600");
			}
			SupportedValues supported_values = checkModeIsSupported(values, value, default_value);
			if( supported_values != null ) {

	        	parameters.set(iso_key, supported_values.selected_value);
	        	setCameraParameters(parameters);
			}
			return supported_values;
		}
		return null;
	}

	@Override
	public String getISOKey() {

    	return this.iso_key;
    }

	@Override
	public int getISO() {
		// not supported for CameraController1
		return 0;
	}

	@Override
	public boolean setISO(int iso) {
		// not supported for CameraController1
		return false;
	}

	@Override
	public long getExposureTime() {
		// not supported for CameraController1
		return 0l;
	}

	@Override
	public boolean setExposureTime(long exposure_time) {
		// not supported for CameraController1
		return false;
	}

	@Override
    public CameraController.Size getPictureSize() {
    	Camera.Parameters parameters = this.getParameters();
    	Camera.Size camera_size = parameters.getPictureSize();
    	return new CameraController.Size(camera_size.width, camera_size.height);
    }

	@Override
	public void setPictureSize(int width, int height) {
    	Camera.Parameters parameters = this.getParameters();
		parameters.setPictureSize(width, height);

    	setCameraParameters(parameters);
	}

	@Override
    public CameraController.Size getPreviewSize() {
    	Camera.Parameters parameters = this.getParameters();
    	Camera.Size camera_size = parameters.getPreviewSize();
    	return new CameraController.Size(camera_size.width, camera_size.height);
    }

	public void setPreviewSize(int width, int height) {
    	Camera.Parameters parameters = this.getParameters();
        parameters.setPreviewSize(width, height);
    	setCameraParameters(parameters);
    }

	public void setVideoStabilization(boolean enabled) {
	    Camera.Parameters parameters = this.getParameters();
        parameters.setVideoStabilization(enabled);
    	setCameraParameters(parameters);
	}

	public boolean getVideoStabilization() {
	    Camera.Parameters parameters = this.getParameters();
        return parameters.getVideoStabilization();
	}

	public int getJpegQuality() {
	    Camera.Parameters parameters = this.getParameters();
	    return parameters.getJpegQuality();
	}

	public void setJpegQuality(int quality) {
	    Camera.Parameters parameters = this.getParameters();
		parameters.setJpegQuality(quality);
    	setCameraParameters(parameters);
	}

	public int getZoom() {
		Camera.Parameters parameters = this.getParameters();
		return parameters.getZoom();
	}

	public void setZoom(int value) {
		Camera.Parameters parameters = this.getParameters();
		parameters.setZoom(value);
    	setCameraParameters(parameters);
	}

	public int getExposureCompensation() {
		Camera.Parameters parameters = this.getParameters();
		return parameters.getExposureCompensation();
	}

	// Returns whether exposure was modified
	public boolean setExposureCompensation(int new_exposure) {
		Camera.Parameters parameters = this.getParameters();
		int current_exposure = parameters.getExposureCompensation();
		if( new_exposure != current_exposure ) {
			parameters.setExposureCompensation(new_exposure);
        	setCameraParameters(parameters);
        	return true;
		}
		return false;
	}

	public void setPreviewFpsRange(int min, int max) {
		Camera.Parameters parameters = this.getParameters();
        parameters.setPreviewFpsRange(min, max);
    	setCameraParameters(parameters);
	}

	public List<int []> getSupportedPreviewFpsRange() {
		Camera.Parameters parameters = this.getParameters();
		try {
			List<int []> fps_ranges = parameters.getSupportedPreviewFpsRange();
			return fps_ranges;
		}
		catch(StringIndexOutOfBoundsException e) {

			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void setFocusValue(String focus_value) {
		Camera.Parameters parameters = this.getParameters();
    	if( focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_locked") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    	}
    	else if( focus_value.equals("focus_mode_infinity") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
    	}
    	else if( focus_value.equals("focus_mode_macro") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
    	}
    	else if( focus_value.equals("focus_mode_fixed") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
    	}
    	else if( focus_value.equals("focus_mode_edof") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
    	}
    	else if( focus_value.equals("focus_mode_continuous_picture") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    	}
    	else if( focus_value.equals("focus_mode_continuous_video") ) {
    		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
    	}
    	setCameraParameters(parameters);
	}

	private String convertFocusModeToValue(String focus_mode) {
		// focus_mode may be null on some devices; we return ""

		String focus_value = "";
		if( focus_mode == null ) {
			// ignore, leave focus_value at null
		}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) ) {
    		focus_value = "focus_mode_auto";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_INFINITY) ) {
    		focus_value = "focus_mode_infinity";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ) {
    		focus_value = "focus_mode_macro";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_FIXED) ) {
    		focus_value = "focus_mode_fixed";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_EDOF) ) {
    		focus_value = "focus_mode_edof";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) ) {
    		focus_value = "focus_mode_continuous_picture";
    	}
		else if( focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ) {
    		focus_value = "focus_mode_continuous_video";
    	}
    	return focus_value;
	}

	@Override
	public String getFocusValue() {
		// returns "" if Parameters.getFocusMode() returns null
		Camera.Parameters parameters = this.getParameters();
		String focus_mode = parameters.getFocusMode();
		// getFocusMode() is documented as never returning null, however I've had null pointer exceptions reported in Google Play
		return convertFocusModeToValue(focus_mode);
	}

	@Override
	public float getFocusDistance() {
		// not supported for CameraController1!
		return 0.0f;
	}

	@Override
	public boolean setFocusDistance(float focus_distance) {
		// not supported for CameraController1!
		return false;
	}

	private String convertFlashValueToMode(String flash_value) {
		String flash_mode = "";
    	if( flash_value.equals("flash_off") ) {
    		flash_mode = Camera.Parameters.FLASH_MODE_OFF;
    	}
    	else if( flash_value.equals("flash_auto") ) {
    		flash_mode = Camera.Parameters.FLASH_MODE_AUTO;
    	}
    	else if( flash_value.equals("flash_on") ) {
    		flash_mode = Camera.Parameters.FLASH_MODE_ON;
    	}
    	else if( flash_value.equals("flash_torch") ) {
    		flash_mode = Camera.Parameters.FLASH_MODE_TORCH;
    	}
    	else if( flash_value.equals("flash_red_eye") ) {
    		flash_mode = Camera.Parameters.FLASH_MODE_RED_EYE;
    	}
    	return flash_mode;
	}

	public void setFlashValue(String flash_value) {
		Camera.Parameters parameters = this.getParameters();

		if( parameters.getFlashMode() == null )
			return; // flash mode not supported
		final String flash_mode = convertFlashValueToMode(flash_value);
    	if( flash_mode.length() > 0 && !flash_mode.equals(parameters.getFlashMode()) ) {
    		if( parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH) && !flash_mode.equals(Camera.Parameters.FLASH_MODE_OFF) ) {
    			// workaround for bug on Nexus 5 and Nexus 6 where torch doesn't switch off until we set FLASH_MODE_OFF
        		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            	setCameraParameters(parameters);
            	// need to set the correct flash mode after a delay
            	Handler handler = new Handler();
            	handler.postDelayed(new Runnable(){
            		@Override
            	    public void run(){

            			if( camera != null ) { // make sure camera wasn't released in the meantime (has a Google Play crash as a result of this)
	            			Camera.Parameters parameters = getParameters();
	                		parameters.setFlashMode(flash_mode);
	                    	setCameraParameters(parameters);
            			}
            	   }
            	}, 100);
    		}
    		else {
        		parameters.setFlashMode(flash_mode);
            	setCameraParameters(parameters);
    		}
    	}
	}

	private String convertFlashModeToValue(String flash_mode) {
		// flash_mode may be null, meaning flash isn't supported; we return ""

		String flash_value = "";
		if( flash_mode == null ) {
			// ignore, leave flash_value at null
		}
		else if( flash_mode.equals(Camera.Parameters.FLASH_MODE_OFF) ) {
    		flash_value = "flash_off";
    	}
    	else if( flash_mode.equals(Camera.Parameters.FLASH_MODE_AUTO) ) {
    		flash_value = "flash_auto";
    	}
    	else if( flash_mode.equals(Camera.Parameters.FLASH_MODE_ON) ) {
    		flash_value = "flash_on";
    	}
    	else if( flash_mode.equals(Camera.Parameters.FLASH_MODE_TORCH) ) {
    		flash_value = "flash_torch";
    	}
    	else if( flash_mode.equals(Camera.Parameters.FLASH_MODE_RED_EYE) ) {
    		flash_value = "flash_red_eye";
    	}
    	return flash_value;
	}

	public String getFlashValue() {
		// returns "" if flash isn't supported
		Camera.Parameters parameters = this.getParameters();
		String flash_mode = parameters.getFlashMode(); // will be null if flash mode not supported
		return convertFlashModeToValue(flash_mode);
	}

	public void setRecordingHint(boolean hint) {

		Camera.Parameters parameters = this.getParameters();
		String focus_mode = parameters.getFocusMode();
		// getFocusMode() is documented as never returning null, however I've had null pointer exceptions reported in Google Play
        if( focus_mode != null && !focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ) {
			parameters.setRecordingHint(hint);
        	setCameraParameters(parameters);
        }
	}

	public void setAutoExposureLock(boolean enabled) {
		Camera.Parameters parameters = this.getParameters();
		parameters.setAutoExposureLock(enabled);
    	setCameraParameters(parameters);
	}

	public boolean getAutoExposureLock() {
		Camera.Parameters parameters = this.getParameters();
		if( !parameters.isAutoExposureLockSupported() )
			return false;
		return parameters.getAutoExposureLock();
	}

	public void setRotation(int rotation) {
		Camera.Parameters parameters = this.getParameters();
		parameters.setRotation(rotation);
    	setCameraParameters(parameters);
	}

	public void setLocationInfo(Location location) {
        Camera.Parameters parameters = this.getParameters();
        parameters.removeGpsData();
        parameters.setGpsTimestamp(System.currentTimeMillis() / 1000); // initialise to a value (from Android camera source)
        parameters.setGpsLatitude(location.getLatitude());
        parameters.setGpsLongitude(location.getLongitude());
        parameters.setGpsProcessingMethod(location.getProvider()); // from http://boundarydevices.com/how-to-write-an-android-camera-app/
        if( location.hasAltitude() ) {
            parameters.setGpsAltitude(location.getAltitude());
        }
        else {
            parameters.setGpsAltitude(0);
        }
        if( location.getTime() != 0 ) { // from Android camera source
        	parameters.setGpsTimestamp(location.getTime() / 1000);
        }
    	setCameraParameters(parameters);
	}

	public void removeLocationInfo() {
        Camera.Parameters parameters = this.getParameters();
        parameters.removeGpsData();
    	setCameraParameters(parameters);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void enableShutterSound(boolean enabled) {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
        	camera.enableShutterSound(enabled);
        }
	}

	public boolean setFocusAndMeteringArea(List<Area> areas) {
		List<Camera.Area> camera_areas = new ArrayList<Camera.Area>();
		for(CameraController.Area area : areas) {
			camera_areas.add(new Camera.Area(area.rect, area.weight));
		}
        Camera.Parameters parameters = this.getParameters();
		String focus_mode = parameters.getFocusMode();

        if( parameters.getMaxNumFocusAreas() != 0 && focus_mode != null && ( focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) ) ) {
		    parameters.setFocusAreas(camera_areas);

		    // also set metering areas
		    if( parameters.getMaxNumMeteringAreas() == 0 ) {

		    }
		    else {
		    	parameters.setMeteringAreas(camera_areas);
		    }

		    setCameraParameters(parameters);

		    return true;
        }
        else if( parameters.getMaxNumMeteringAreas() != 0 ) {
	    	parameters.setMeteringAreas(camera_areas);

		    setCameraParameters(parameters);
        }
        return false;
	}

	public void clearFocusAndMetering() {
        Camera.Parameters parameters = this.getParameters();
        boolean update_parameters = false;
        if( parameters.getMaxNumFocusAreas() > 0 ) {
        	parameters.setFocusAreas(null);
        	update_parameters = true;
        }
        if( parameters.getMaxNumMeteringAreas() > 0 ) {
        	parameters.setMeteringAreas(null);
        	update_parameters = true;
        }
        if( update_parameters ) {
		    setCameraParameters(parameters);
        }
	}

	public List<Area> getFocusAreas() {
        Camera.Parameters parameters = this.getParameters();
		List<Camera.Area> camera_areas = parameters.getFocusAreas();
		if( camera_areas == null )
			return null;
		List<Area> areas = new ArrayList<Area>();
		for(Camera.Area camera_area : camera_areas) {
			areas.add(new CameraController.Area(camera_area.rect, camera_area.weight));
		}
		return areas;
	}

	public List<Area> getMeteringAreas() {
        Camera.Parameters parameters = this.getParameters();
		List<Camera.Area> camera_areas = parameters.getMeteringAreas();
		if( camera_areas == null )
			return null;
		List<Area> areas = new ArrayList<Area>();
		for(Camera.Area camera_area : camera_areas) {
			areas.add(new CameraController.Area(camera_area.rect, camera_area.weight));
		}
		return areas;
	}

	public boolean supportsAutoFocus() {
        Camera.Parameters parameters = this.getParameters();
		String focus_mode = parameters.getFocusMode();
		if( focus_mode != null && ( focus_mode.equals(Camera.Parameters.FOCUS_MODE_AUTO) || focus_mode.equals(Camera.Parameters.FOCUS_MODE_MACRO) ) ) {
        	return true;
        }
        return false;
	}
	
	public boolean focusIsVideo() {
		Camera.Parameters parameters = this.getParameters();
		String current_focus_mode = parameters.getFocusMode();
		boolean focus_is_video = current_focus_mode != null && current_focus_mode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

		return focus_is_video;
	}
	
	@Override
	public 
	void reconnect() throws CameraControllerException {

		try {
			camera.reconnect();
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new CameraControllerException();
		}
	}
	
	@Override
	public void setPreviewDisplay(SurfaceHolder holder) throws CameraControllerException {
		try {
			camera.setPreviewDisplay(holder);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new CameraControllerException();
		}
	}

	@Override
	public void setPreviewTexture(SurfaceTexture texture) throws CameraControllerException {
		try {
			camera.setPreviewTexture(texture);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new CameraControllerException();
		}
	}

	@Override
	public void startPreview() throws CameraControllerException {
		try {
			camera.startPreview();
		}
		catch(RuntimeException e) {
			e.printStackTrace();
			throw new CameraControllerException();
		}
	}
	
	@Override
	public void stopPreview() {
		camera.stopPreview();
	}
	
	// returns false if RuntimeException thrown (may include if face-detection already started)
	public boolean startFaceDetection() {
	    try {
			camera.startFaceDetection();
	    }
	    catch(RuntimeException e) {
	    	return false;
	    }
	    return true;
	}
	
	public void setFaceDetectionListener(final CameraController.FaceDetectionListener listener) {
		class CameraFaceDetectionListener implements Camera.FaceDetectionListener {
		    @Override
		    public void onFaceDetection(Camera.Face[] camera_faces, Camera camera) {
		    	Face [] faces = new Face[camera_faces.length];
		    	for(int i=0;i<camera_faces.length;i++) {
		    		faces[i] = new Face(camera_faces[i].score, camera_faces[i].rect);
		    	}
		    	listener.onFaceDetection(faces);
		    }
		}
		camera.setFaceDetectionListener(new CameraFaceDetectionListener());
	}

	public void autoFocus(final CameraController.AutoFocusCallback cb) {
        Camera.AutoFocusCallback camera_cb = new Camera.AutoFocusCallback() {
    		boolean done_autofocus = false;

    		@Override
			public void onAutoFocus(boolean success, Camera camera) {
				if( !done_autofocus ) {
					done_autofocus = true;
					cb.onAutoFocus(success);
				}
			}
        };
        try {
        	camera.autoFocus(camera_cb);
        }
		catch(RuntimeException e) {
			e.printStackTrace();

			cb.onAutoFocus(false);
		}
	}
	
	public void cancelAutoFocus() {
		try {
			camera.cancelAutoFocus();
		}
		catch(RuntimeException e) {
    		e.printStackTrace();
		}
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setContinuousFocusMoveCallback(final ContinuousFocusMoveCallback cb) {
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
			try {
				if( cb != null ) {
					camera.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
						@Override
						public void onAutoFocusMoving(boolean start, Camera camera) {
							cb.onContinuousFocusMove(start);
						}
					});
				}
				else {
					camera.setAutoFocusMoveCallback(null);
				}
			}
			catch(RuntimeException e) {

				e.printStackTrace();
			}
		}
	}

	private static class TakePictureShutterCallback implements Camera.ShutterCallback {
		@Override
        public void onShutter() {

        }
	}
	
	public void takePicture(final CameraController.PictureCallback picture, final ErrorCallback error) {

    	Camera.ShutterCallback shutter = new TakePictureShutterCallback();
        Camera.PictureCallback camera_jpeg = picture == null ? null : new Camera.PictureCallback() {
    	    public void onPictureTaken(byte[] data, Camera cam) {
    	    	// n.b., this is automatically run in a different thread
    	    	picture.onCompleted();
    	    }
        };

        try {
        	camera.takePicture(shutter, null, camera_jpeg);
        }
		catch(RuntimeException e) {

			e.printStackTrace();
			error.onError();
		}
	}
	
	public void setDisplayOrientation(int degrees) {
	    int result = 0;
	    if( camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ) {
	        result = (camera_info.orientation + degrees) % 360;
	        result = (360 - result) % 360;  // compensate the mirror
	    }
	    else {
	        result = (camera_info.orientation - degrees + 360) % 360;
	    }

		camera.setDisplayOrientation(result);
	    this.display_orientation = result;
	}
	
	public int getDisplayOrientation() {
		return this.display_orientation;
	}
	
	public int getCameraOrientation() {
		return camera_info.orientation;
	}
	
	public boolean isFrontFacing() {
		return (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
	}
	
	public void unlock() {
		this.stopPreview();
		camera.unlock();
	}
	
	@Override
	public void initVideoRecorderPrePrepare(MediaRecorder video_recorder) {
    	video_recorder.setCamera(camera);
	}
	
	@Override
	public void initVideoRecorderPostPrepare(MediaRecorder video_recorder) throws CameraControllerException {

	}
	
	@Override
	public String getParametersString() {
		String string = "";
		try {
			string = this.getParameters().flatten();
		}
        catch(Exception e) {
        	e.printStackTrace();
        }
		return string;
	}
}
