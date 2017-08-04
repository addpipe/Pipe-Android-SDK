package com.android.pipe.pipeandroidsdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class Preview implements SurfaceHolder.Callback, TextureView.SurfaceTextureListener {

	private boolean using_android_l = false;
	private boolean using_texture_view = false;

	private ApplicationInterface applicationInterface = null;
	private CameraSurface cameraSurface = null;
	private CanvasView canvasView = null;
	private boolean set_preview_size = false;
	private int preview_w = 0, preview_h = 0;
	private boolean set_textureview_size = false;
	private int textureview_w = 0, textureview_h = 0;

    private Matrix camera_to_preview_matrix = new Matrix();
    private Matrix preview_to_camera_matrix = new Matrix();
    private double preview_targetRatio = 0.0;


	private boolean app_is_paused = true;
	private boolean has_surface = false;
	private boolean has_aspect_ratio = false;
	private double aspect_ratio = 0.0f;
	private CameraControllerManager camera_controller_manager = null;
	private CameraController camera_controller = null;
	private boolean is_video = false;
	private MediaRecorder video_recorder = null;
	private boolean video_start_time_set = false;
	private long video_start_time = 0;
	private long video_accumulated_time = 0;
	private static final long min_safe_restart_video_time = 1000;
	private int video_method = ApplicationInterface.VIDEOMETHOD_FILE;
	private Uri video_uri = null;
	private String video_filename = null;

	private static final int PHASE_NORMAL = 0;
	private static final int PHASE_TIMER = 1;
	private static final int PHASE_TAKING_PHOTO = 2;
	private static final int PHASE_PREVIEW_PAUSED = 3;
	private int phase = PHASE_NORMAL;
	private Timer takePictureTimer = new Timer();
	private TimerTask takePictureTimerTask = null;
	private Timer beepTimer = new Timer();
	private TimerTask beepTimerTask = null;
	private Timer restartVideoTimer = new Timer();
	private TimerTask restartVideoTimerTask = null;
	private Timer flashVideoTimer = new Timer();
	private TimerTask flashVideoTimerTask = null;
	private long take_photo_time = 0;
	private int remaining_burst_photos = 0;
	private int remaining_restart_video = 0;

	private boolean is_preview_started = false;

	private int current_orientation = 0;
	private int current_rotation = 0;
	private boolean has_level_angle = false;
	private double level_angle = 0.0f;
	private double orig_level_angle = 0.0f;
	
	private boolean has_zoom = false;
	private int max_zoom_factor = 0;
	private GestureDetector gestureDetector = null;
	private ScaleGestureDetector scaleGestureDetector = null;
	private List<Integer> zoom_ratios = null;
	private float minimum_focus_distance = 0.0f;
	private boolean touch_was_multitouch = false;
	private float touch_orig_x = 0.0f;
	private float touch_orig_y = 0.0f;

	private List<String> supported_flash_values = null;
	private int current_flash_index = -1;

	private List<String> supported_focus_values = null;
	private int current_focus_index = -1;
	private int max_num_focus_areas = 0;
	private boolean continuous_focus_move_is_started = false;
	
	private boolean is_exposure_lock_supported = false;
	private boolean is_exposure_locked = false;

	private List<String> color_effects = null;
	private List<String> scene_modes = null;
	private List<String> white_balances = null;
	private List<String> isos = null;
	private boolean supports_iso_range = false;
	private int min_iso = 0;
	private int max_iso = 0;
	private boolean supports_exposure_time = false;
	private long min_exposure_time = 0l;
	private long max_exposure_time = 0l;
	private List<String> exposures = null;
	private int min_exposure = 0;
	private int max_exposure = 0;
	private float exposure_step = 0.0f;

	private List<CameraController.Size> supported_preview_sizes = null;
	
	private List<CameraController.Size> sizes = null;
	private int current_size_index = -1;

	private List<String> video_quality = null;
	private int current_video_quality = -1;
	private List<CameraController.Size> video_sizes = null;

	private Toast last_toast = null;
	private ToastBoxer flash_toast = new ToastBoxer();
	private ToastBoxer focus_toast = new ToastBoxer();
	private ToastBoxer take_photo_toast = new ToastBoxer();
	private ToastBoxer seekbar_toast = new ToastBoxer();
	
	private int ui_rotation = 0;

	private boolean supports_face_detection = false;
	private boolean using_face_detection = false;
	private CameraController.Face [] faces_detected = null;
	private boolean supports_video_stabilization = false;
	private boolean can_disable_shutter_sound = false;
	private boolean has_focus_area = false;
	private long focus_complete_time = -1;
	private long focus_started_time = -1;
	private int focus_success = FOCUS_DONE;
	private static final int FOCUS_WAITING = 0;
	private static final int FOCUS_SUCCESS = 1;
	private static final int FOCUS_FAILED = 2;
	private static final int FOCUS_DONE = 3;
	private String set_flash_value_after_autofocus = "";
	private boolean take_photo_after_autofocus = false;
	private boolean successfully_focused = false;
	private long successfully_focused_time = -1;

	private static final float sensor_alpha = 0.8f;
    private boolean has_gravity = false;
    private float [] gravity = new float[3];
    private boolean has_geomagnetic = false;
    private float [] geomagnetic = new float[3];
    private float [] deviceRotation = new float[9];
    private float [] cameraRotation = new float[9];
    private float [] deviceInclination = new float[9];
    private boolean has_geo_direction = false;
    private float [] geo_direction = new float[3];

	private final DecimalFormat decimal_format_1dp = new DecimalFormat("#.#");
	private final DecimalFormat decimal_format_2dp = new DecimalFormat("#.##");

	// for testing:
	public int count_cameraStartPreview = 0;
	public int count_cameraAutoFocus = 0;
	public int count_cameraTakePicture = 0;
	public int count_cameraContinuousFocusMoving = 0;
	public boolean test_fail_open_camera = false;
	public boolean test_video_failure = false;

	public Preview(ApplicationInterface applicationInterface, Bundle savedInstanceState, ViewGroup parent) {
		
		this.applicationInterface = applicationInterface;
		
		this.using_android_l = applicationInterface.useCamera2();

		if( using_android_l ) {
        	this.using_texture_view = true;
		}

        if( using_texture_view ) {
    		this.cameraSurface = new MyTextureView(getContext(), savedInstanceState, this);

    		this.canvasView = new CanvasView(getContext(), savedInstanceState, this);
    		camera_controller_manager = new CameraControllerManager2(getContext());
        }
        else {
    		this.cameraSurface = new MySurfaceView(getContext(), savedInstanceState, this);
    		camera_controller_manager = new CameraControllerManager1();
        }

	    gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener());
	    scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

		parent.addView(cameraSurface.getView());

		if( canvasView != null ) {
			parent.addView(canvasView);
		}
	}

	private Resources getResources() {
		return cameraSurface.getView().getResources();
	}
	
	public View getView() {
		return cameraSurface.getView();
	}

	private void calculateCameraToPreviewMatrix() {
		if( camera_controller == null )
			return;
		camera_to_preview_matrix.reset();
	    if( !using_android_l ) {
			boolean mirror = camera_controller.isFrontFacing();
			camera_to_preview_matrix.setScale(mirror ? -1 : 1, 1);
			camera_to_preview_matrix.postRotate(camera_controller.getDisplayOrientation());
	    }
	    else {
	    	boolean mirror = camera_controller.isFrontFacing();
			camera_to_preview_matrix.setScale(1, mirror ? -1 : 1);
	    }
		camera_to_preview_matrix.postScale(cameraSurface.getView().getWidth() / 2000f, cameraSurface.getView().getHeight() / 2000f);
		camera_to_preview_matrix.postTranslate(cameraSurface.getView().getWidth() / 2f, cameraSurface.getView().getHeight() / 2f);
	}
	
	private void calculatePreviewToCameraMatrix() {
		if( camera_controller == null )
			return;
		calculateCameraToPreviewMatrix();
	}

	private ArrayList<CameraController.Area> getAreas(float x, float y) {
		float [] coords = {x, y};
		calculatePreviewToCameraMatrix();
		preview_to_camera_matrix.mapPoints(coords);
		float focus_x = coords[0];
		float focus_y = coords[1];
		
		int focus_size = 50;

		Rect rect = new Rect();
		rect.left = (int)focus_x - focus_size;
		rect.right = (int)focus_x + focus_size;
		rect.top = (int)focus_y - focus_size;
		rect.bottom = (int)focus_y + focus_size;
		if( rect.left < -1000 ) {
			rect.left = -1000;
			rect.right = rect.left + 2*focus_size;
		}
		else if( rect.right > 1000 ) {
			rect.right = 1000;
			rect.left = rect.right - 2*focus_size;
		}
		if( rect.top < -1000 ) {
			rect.top = -1000;
			rect.bottom = rect.top + 2*focus_size;
		}
		else if( rect.bottom > 1000 ) {
			rect.bottom = 1000;
			rect.top = rect.bottom - 2*focus_size;
		}

	    ArrayList<CameraController.Area> areas = new ArrayList<CameraController.Area>();
	    areas.add(new CameraController.Area(rect, 1000));
	    return areas;
	}

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    	@Override
    	public boolean onScale(ScaleGestureDetector detector) {
    		if( Preview.this.camera_controller != null && Preview.this.has_zoom ) {
    			Preview.this.scaleZoom(detector.getScaleFactor());
    		}
    		return true;
    	}
    }
    
    public void clearFocusAreas() {

		if( camera_controller == null ) {

			return;
		}
        camera_controller.clearFocusAndMetering();
		has_focus_area = false;
		focus_success = FOCUS_DONE;
		successfully_focused = false;
    }

    public void getMeasureSpec(int [] spec, int widthSpec, int heightSpec) {
    	if( !this.hasAspectRatio() ) {
    		spec[0] = widthSpec;
    		spec[1] = heightSpec;
    		return;
    	}
    	double aspect_ratio = this.getAspectRatio();

    	int previewWidth = MeasureSpec.getSize(widthSpec);
        int previewHeight = MeasureSpec.getSize(heightSpec);


        int hPadding = cameraSurface.getView().getPaddingLeft() + cameraSurface.getView().getPaddingRight();
        int vPadding = cameraSurface.getView().getPaddingTop() + cameraSurface.getView().getPaddingBottom();


        previewWidth -= hPadding;
        previewHeight -= vPadding;

        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);

        if (longSide > shortSide * aspect_ratio) {
            longSide = (int) ((double) shortSide * aspect_ratio);
        } else {
            shortSide = (int) ((double) longSide / aspect_ratio);
        }
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }

        spec[0] = MeasureSpec.makeMeasureSpec(widthSpec, MeasureSpec.EXACTLY);
        spec[1] = MeasureSpec.makeMeasureSpec(heightSpec, MeasureSpec.EXACTLY);
    }
    
    private void mySurfaceCreated() {
		this.has_surface = true;
		this.openCamera();
    }
    
    private void mySurfaceDestroyed() {
		this.has_surface = false;
		this.closeCamera();
    }
    
    private void mySurfaceChanged() {
        if( camera_controller == null ) {
            return;
        }
	}
    
	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		mySurfaceCreated();
		cameraSurface.getView().setWillNotDraw(false);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		mySurfaceDestroyed();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if( holder.getSurface() == null ) {
            return;
        }
		mySurfaceChanged();
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture arg0, int width, int height) {
		this.set_textureview_size = true;
		this.textureview_w = width;
		this.textureview_h = height;
		mySurfaceCreated();
		configureTransform();
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
		this.set_textureview_size = false;
		this.textureview_w = 0;
		this.textureview_h = 0;
		mySurfaceDestroyed();
		return true;
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int width, int height) {
		this.set_textureview_size = true;
		this.textureview_w = width;
		this.textureview_h = height;
		mySurfaceChanged();
		configureTransform();
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
	}

    private void configureTransform() {
    	if( camera_controller == null || !this.set_preview_size || !this.set_textureview_size )
    		return;

    	int rotation = getDisplayRotation();
    	Matrix matrix = new Matrix(); 
		RectF viewRect = new RectF(0, 0, this.textureview_w, this.textureview_h); 
		RectF bufferRect = new RectF(0, 0, this.preview_h, this.preview_w); 
		float centerX = viewRect.centerX(); 
		float centerY = viewRect.centerY(); 
        if( Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation ) { 
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY()); 
	        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL); 
	        float scale = Math.max(
	        		(float) textureview_h / preview_h, 
                    (float) textureview_w / preview_w); 
            matrix.postScale(scale, scale, centerX, centerY); 
            matrix.postRotate(90 * (rotation - 2), centerX, centerY); 
        } 
        cameraSurface.setTransform(matrix); 
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopVideo(boolean from_restart) {

		if( video_recorder == null ) {
			return;
		}
		applicationInterface.stoppingVideo();
		if( restartVideoTimerTask != null ) {
			restartVideoTimerTask.cancel();
			restartVideoTimerTask = null;
		}
		if( flashVideoTimerTask != null ) {
			flashVideoTimerTask.cancel();
			flashVideoTimerTask = null;
		}
		if( !from_restart ) {
			remaining_restart_video = 0;
		}

		if( video_recorder != null ) {
			this.phase = PHASE_NORMAL;

			video_recorder.stop();

//			try {
//				video_recorder.setOnErrorListener(null);
//				video_recorder.setOnInfoListener(null);
//
//				video_recorder.stop();
//
//			}
//			catch(RuntimeException e) {
//
//	    		if( video_method == ApplicationInterface.VIDEOMETHOD_SAF ) {
//	    			if( video_uri != null ) {
//
//	    				DocumentsContract.deleteDocument(getContext().getContentResolver(), video_uri);
//	    			}
//	    		}
//	    		else if( video_method == ApplicationInterface.VIDEOMETHOD_FILE ) {
//		    		if( video_filename != null ) {
//
//		    			File file = new File(video_filename);
//		    		}
//	    		}
//	    		video_method = ApplicationInterface.VIDEOMETHOD_FILE;
//	    		video_uri = null;
//    			video_filename = null;
//
//    			if( !video_start_time_set || System.currentTimeMillis() - video_start_time > 2000 ) {
//    	        	CamcorderProfile profile = getCamcorderProfile();
//    				applicationInterface.onVideoRecordStopError(profile);
//    			}
//			}

    		video_recorder.reset();

    		video_recorder.release(); 
    		video_recorder = null;
			reconnectCamera(false);
			applicationInterface.stoppedVideo(video_method, video_uri, video_filename);
    		video_method = ApplicationInterface.VIDEOMETHOD_FILE;
    		video_uri = null;
			video_filename = null;
		}
	}
	
	private Context getContext() {
		return applicationInterface.getContext();
	}

	private void restartVideo(boolean due_to_max_filesize) {

		if( video_recorder != null ) {
			if( due_to_max_filesize ) {
				long last_time = System.currentTimeMillis() - video_start_time;
				video_accumulated_time += last_time;

			}
			else {
				video_accumulated_time = 0;
			}
    		stopVideo(true);

			if( due_to_max_filesize ) {
				long video_max_duration = applicationInterface.getVideoMaxDurationPref();
				if( video_max_duration > 0 ) {
					video_max_duration -= video_accumulated_time;
					if( video_max_duration < min_safe_restart_video_time ) {
			    		due_to_max_filesize = false;
					}
				}
			}
			if( due_to_max_filesize || remaining_restart_video > 0 ) {
				if( is_video ) {
					String toast = null;
					if( !due_to_max_filesize )
						toast = remaining_restart_video + " " + getContext().getResources().getString(R.string.repeats_to_go);
					takePicture(due_to_max_filesize);
					if( !due_to_max_filesize ) {
						showToast(null, toast);
						remaining_restart_video--;
					}
				}
				else {
					remaining_restart_video = 0;
				}
			}
		}
	}
	
	private void reconnectCamera(boolean quiet) {

        if( camera_controller != null ) {
    		try {
    			camera_controller.reconnect();
    			this.setPreviewPaused(false);
			}
    		catch(CameraControllerException e) {

				e.printStackTrace();
				applicationInterface.onFailedReconnectError();
	    	    closeCamera();
			}
    		try {
    			tryAutoFocus(false, false);
    		}
    		catch(RuntimeException e) {

    			e.printStackTrace();

				this.is_preview_started = false;
    			if( !quiet ) {
    	        	CamcorderProfile profile = getCamcorderProfile();
    				applicationInterface.onVideoRecordStopError(profile);
    			}
    			camera_controller.release();
    			camera_controller = null;
    			openCamera();
    		}
		}
	}

	private void closeCamera() {

		has_focus_area = false;
		focus_success = FOCUS_DONE;
		focus_started_time = -1;
		take_photo_after_autofocus = false;
		set_flash_value_after_autofocus = "";
		successfully_focused = false;
		preview_targetRatio = 0.0;

		if( continuous_focus_move_is_started ) {
			continuous_focus_move_is_started = false;
			applicationInterface.onContinuousFocusMove(false);
		}
		applicationInterface.cameraClosed();
		cancelTimer();
		if( camera_controller != null ) {
			if( video_recorder != null ) {
				stopVideo(false);
			}
			if( this.is_video ) {
				this.updateFocusForVideo(false);
			}

			if( camera_controller != null ) {

				pausePreview();

				camera_controller.release();
				camera_controller = null;
			}
		}

	}
	
	public void cancelTimer() {

		if( this.isOnTimer() ) {
			takePictureTimerTask.cancel();
			takePictureTimerTask = null;
			if( beepTimerTask != null ) {
				beepTimerTask.cancel();
				beepTimerTask = null;
			}
			this.phase = PHASE_NORMAL;

		}
	}
	
	public void pausePreview() {

		if( camera_controller == null ) {

			return;
		}
		if( this.is_video ) {
			this.updateFocusForVideo(false);
		}
		this.setPreviewPaused(false);

		camera_controller.stopPreview();
		this.phase = PHASE_NORMAL;
		this.is_preview_started = false;

		applicationInterface.cameraInOperation(false);

	}

	private void openCamera() {

		is_preview_started = false;
    	set_preview_size = false;
    	preview_w = 0;
    	preview_h = 0;
		has_focus_area = false;
		focus_success = FOCUS_DONE;
		focus_started_time = -1;
		take_photo_after_autofocus = false;
		set_flash_value_after_autofocus = "";
		successfully_focused = false;
		preview_targetRatio = 0.0;
		scene_modes = null;
		has_zoom = false;
		max_zoom_factor = 0;
		minimum_focus_distance = 0.0f;
		zoom_ratios = null;
		faces_detected = null;
		supports_face_detection = false;
		using_face_detection = false;
		supports_video_stabilization = false;
		can_disable_shutter_sound = false;
		color_effects = null;
		white_balances = null;
		isos = null;
		supports_iso_range = false;
		min_iso = 0;
		max_iso = 0;
		supports_exposure_time = false;
		min_exposure_time = 0l;
		max_exposure_time = 0l;
		exposures = null;
		min_exposure = 0;
		max_exposure = 0;
		exposure_step = 0.0f;
		sizes = null;
		current_size_index = -1;
		video_quality = null;
		current_video_quality = -1;
		supported_flash_values = null;
		current_flash_index = -1;
		supported_focus_values = null;
		current_focus_index = -1;
		max_num_focus_areas = 0;
		applicationInterface.cameraInOperation(false);

		if( !this.has_surface ) {
			return;
		}
		if( this.app_is_paused ) {
			return;
		}

		try {
			int cameraId = applicationInterface.getCameraIdPref();
			if( cameraId < 0 || cameraId >= camera_controller_manager.getNumberOfCameras() ) {

				cameraId = 0;
				applicationInterface.setCameraIdPref(cameraId);
			}

			if( test_fail_open_camera ) {

				throw new CameraControllerException();
			}

	        if( using_android_l ) {
	    		CameraController.ErrorCallback previewErrorCallback = new CameraController.ErrorCallback() {
	    			public void onError() {

	        			applicationInterface.onFailedStartPreview();
	        	    }
	    		};
	        	camera_controller = new CameraController2(this.getContext(), cameraId, previewErrorCallback);
	        }
	        else
				camera_controller = new CameraController1(cameraId);


		}
		catch(CameraControllerException e) {

			e.printStackTrace();
			camera_controller = null;
		}

		boolean take_photo = false;
		if( camera_controller != null ) {
			Activity activity = (Activity)this.getContext();

			if( activity.getIntent() != null && activity.getIntent().getExtras() != null ) {
				take_photo = activity.getIntent().getExtras().getBoolean(TakePhoto.TAKE_PHOTO);
				activity.getIntent().removeExtra(TakePhoto.TAKE_PHOTO);
			}

	        this.setCameraDisplayOrientation();
	        new OrientationEventListener(activity) {
				@Override
				public void onOrientationChanged(int orientation) {
					Preview.this.onOrientationChanged(orientation);
				}
	        }.enable();

			cameraSurface.setPreviewDisplay(camera_controller);

		    setupCamera(take_photo);
		}

	}

	public void setupCamera(boolean take_photo) {

		if( camera_controller == null ) {

			return;
		}

		boolean do_startup_focus = !take_photo && applicationInterface.getStartupFocusPref();

		if( this.is_video ) {
			this.updateFocusForVideo(false);
		}

		setupCameraParameters();

		boolean saved_is_video = applicationInterface.isVideoPref();

		if( saved_is_video != this.is_video ) {
			this.switchVideo(true);
		}

		if( do_startup_focus && using_android_l && camera_controller.supportsAutoFocus() ) {

			set_flash_value_after_autofocus = "";
			String old_flash_value = camera_controller.getFlashValue();
			if( old_flash_value.length() > 0 && !old_flash_value.equals("flash_off") && !old_flash_value.equals("flash_torch") ) {
				set_flash_value_after_autofocus = old_flash_value;
				camera_controller.setFlashValue("flash_off");
			}
		}

		setPreviewSize();

		startCameraPreview();

	    if( take_photo ) {
			if( this.is_video ) {
				this.switchVideo(false);
			}

	    	final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					takePicture(false);
				}
			}, 500);
		}

		applicationInterface.cameraSetup();


	    if( do_startup_focus ) {
	    	final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {

					tryAutoFocus(true, false);
				}
			}, 500);
	    }

	}

	private void setupCameraParameters() {
		{
			String value = applicationInterface.getSceneModePref();

			CameraController.SupportedValues supported_values = camera_controller.setSceneMode(value);
			if( supported_values != null ) {
				scene_modes = supported_values.values;

				applicationInterface.setSceneModePref(supported_values.selected_value);
			}
			else {

				applicationInterface.clearSceneModePref();
			}
		}
		
		{

			CameraController.CameraFeatures camera_features = camera_controller.getCameraFeatures();
			this.has_zoom = camera_features.is_zoom_supported;
			if( this.has_zoom ) {
				this.max_zoom_factor = camera_features.max_zoom;
				this.zoom_ratios = camera_features.zoom_ratios;
			}
			this.minimum_focus_distance = camera_features.minimum_focus_distance;
			this.supports_face_detection = camera_features.supports_face_detection;
			this.sizes = camera_features.picture_sizes;
	        supported_flash_values = camera_features.supported_flash_values;
	        supported_focus_values = camera_features.supported_focus_values;
	        this.max_num_focus_areas = camera_features.max_num_focus_areas;
	        this.is_exposure_lock_supported = camera_features.is_exposure_lock_supported;
	        this.supports_video_stabilization = camera_features.is_video_stabilization_supported;
	        this.can_disable_shutter_sound = camera_features.can_disable_shutter_sound;
	        this.supports_iso_range = camera_features.supports_iso_range;
	        this.min_iso = camera_features.min_iso;
	        this.max_iso = camera_features.max_iso;
	        this.supports_exposure_time = camera_features.supports_exposure_time;
	        this.min_exposure_time = camera_features.min_exposure_time;
	        this.max_exposure_time = camera_features.max_exposure_time;
			this.min_exposure = camera_features.min_exposure;
			this.max_exposure = camera_features.max_exposure;
			this.exposure_step = camera_features.exposure_step;
			this.video_sizes = camera_features.video_sizes;
	        this.supported_preview_sizes = camera_features.preview_sizes;
		}
		
		{

			this.faces_detected = null;
			this.using_face_detection = false;

			if( this.using_face_detection ) {
				class MyFaceDetectionListener implements CameraController.FaceDetectionListener {
				    @Override
				    public void onFaceDetection(CameraController.Face[] faces) {
				    	faces_detected = new CameraController.Face[faces.length];
				    	System.arraycopy(faces, 0, faces_detected, 0, faces.length);				    	
				    }
				}
				camera_controller.setFaceDetectionListener(new MyFaceDetectionListener());
			}
		}
		
		{
			if( this.supports_video_stabilization ) {
				boolean using_video_stabilization = applicationInterface.getVideoStabilizationPref();
				camera_controller.setVideoStabilization(using_video_stabilization);
			}
		}

		{
			String value = applicationInterface.getColorEffectPref();

			CameraController.SupportedValues supported_values = camera_controller.setColorEffect(value);
			if( supported_values != null ) {
				color_effects = supported_values.values;

				applicationInterface.setColorEffectPref(supported_values.selected_value);
			}
		}

		{
			current_size_index = -1;
			Pair<Integer, Integer> resolution = applicationInterface.getCameraResolutionPref();
			if( resolution != null ) {
				int resolution_w = resolution.first;
				int resolution_h = resolution.second;
				// now find size in valid list
				for(int i=0;i<sizes.size() && current_size_index==-1;i++) {
					CameraController.Size size = sizes.get(i);
		        	if( size.width == resolution_w && size.height == resolution_h ) {
		        		current_size_index = i;
		        	}
				}
			}

			if( current_size_index == -1 ) {

				CameraController.Size current_size = null;
				for(int i=0;i<sizes.size();i++) {
					CameraController.Size size = sizes.get(i);
					if( current_size == null || size.width*size.height > current_size.width*current_size.height ) {
						current_size_index = i;
						current_size = size;
					}
				}
			}

			if( current_size_index != -1 ) {
				CameraController.Size current_size = sizes.get(current_size_index);

	    		applicationInterface.setCameraResolutionPref(current_size.width, current_size.height);
			}
		}

		initialiseVideoSizes();
		initialiseVideoQuality();

		current_video_quality = -1;
		String video_quality_value_s = applicationInterface.getVideoQualityPref();

		if( video_quality_value_s.length() > 0 ) {
			for(int i=0;i<video_quality.size() && current_video_quality==-1;i++) {
	        	if( video_quality.get(i).equals(video_quality_value_s) ) {
	        		current_video_quality = i;
	        	}
			}
			if( current_video_quality == -1 ) {

			}
		}

		if( current_video_quality == -1 && video_quality.size() > 0 ) {
			current_video_quality = 0; // start with highest quality
			for(int i=0;i<video_quality.size();i++) {
				CamcorderProfile profile = getCamcorderProfile(video_quality.get(i));
				if( profile.videoFrameWidth == 1920 && profile.videoFrameHeight == 1080 ) {
					current_video_quality = i;
					break;
				}
			}
		}
		if( current_video_quality != -1 ) {
			applicationInterface.setVideoQualityPref(video_quality.get(current_video_quality));
		}

		{
			current_flash_index = -1;
			if( supported_flash_values != null && supported_flash_values.size() > 1 ) {

				String flash_value = applicationInterface.getFlashPref();
				if( flash_value.length() > 0 ) {
					if( !updateFlash(flash_value, false) ) { // don't need to save, as this is the value that's already saved
						updateFlash(0, true);
					}
				}
				else {
					updateFlash("flash_auto", true);
				}
			}
			else {
				supported_flash_values = null;
			}
		}

		{
			current_focus_index = -1;
			if( supported_focus_values != null && supported_focus_values.size() > 1 ) {

				setFocusPref(true);
			}
			else {
				supported_focus_values = null;
			}
		}

		{
	    	is_exposure_locked = false;
		}

	}
	
	private void setPreviewSize() {
		if( camera_controller == null ) {
			return;
		}
		if( is_preview_started ) {
			throw new RuntimeException(); // throw as RuntimeException, as this is a programming error
		}
		if( !using_android_l ) {
			this.cancelAutoFocus();
		}

		CameraController.Size new_size = null;
    	if( this.is_video ) {
    		CamcorderProfile profile = getCamcorderProfile();
        	double targetRatio = ((double)profile.videoFrameWidth) / (double)profile.videoFrameHeight;
        	new_size = getOptimalVideoPictureSize(sizes, targetRatio);
    	}
    	else {
    		if( current_size_index != -1 ) {
    			new_size = sizes.get(current_size_index);
    		}
    	}
    	if( new_size != null ) {
    		camera_controller.setPictureSize(new_size.width, new_size.height);
    	}

        if( supported_preview_sizes != null && supported_preview_sizes.size() > 0 ) {

        	CameraController.Size best_size = getOptimalPreviewSize(supported_preview_sizes);

        	camera_controller.setPreviewSize(best_size.width, best_size.height);
        	this.set_preview_size = true;
        	this.preview_w = best_size.width;
        	this.preview_h = best_size.height;
    		this.setAspectRatio( ((double)best_size.width) / (double)best_size.height );
        }
	}
	
	private static class SortVideoSizesComparator implements Comparator<CameraController.Size>, Serializable {
		private static final long serialVersionUID = 5802214721033718212L;

		@Override
		public int compare(final CameraController.Size a, final CameraController.Size b) {
			return b.width * b.height - a.width * a.height;
		}
	}

	private void sortVideoSizes() {
		Collections.sort(this.video_sizes, new SortVideoSizesComparator());
	}
	
	private void initialiseVideoSizes() {
		if( camera_controller == null ) {
			return;
		}
		this.sortVideoSizes();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void initialiseVideoQuality() {
		int cameraId = camera_controller.getCameraId();
		SparseArray<Pair<Integer, Integer>> profiles = new SparseArray<Pair<Integer, Integer>>();
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
        	profiles.put(CamcorderProfile.QUALITY_HIGH, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
	        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P) ) {
	    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P);
	        	profiles.put(CamcorderProfile.QUALITY_2160P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
	        }
		}
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
        	profiles.put(CamcorderProfile.QUALITY_1080P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
        	profiles.put(CamcorderProfile.QUALITY_720P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
        	profiles.put(CamcorderProfile.QUALITY_480P, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_CIF);
        	profiles.put(CamcorderProfile.QUALITY_CIF, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA);
        	profiles.put(CamcorderProfile.QUALITY_QVGA, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QCIF) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QCIF);
        	profiles.put(CamcorderProfile.QUALITY_QCIF, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        if( CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW) ) {
    		CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
        	profiles.put(CamcorderProfile.QUALITY_LOW, new Pair<Integer, Integer>(profile.videoFrameWidth, profile.videoFrameHeight));
        }
        initialiseVideoQualityFromProfiles(profiles);
	}

	private void addVideoResolutions(boolean done_video_size[], int base_profile, int min_resolution_w, int min_resolution_h) {
		if( video_sizes == null ) {
			return;
		}

    	for(int i=0;i<video_sizes.size();i++) {
    		if( done_video_size[i] )
    			continue;
    		CameraController.Size size = video_sizes.get(i);
    		if( size.width == min_resolution_w && size.height == min_resolution_h ) {
    			String str = "" + base_profile;
            	video_quality.add(str);
	        	done_video_size[i] = true;

    		}
    		else if( base_profile == CamcorderProfile.QUALITY_LOW || size.width * size.height >= min_resolution_w*min_resolution_h ) {
    			String str = "" + base_profile + "_r" + size.width + "x" + size.height;
	        	video_quality.add(str);
	        	done_video_size[i] = true;

    		}
        }
	}
	
	public void initialiseVideoQualityFromProfiles(SparseArray<Pair<Integer, Integer>> profiles) {

        video_quality = new Vector<String>();
        boolean done_video_size[] = null;
        if( video_sizes != null ) {
        	done_video_size = new boolean[video_sizes.size()];
        	for(int i=0;i<video_sizes.size();i++)
        		done_video_size[i] = false;
        }
        if( profiles.get(CamcorderProfile.QUALITY_HIGH) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_HIGH);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_HIGH, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_1080P) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_1080P);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_1080P, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_720P) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_720P);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_720P, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_480P) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_480P);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_480P, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_CIF) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_CIF);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_CIF, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_QVGA) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_QVGA);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_QVGA, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_QCIF) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_QCIF);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_QCIF, pair.first, pair.second);
        }
        if( profiles.get(CamcorderProfile.QUALITY_LOW) != null ) {

    		Pair<Integer, Integer> pair = profiles.get(CamcorderProfile.QUALITY_LOW);
    		addVideoResolutions(done_video_size, CamcorderProfile.QUALITY_LOW, pair.first, pair.second);
        }

	}
	
	private CamcorderProfile getCamcorderProfile(String quality) {

		if( camera_controller == null ) {

			return CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH);
		}
		int cameraId = camera_controller.getCameraId();
		CamcorderProfile camcorder_profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH); // default
		try {
			String profile_string = quality;
			int index = profile_string.indexOf('_');
			if( index != -1 ) {
				profile_string = quality.substring(0, index);

			}
			int profile = Integer.parseInt(profile_string);
			camcorder_profile = CamcorderProfile.get(cameraId, profile);
			if( index != -1 && index+1 < quality.length() ) {
				String override_string = quality.substring(index+1);

				if( override_string.charAt(0) == 'r' && override_string.length() >= 4 ) {
					index = override_string.indexOf('x');
					if( index == -1 ) {

					}
					else {
						String resolution_w_s = override_string.substring(1, index); // skip first 'r'
						String resolution_h_s = override_string.substring(index+1);

						int resolution_w = Integer.parseInt(resolution_w_s);
						int resolution_h = Integer.parseInt(resolution_h_s);
						camcorder_profile.videoFrameWidth = resolution_w;
						camcorder_profile.videoFrameHeight = resolution_h;
					}
				}

			}
		}
        catch(NumberFormatException e) {

    		e.printStackTrace();
        }
		return camcorder_profile;
	}
	
	public CamcorderProfile getCamcorderProfile() {
		if( camera_controller == null ) {
			return CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH);
		}

		CamcorderProfile profile = null;
		int cameraId = camera_controller.getCameraId();
		if( applicationInterface.getForce4KPref() ) {

			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
			profile.videoFrameWidth = 3840;
			profile.videoFrameHeight = 2160;
			profile.videoBitRate = (int)(profile.videoBitRate*2.8); // need a higher bitrate for the better quality - this is roughly based on the bitrate used by an S5's native camera app at 4K (47.6 Mbps, compared to 16.9 Mbps which is what's returned by the QUALITY_HIGH profile)
		}
		else if( current_video_quality != -1 ) {
			profile = getCamcorderProfile(video_quality.get(current_video_quality));
		}
		else {
			profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
		}

		String bitrate_value = applicationInterface.getVideoBitratePref();
		if( !bitrate_value.equals("default") ) {
			try {
				int bitrate = Integer.parseInt(bitrate_value);

				profile.videoBitRate = bitrate;
			}
			catch(NumberFormatException exception) {

			}
		}
		String fps_value = applicationInterface.getVideoFPSPref();
		if( !fps_value.equals("default") ) {
			try {
				int fps = Integer.parseInt(fps_value);

				profile.videoFrameRate = fps;
			}
			catch(NumberFormatException exception) {

			}
		}

		return profile;
	}
	
	private static String formatFloatToString(final float f) {
		final int i=(int)f;
		if( f == i )
			return Integer.toString(i);
		return String.format(Locale.getDefault(), "%.2f", f);
	}

	private static int greatestCommonFactor(int a, int b) {
	    while( b > 0 ) {
	        int temp = b;
	        b = a % b;
	        a = temp;
	    }
	    return a;
	}
	
	private static String getAspectRatio(int width, int height) {
		int gcf = greatestCommonFactor(width, height);
		if( gcf > 0 ) {

			width /= gcf;
			height /= gcf;
		}
		return width + ":" + height;
	}
	
	public static String getMPString(int width, int height) {
		float mp = (width*height)/1000000.0f;
		return formatFloatToString(mp) + "MP";
	}
	
	public static String getAspectRatioMPString(int width, int height) {
		return "(" + getAspectRatio(width, height) + ", " + getMPString(width, height) + ")";
	}

	public double getTargetRatio() {
		return preview_targetRatio;
	}

	private double calculateTargetRatioForPreview(Point display_size) {
        double targetRatio = 0.0f;
		String preview_size = applicationInterface.getPreviewSizePref();
		if( preview_size.equals("preference_preview_size_wysiwyg") || this.is_video ) {
	        if( this.is_video ) {

	        	CamcorderProfile profile = getCamcorderProfile();

	        	targetRatio = ((double)profile.videoFrameWidth) / (double)profile.videoFrameHeight;
	        }
	        else {

	        	CameraController.Size picture_size = camera_controller.getPictureSize();

	        	targetRatio = ((double)picture_size.width) / (double)picture_size.height;
	        }
		}
		else {

        	targetRatio = ((double)display_size.x) / (double)display_size.y;
		}
		this.preview_targetRatio = targetRatio;

		return targetRatio;
	}

	public CameraController.Size getClosestSize(List<CameraController.Size> sizes, double targetRatio) {

		CameraController.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        for(CameraController.Size size : sizes) {
            double ratio = (double)size.width / size.height;
            if( Math.abs(ratio - targetRatio) < minDiff ) {
                optimalSize = size;
                minDiff = Math.abs(ratio - targetRatio);
            }
        }
        return optimalSize;
	}

	public CameraController.Size getOptimalPreviewSize(List<CameraController.Size> sizes) {

		final double ASPECT_TOLERANCE = 0.05;
        if( sizes == null )
        	return null;
        CameraController.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Point display_size = new Point();
		Activity activity = (Activity)this.getContext();
        {
            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getSize(display_size);

        }
        double targetRatio = calculateTargetRatioForPreview(display_size);
        int targetHeight = Math.min(display_size.y, display_size.x);
        if( targetHeight <= 0 ) {
            targetHeight = display_size.y;
        }

        for(CameraController.Size size : sizes) {

            double ratio = (double)size.width / size.height;
            if( Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE )
            	continue;
            if( Math.abs(size.height - targetHeight) < minDiff ) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if( optimalSize == null ) {
    		optimalSize = getClosestSize(sizes, targetRatio);
        }

        return optimalSize;
    }

	public CameraController.Size getOptimalVideoPictureSize(List<CameraController.Size> sizes, double targetRatio) {

		final double ASPECT_TOLERANCE = 0.05;
        if( sizes == null )
        	return null;
        CameraController.Size optimalSize = null;

        for(CameraController.Size size : sizes) {

            double ratio = (double)size.width / size.height;
            if( Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE )
            	continue;
            if( optimalSize == null || size.width > optimalSize.width ) {
                optimalSize = size;
            }
        }
        if( optimalSize == null ) {

    		optimalSize = getClosestSize(sizes, targetRatio);
        }

        return optimalSize;
    }

    private void setAspectRatio(double ratio) {
        if( ratio <= 0.0 )
        	throw new IllegalArgumentException();

        has_aspect_ratio = true;
        if( aspect_ratio != ratio ) {
        	aspect_ratio = ratio;

    		cameraSurface.getView().requestLayout();
    		if( canvasView != null ) {
    			canvasView.requestLayout();
    		}
        }
    }
    
    private boolean hasAspectRatio() {
    	return has_aspect_ratio;
    }

    private double getAspectRatio() {
    	return aspect_ratio;
    }

    public int getDisplayRotation() {
		Activity activity = (Activity)this.getContext();
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

		String rotate_preview = applicationInterface.getPreviewRotationPref();

		if( rotate_preview.equals("180") ) {
			switch (rotation) {
				case Surface.ROTATION_0: rotation = Surface.ROTATION_180; break;
				case Surface.ROTATION_90: rotation = Surface.ROTATION_270; break;
				case Surface.ROTATION_180: rotation = Surface.ROTATION_0; break;
				case Surface.ROTATION_270: rotation = Surface.ROTATION_90; break;
				default:
					break;
			}
		}


		return rotation;
    }
    
    public void setCameraDisplayOrientation() {

		if( camera_controller == null ) {
			return;
		}

	    if( using_android_l ) {
			configureTransform();
	    }
	    else {
		    int rotation = getDisplayRotation();
		    int degrees = 0;
		    switch (rotation) {
		    	case Surface.ROTATION_0: degrees = 0; break;
		        case Surface.ROTATION_90: degrees = 90; break;
		        case Surface.ROTATION_180: degrees = 180; break;
		        case Surface.ROTATION_270: degrees = 270; break;
	    		default:
	    			break;
		    }

			camera_controller.setDisplayOrientation(degrees);
	    }
	}

	private void onOrientationChanged(int orientation) {

		if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
			return;
		if( camera_controller == null ) {
			return;
		}
	    orientation = (orientation + 45) / 90 * 90;
	    this.current_orientation = orientation % 360;
	    int new_rotation = 0;
	    int camera_orientation = camera_controller.getCameraOrientation();
	    if( camera_controller.isFrontFacing() ) {
	    	new_rotation = (camera_orientation - orientation + 360) % 360;
	    }
	    else {
	    	new_rotation = (camera_orientation + orientation) % 360;
		}
		if (new_rotation != current_rotation ) {
	    	this.current_rotation = new_rotation;
	    }

		Log.d("Orient", String.valueOf(current_rotation));

	}


	private int getImageVideoRotation() {

		return this.current_rotation;
	}

	public void draw(Canvas canvas) {
		if( this.app_is_paused ) {
			return;
		}

		if( this.focus_success != FOCUS_DONE ) {
			if( focus_complete_time != -1 && System.currentTimeMillis() > focus_complete_time + 1000 ) {
				focus_success = FOCUS_DONE;
			}
		}
		applicationInterface.onDrawPreview(canvas);
	}

	public void scaleZoom(float scale_factor) {

		if( this.camera_controller != null && this.has_zoom ) {
			int zoom_factor = camera_controller.getZoom();
			float zoom_ratio = this.zoom_ratios.get(zoom_factor)/100.0f;
			zoom_ratio *= scale_factor;

			int new_zoom_factor = zoom_factor;
			if( zoom_ratio <= 1.0f ) {
				new_zoom_factor = 0;
			}
			else if( zoom_ratio >= zoom_ratios.get(max_zoom_factor)/100.0f ) {
				new_zoom_factor = max_zoom_factor;
			}
			else {

				if( scale_factor > 1.0f ) {
    				for(int i=zoom_factor;i<zoom_ratios.size();i++) {
    					if( zoom_ratios.get(i)/100.0f >= zoom_ratio ) {

    						new_zoom_factor = i;
    						break;
    					}
    				}
				}
				else {
    				for(int i=zoom_factor;i>=0;i--) {
    					if( zoom_ratios.get(i)/100.0f <= zoom_ratio ) {

    						new_zoom_factor = i;
    						break;
    					}
    				}
				}
			}

			zoomTo(new_zoom_factor);
		}
	}
	
	public void zoomTo(int new_zoom_factor) {

		if( new_zoom_factor < 0 )
			new_zoom_factor = 0;
		else if( new_zoom_factor > max_zoom_factor )
			new_zoom_factor = max_zoom_factor;
    	if (camera_controller != null ) {
			if( this.has_zoom ) {
				camera_controller.setZoom(new_zoom_factor);

	    		clearFocusAreas();
			}
        }
	}
	
	public void setFocusDistance(float new_focus_distance) {

		if( camera_controller != null ) {
			if( new_focus_distance < 0.0f )
				new_focus_distance = 0.0f;
			else if( new_focus_distance > minimum_focus_distance )
				new_focus_distance = minimum_focus_distance;
			if( camera_controller.setFocusDistance(new_focus_distance) ) {
				// now save

			}
		}
	}
	
	public void setExposure(int new_exposure) {

		if( camera_controller != null && ( min_exposure != 0 || max_exposure != 0 ) ) {
			cancelAutoFocus();
			if( new_exposure < min_exposure )
				new_exposure = min_exposure;
			else if( new_exposure > max_exposure )
				new_exposure = max_exposure;
			if( camera_controller.setExposureCompensation(new_exposure) ) {
				// now save
	    		showToast(seekbar_toast, getExposureCompensationString(new_exposure), 96);
			}
		}
	}
	
	public void setExposureTime(long new_exposure_time) {

		if( camera_controller != null && supports_exposure_time ) {
			if( new_exposure_time < min_exposure_time )
				new_exposure_time = min_exposure_time;
			else if( new_exposure_time > max_exposure_time )
				new_exposure_time = max_exposure_time;
			if( camera_controller.setExposureTime(new_exposure_time) ) {
				// now save
	    		showToast(seekbar_toast, getExposureTimeString(new_exposure_time), 96);
			}
		}
	}
	
	public String getExposureCompensationString(int exposure) {
		float exposure_ev = exposure * exposure_step;
		return getResources().getString(R.string.exposure_compensation) + " " + (exposure > 0 ? "+" : "") + decimal_format_2dp.format(exposure_ev) + " EV";
	}
	
	public String getISOString(int iso) {
		return getResources().getString(R.string.iso) + " " + iso;
	}

	public String getExposureTimeString(long exposure_time) {
		double exposure_time_s = exposure_time/1000000000.0;
		double exposure_time_r = 1.0/exposure_time_s;
		return " 1/" + decimal_format_1dp.format(exposure_time_r);
	}



	public boolean canSwitchCamera() {
		if( this.phase == PHASE_TAKING_PHOTO ) {

			return false;
		}
		int n_cameras = camera_controller_manager.getNumberOfCameras();

		if( n_cameras == 0 )
			return false;
		return true;
	}

	public void setCamera(int cameraId) {

		if( cameraId < 0 || cameraId >= camera_controller_manager.getNumberOfCameras() ) {

			cameraId = 0;
		}
		if( canSwitchCamera() ) {
			closeCamera();
			applicationInterface.setCameraIdPref(cameraId);
			this.openCamera();
		}
	}
	
	public int [] matchPreviewFpsToVideo(List<int []> fps_ranges, int video_frame_rate) {

		int selected_min_fps = -1, selected_max_fps = -1, selected_diff = -1;
        for(int [] fps_range : fps_ranges) {

			int min_fps = fps_range[0];
			int max_fps = fps_range[1];
			if( min_fps <= video_frame_rate && max_fps >= video_frame_rate ) {
    			int diff = max_fps - min_fps;
    			if( selected_diff == -1 || diff < selected_diff ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
    				selected_diff = diff;
    			}
			}
        }
        if( selected_min_fps != -1 ) {

        }
        else {
        	selected_diff = -1;
        	int selected_dist = -1;
            for(int [] fps_range : fps_ranges) {
    			int min_fps = fps_range[0];
    			int max_fps = fps_range[1];
    			int diff = max_fps - min_fps;
    			int dist = -1;
    			if( max_fps < video_frame_rate )
    				dist = video_frame_rate - max_fps;
    			else
    				dist = min_fps - video_frame_rate;

    			if( selected_dist == -1 || dist < selected_dist || ( dist == selected_dist && diff < selected_diff ) ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
    				selected_dist = dist;
    				selected_diff = diff;
    			}
            }

        }
    	return new int[]{selected_min_fps, selected_max_fps};
	}

	public int [] chooseBestPreviewFps(List<int []> fps_ranges) {

		int selected_min_fps = -1, selected_max_fps = -1;
        for(int [] fps_range : fps_ranges) {

			int min_fps = fps_range[0];
			int max_fps = fps_range[1];
			if( max_fps >= 30000 ) {
				if( selected_min_fps == -1 || min_fps < selected_min_fps ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
				}
				else if( min_fps == selected_min_fps && max_fps > selected_max_fps ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
				}
			}
        }

        if( selected_min_fps != -1 ) {

        }
        else {
        	int selected_diff = -1;
            for(int [] fps_range : fps_ranges) {
    			int min_fps = fps_range[0];
    			int max_fps = fps_range[1];
    			int diff = max_fps - min_fps;
    			if( selected_diff == -1 || diff > selected_diff ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
    				selected_diff = diff;
    			}
    			else if( diff == selected_diff && max_fps > selected_max_fps ) {
    				selected_min_fps = min_fps;
    				selected_max_fps = max_fps;
    				selected_diff = diff;
    			}
            }

        }
    	return new int[]{selected_min_fps, selected_max_fps};
	}


	private void setPreviewFps() {

		CamcorderProfile profile = getCamcorderProfile();
		List<int []> fps_ranges = camera_controller.getSupportedPreviewFpsRange();
		if( fps_ranges == null || fps_ranges.size() == 0 ) {

			return;
		}
		int [] selected_fps = null;
		if( this.is_video ) {
			boolean preview_too_dark = Build.MODEL.equals("Nexus 5") || Build.MODEL.equals("Nexus 6");
			String fps_value = applicationInterface.getVideoFPSPref();

			if( fps_value.equals("default") && preview_too_dark ) {
				selected_fps = chooseBestPreviewFps(fps_ranges);
			}
			else {
				selected_fps = matchPreviewFpsToVideo(fps_ranges, profile.videoFrameRate*1000);
			}
		}
		else {
			selected_fps = chooseBestPreviewFps(fps_ranges);
		}
        camera_controller.setPreviewFpsRange(selected_fps[0], selected_fps[1]);
	}
	
	public void switchVideo(boolean during_startup) {

		if( camera_controller == null ) {

			return;
		}
		boolean old_is_video = is_video;

		if( this.is_video ) {
			if( video_recorder != null ) {
				stopVideo(false);
			}
			this.is_video = false;
		}
		else {
			if( this.isOnTimer() ) {
				cancelTimer();
				this.is_video = true;
			}

			else if( this.phase == PHASE_TAKING_PHOTO ) {
				// wait until photo taken

			}
			else {
				this.is_video = true;
			}
		}
		
		if( is_video != old_is_video ) {
			setFocusPref(false);
			updateFocusForVideo(false);


			if( !during_startup ) {
				applicationInterface.setVideoPref(is_video);
	    	}
			
			if( !during_startup ) {
				String focus_value = current_focus_index != -1 ? supported_focus_values.get(current_focus_index) : null;

				if( !is_video && focus_value != null && focus_value.equals("focus_mode_continuous_picture") ) {

					this.onPause();
					this.onResume();
				}
				else {
					if( this.is_preview_started ) {
						camera_controller.stopPreview();
						this.is_preview_started = false;
					}
					setPreviewSize();
			        this.startCameraPreview();
				}
			}

		}
	}
	
	public boolean focusIsVideo() {
		if( camera_controller != null ) {
			return camera_controller.focusIsVideo();
		}
		return false;
	}
	
	private void setFocusPref(boolean auto_focus) {

		String focus_value = applicationInterface.getFocusPref(is_video);
		if( focus_value.length() > 0 ) {

			if( !updateFocus(focus_value, true, false, auto_focus) ) {
				updateFocus(0, true, true, auto_focus);
			}
		}
		else {
			updateFocus(is_video ? "focus_mode_continuous_video" : "focus_mode_auto", true, true, auto_focus);
		}
	}

	public void updateFocusForVideo(boolean auto_focus) {
		if( this.supported_focus_values != null && camera_controller != null && is_video ) { // originally we reset focus mode for photo mode too, but now we only do this for video mode (so if user wants to use continuous video mode for photo mode, that's fine, and we don't reset it)
			boolean focus_is_video = focusIsVideo();

			if( focus_is_video != is_video ) {

				updateFocus(is_video ? "focus_mode_continuous_video" : "focus_mode_auto", true, true, auto_focus);
			}
		}
	}
	
	public String getErrorFeatures(CamcorderProfile profile) {
		boolean was_4k = false, was_bitrate = false, was_fps = false;
		if( profile.videoFrameWidth == 3840 && profile.videoFrameHeight == 2160 && applicationInterface.getForce4KPref() ) {
			was_4k = true;
		}
		String bitrate_value = applicationInterface.getVideoBitratePref();
		if( !bitrate_value.equals("default") ) {
			was_bitrate = true;
		}
		String fps_value = applicationInterface.getVideoFPSPref();
		if( !fps_value.equals("default") ) {
			was_fps = true;
		}
		String features = "";
		if( was_4k || was_bitrate || was_fps ) {
			if( was_4k ) {
				features = "4K UHD";
			}
			if( was_bitrate ) {
				if( features.length() == 0 )
					features = "Bitrate";
				else
					features += "/Bitrate";
			}
			if( was_fps ) {
				if( features.length() == 0 )
					features = "Frame rate";
				else
					features += "/Frame rate";
			}
		}
		return features;
	}

	public void updateFlash(String focus_value) {

		if( this.phase == PHASE_TAKING_PHOTO && !is_video ) {

			return;
		}
		updateFlash(focus_value, true);
	}

	private boolean updateFlash(String flash_value, boolean save) {

		if( supported_flash_values != null ) {
	    	int new_flash_index = supported_flash_values.indexOf(flash_value);

	    	if( new_flash_index != -1 ) {
	    		updateFlash(new_flash_index, save);
	    		return true;
	    	}
		}
    	return false;
	}
	
	private void updateFlash(int new_flash_index, boolean save) {

		if( supported_flash_values != null && new_flash_index != current_flash_index ) {
			boolean initial = current_flash_index==-1;
			current_flash_index = new_flash_index;


	    	String [] flash_entries = getResources().getStringArray(R.array.flash_entries);
			String flash_value = supported_flash_values.get(current_flash_index);

	    	String [] flash_values = getResources().getStringArray(R.array.flash_values);
	    	for(int i=0;i<flash_values.length;i++) {

	    		if( flash_value.equals(flash_values[i]) ) {

	    			if( !initial ) {
	    				showToast(flash_toast, flash_entries[i]);
	    			}
	    			break;
	    		}
	    	}
	    	this.setFlash(flash_value);
	    	if( save ) {
	    		applicationInterface.setFlashPref(flash_value);
	    	}
		}
	}

	private void setFlash(String flash_value) {

		set_flash_value_after_autofocus = "";
		if( camera_controller == null ) {

			return;
		}
		cancelAutoFocus();
        camera_controller.setFlashValue(flash_value);
	}

    public String getCurrentFlashValue() {
    	if( this.current_flash_index == -1 )
    		return null;
    	return this.supported_flash_values.get(current_flash_index);
    }

	public void updateFocus(String focus_value, boolean quiet, boolean auto_focus) {

		if( this.phase == PHASE_TAKING_PHOTO ) {
			// just to be safe - otherwise problem that changing the focus mode will cancel the autofocus before taking a photo, so we never take a photo, but is_taking_photo remains true!
			return;
		}
		updateFocus(focus_value, quiet, true, auto_focus);
	}

	private boolean updateFocus(String focus_value, boolean quiet, boolean save, boolean auto_focus) {

		if( this.supported_focus_values != null ) {
	    	int new_focus_index = supported_focus_values.indexOf(focus_value);

	    	if( new_focus_index != -1 ) {
	    		updateFocus(new_focus_index, quiet, save, auto_focus);
	    		return true;
	    	}
		}
    	return false;
	}

	private String findEntryForValue(String value, int entries_id, int values_id) {
    	String [] entries = getResources().getStringArray(entries_id);
    	String [] values = getResources().getStringArray(values_id);
    	for(int i=0;i<values.length;i++) {

    		if( value.equals(values[i]) ) {

				return entries[i];
    		}
    	}
    	return null;
	}
	
	public String findFocusEntryForValue(String focus_value) {
		return findEntryForValue(focus_value, R.array.focus_mode_entries, R.array.focus_mode_values);
	}
	
	private void updateFocus(int new_focus_index, boolean quiet, boolean save, boolean auto_focus) {

		if( this.supported_focus_values != null && new_focus_index != current_focus_index ) {
			current_focus_index = new_focus_index;

			String focus_value = supported_focus_values.get(current_focus_index);

			if( !quiet ) {
				String focus_entry = findFocusEntryForValue(focus_value);
				if (focus_entry != null ) {
    				showToast(focus_toast, focus_entry);
				}
			}
	    	this.setFocusValue(focus_value, auto_focus);

	    	if( save ) {
				// now save
	    		applicationInterface.setFocusPref(focus_value, is_video);
	    	}
		}
	}

	public String getCurrentFocusValue() {

		if( camera_controller == null ) {
			return null;
		}
		if( this.supported_focus_values != null && this.current_focus_index != -1 )
			return this.supported_focus_values.get(current_focus_index);
		return null;
	}

	private void setFocusValue(String focus_value, boolean auto_focus) {
		if( camera_controller == null ) {
			return;
		}
		cancelAutoFocus();
        camera_controller.setFocusValue(focus_value);
		setupContinuousFocusMove();
		clearFocusAreas();
		if( auto_focus && !focus_value.equals("focus_mode_locked") ) {
			tryAutoFocus(false, false);
		}
	}
	
	private void setupContinuousFocusMove() {
		if( continuous_focus_move_is_started ) {
			continuous_focus_move_is_started = false;
			applicationInterface.onContinuousFocusMove(false);
		}
		String focus_value = current_focus_index != -1 ? supported_focus_values.get(current_focus_index) : null;

		if( camera_controller != null && focus_value != null && focus_value.equals("focus_mode_continuous_picture") && !this.is_video) {

			camera_controller.setContinuousFocusMoveCallback(new CameraController.ContinuousFocusMoveCallback() {
				@Override
				public void onContinuousFocusMove(boolean start) {
					if (start != continuous_focus_move_is_started) { // filter out repeated calls with same start value
						continuous_focus_move_is_started = start;
						count_cameraContinuousFocusMoving++;
						applicationInterface.onContinuousFocusMove(start);
					}
				}
			});
		}
		else if( camera_controller != null ) {

			camera_controller.setContinuousFocusMoveCallback(null);
		}
	}

	public void toggleExposureLock() {

		if( camera_controller == null ) {

			return;
		}
		if( is_exposure_lock_supported ) {
			is_exposure_locked = !is_exposure_locked;
			cancelAutoFocus();
	        camera_controller.setAutoExposureLock(is_exposure_locked);
		}
	}


	public void takePicturePressed() {

		if( camera_controller == null ) {


			this.phase = PHASE_NORMAL;
			return;
		}
		if( !this.has_surface ) {

			this.phase = PHASE_NORMAL;
			return;
		}

		if( this.isOnTimer() ) {
			cancelTimer();
		    showToast(take_photo_toast, R.string.cancelled_timer);
			return;
		}

		if( this.phase == PHASE_TAKING_PHOTO ) {
    		if( is_video ) {
				stopVideo(false);

    			if( !video_start_time_set || System.currentTimeMillis() - video_start_time < 500 ) {

    			}

    			else {
//    				stopVideo(false);
    			}
    		}
    		else {
    			if( remaining_burst_photos != 0 ) {
    				remaining_burst_photos = 0;
    			    showToast(take_photo_toast, R.string.cancelled_burst_mode);
    			}
    		}
    		return;
    	}

        this.startCameraPreview();

		long timer_delay = applicationInterface.getTimerPref();

		String burst_mode_value = applicationInterface.getRepeatPref();
		int n_burst = 1;
		if( burst_mode_value.equals("unlimited") ) {

			n_burst = -1;
			remaining_burst_photos = -1;
		}
		else {
			try {
				n_burst = Integer.parseInt(burst_mode_value);

			}
	        catch(NumberFormatException e) {

	    		e.printStackTrace();
	    		n_burst = 1;
	        }
			remaining_burst_photos = n_burst-1;
		}
		
		if( timer_delay == 0 ) {
			takePicture(false);
		}
		else {
			takePictureOnTimer(timer_delay, false);
		}

	}
	
	private void takePictureOnTimer(final long timer_delay, boolean repeated) {

        this.phase = PHASE_TIMER;
		class TakePictureTimerTask extends TimerTask {
			public void run() {
				if( beepTimerTask != null ) {
					beepTimerTask.cancel();
					beepTimerTask = null;
				}
				Activity activity = (Activity)Preview.this.getContext();
				activity.runOnUiThread(new Runnable() {
					public void run() {
						// we run on main thread to avoid problem of camera closing at the same time
						// but still need to check that the camera hasn't closed or the task halted, since TimerTask.run() started
						if( camera_controller != null && takePictureTimerTask != null )
							takePicture(false);

					}
				});
			}
		}
		take_photo_time = System.currentTimeMillis() + timer_delay;


    	takePictureTimer.schedule(takePictureTimerTask = new TakePictureTimerTask(), timer_delay);

		class BeepTimerTask extends TimerTask {
			long remaining_time = timer_delay;

			public void run() {
				remaining_time -= 1000;
			}
		}
    	beepTimer.schedule(beepTimerTask = new BeepTimerTask(), 0, 1000);
	}
	
	private void flashVideo() {

		String flash_value = camera_controller.getFlashValue();
		if( flash_value.length() == 0 )
			return;
		String flash_value_ui = getCurrentFlashValue();
		if( flash_value_ui == null )
			return;
		if( flash_value_ui.equals("flash_torch") )
			return;
		if( flash_value.equals("flash_torch") ) {
			cancelAutoFocus();
	        camera_controller.setFlashValue(flash_value_ui);
			return;
		}
		cancelAutoFocus();
        camera_controller.setFlashValue("flash_torch");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cancelAutoFocus();
        camera_controller.setFlashValue(flash_value_ui);
	}

	private void onVideoInfo(int what, int extra) {
		boolean restart_on_max_filesize = applicationInterface.getVideoRestartMaxFileSizePref();
		if( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED && restart_on_max_filesize ) {

			Activity activity = (Activity)Preview.this.getContext();
			activity.runOnUiThread(new Runnable() {
				public void run() {
					if( camera_controller != null )
						restartVideo(true);

				}
			});
		}
		else if( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED || what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED ) {
			stopVideo(false);
		}
		applicationInterface.onVideoInfo(what, extra); // call this last, so that toasts show up properly (as we're hogging the UI thread here, and mediarecorder takes time to stop)
	}
	
	private void onVideoError(int what, int extra) {
		stopVideo(false);
		applicationInterface.onVideoError(what, extra); // call this last, so that toasts show up properly (as we're hogging the UI thread here, and mediarecorder takes time to stop)
	}

	private void takePicture(boolean max_filesize_restart) {


        this.phase = PHASE_TAKING_PHOTO;
		this.take_photo_after_autofocus = false;
		if( camera_controller == null ) {

			this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
			return;
		}
		if( !this.has_surface ) {

			this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
			return;
		}

		if( is_video ) {

//			camera_controller.setDisplayOrientation(current_rotation + 90); //HereRotation

    		startVideoRecording(max_filesize_restart);
        	return;
		}

		takePhoto(false);

	}
	

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void startVideoRecording(boolean max_filesize_restart) {
		focus_success = FOCUS_DONE;

		boolean created_video_file = false;
		video_method = ApplicationInterface.VIDEOMETHOD_FILE;
		video_uri = null;
		video_filename = null;
		ParcelFileDescriptor pfd_saf = null;
		try {
			video_method = applicationInterface.createOutputVideoMethod();

    		if( video_method == ApplicationInterface.VIDEOMETHOD_FILE ) {
    			File videoFile = applicationInterface.createOutputVideoFile();
				video_filename = videoFile.getAbsolutePath();
				created_video_file = true;

    		}
    		else {
	    		if( video_method == ApplicationInterface.VIDEOMETHOD_SAF ) {
	    			video_uri = applicationInterface.createOutputVideoSAF();
	    		}
	    		else {
	    			video_uri = applicationInterface.createOutputVideoUri();
	    		}
    			created_video_file = true;

	    		pfd_saf = getContext().getContentResolver().openFileDescriptor(video_uri, "rw");
    		}
		}
		catch(IOException e) {
			e.printStackTrace();
            applicationInterface.onFailedCreateVideoFileError();
			this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
		}

		if( created_video_file ) {
        	CamcorderProfile profile = getCamcorderProfile();

    		video_recorder = new MediaRecorder();
    		this.camera_controller.unlock();

        	video_recorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
				@Override
				public void onInfo(MediaRecorder mr, int what, int extra) {

					final int final_what = what;
					final int final_extra = extra;
					Activity activity = (Activity) Preview.this.getContext();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// we run on main thread to avoid problem of camera closing at the same time
							onVideoInfo(final_what, final_extra);
						}
					});
				}
			});
        	video_recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
				public void onError(MediaRecorder mr, int what, int extra) {
					final int final_what = what;
					final int final_extra = extra;
					Activity activity = (Activity) Preview.this.getContext();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							// we run on main thread to avoid problem of camera closing at the same time
							onVideoError(final_what, final_extra);
						}
					});
				}
			});

			camera_controller.initVideoRecorderPrePrepare(video_recorder);

			int audio_source = MediaRecorder.AudioSource.DEFAULT;

			video_recorder.setAudioSource(audio_source);
			video_recorder.setVideoSource(using_android_l ? MediaRecorder.VideoSource.SURFACE : MediaRecorder.VideoSource.CAMERA);
			video_recorder.setProfile(profile);

			if (PipeRecorder.recordHDValue) {
				video_recorder.setVideoSize(1280, 720);
			} else {
				video_recorder.setVideoSize(720, 480);
			}

			long video_max_filesize = applicationInterface.getVideoMaxFileSizePref();
			if( video_max_filesize > 0 ) {
	    		video_recorder.setMaxFileSize(video_max_filesize);
			}

    		if( video_method == ApplicationInterface.VIDEOMETHOD_FILE ) {
    			video_recorder.setOutputFile(video_filename);
    		}
    		else {
    			video_recorder.setOutputFile(pfd_saf.getFileDescriptor());
    		}
        	try {
        		applicationInterface.cameraInOperation(true);
        		applicationInterface.startingVideo();

    			cameraSurface.setVideoRecorder(video_recorder);

				if (getImageVideoRotation() == 0) {
					video_recorder.setOrientationHint(getImageVideoRotation() + 90);
				} else if (getImageVideoRotation() == 180) {
					video_recorder.setOrientationHint(getImageVideoRotation() - 90);
				} else {
					video_recorder.setOrientationHint(getImageVideoRotation());
				}

				video_recorder.prepare();
	        	camera_controller.initVideoRecorderPostPrepare(video_recorder);

            	video_recorder.start();

				if( test_video_failure ) {

					throw new RuntimeException();
				}
            	video_start_time = System.currentTimeMillis();
            	video_start_time_set = true;

				if( remaining_restart_video == 0 && !max_filesize_restart ) {
					remaining_restart_video = applicationInterface.getVideoRestartTimesPref();
				}


				long video_max_duration = applicationInterface.getVideoMaxDurationPref();

				if( max_filesize_restart ) {
					if( video_max_duration > 0 ) {
    					video_max_duration -= video_accumulated_time;
    					if( video_max_duration < min_safe_restart_video_time ) {

    			    		video_max_duration = min_safe_restart_video_time;
    					}
					}
				}
				else {
    				video_accumulated_time = 0;
				}

				if( video_max_duration > 0 ) {
					class RestartVideoTimerTask extends TimerTask {
    					public void run() {

    						Activity activity = (Activity)Preview.this.getContext();
    						activity.runOnUiThread(new Runnable() {
    							public void run() {
    								if( camera_controller != null && restartVideoTimerTask != null )
    									restartVideo(false);

    							}
    						});
    					}
    				}
    		    	restartVideoTimer.schedule(restartVideoTimerTask = new RestartVideoTimerTask(), video_max_duration);
				}

				if( applicationInterface.getVideoFlashPref() && supportsFlash() ) {
					class FlashVideoTimerTask extends TimerTask {
    					public void run() {

    						Activity activity = (Activity)Preview.this.getContext();
    						activity.runOnUiThread(new Runnable() {
    							public void run() {
    								if( camera_controller != null && flashVideoTimerTask != null )
    									flashVideo();
    								else {

    								}
    							}
    						});
    					}
					}
    		    	flashVideoTimer.schedule(flashVideoTimerTask = new FlashVideoTimerTask(), 0, 1000);
				}
			}
        	catch(IOException e) {
				e.printStackTrace();
	    	    applicationInterface.onFailedCreateVideoFileError();
	    		video_recorder.reset();
	    		video_recorder.release(); 
	    		video_recorder = null;
				this.phase = PHASE_NORMAL;
				applicationInterface.cameraInOperation(false);
				this.reconnectCamera(true);
			}
        	catch(RuntimeException e) {
        		e.printStackTrace();
				failedToStartVideoRecorder(profile);
			}
        	catch(CameraControllerException e) {

				e.printStackTrace();
				failedToStartVideoRecorder(profile);
			}
		}
	}
	
	private void failedToStartVideoRecorder(CamcorderProfile profile) {
		applicationInterface.onVideoRecordStartError(profile);
		video_recorder.reset();
		video_recorder.release(); 
		video_recorder = null;
		this.phase = PHASE_NORMAL;
		applicationInterface.cameraInOperation(false);
		this.reconnectCamera(true);
	}

	private void takePhoto(boolean skip_autofocus) {

		applicationInterface.cameraInOperation(true);
		String focus_value = current_focus_index != -1 ? supported_focus_values.get(current_focus_index) : null;

		if( focus_value != null && ( focus_value.equals("focus_mode_continuous_picture") || focus_value.equals("focus_mode_continuous_video") ) ) {
			CameraController.AutoFocusCallback autoFocusCallback = new CameraController.AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success) {
					takePhotoWhenFocused();
				}
	        };
			camera_controller.autoFocus(autoFocusCallback);
		}
		else if( this.recentlyFocused() || skip_autofocus ) {

			takePhotoWhenFocused();
		}

		else if( focus_value != null && ( focus_value.equals("focus_mode_auto") || focus_value.equals("focus_mode_macro") ) ) {
			synchronized(this) {
				if( focus_success == FOCUS_WAITING ) {
					take_photo_after_autofocus = true;
				}
				else {
					focus_success = FOCUS_DONE;
			        CameraController.AutoFocusCallback autoFocusCallback = new CameraController.AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success) {

							ensureFlashCorrect();
							takePhotoWhenFocused();
						}
			        };

					camera_controller.autoFocus(autoFocusCallback);
					count_cameraAutoFocus++;
				}
			}
		}
		else {
			takePhotoWhenFocused();
		}
	}

	private void takePhotoWhenFocused() {

		if( camera_controller == null ) {

			this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
			return;
		}
		if( !this.has_surface ) {

			this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
			return;
		}

		final String focus_value = current_focus_index != -1 ? supported_focus_values.get(current_focus_index) : null;

		if( focus_value != null && focus_value.equals("focus_mode_locked") && focus_success == FOCUS_WAITING ) {
			cancelAutoFocus();
		}

		focus_success = FOCUS_DONE;
		successfully_focused = false;

		CameraController.PictureCallback pictureCallback = new CameraController.PictureCallback() {
			private boolean success = false; // whether jpeg callback succeeded

			public void onCompleted() {

    	        if( !using_android_l ) {
    	        	is_preview_started = false;
    	        }// need to set this even if remaining burst photos, so we can restart the preview
    	        if( remaining_burst_photos == -1 || remaining_burst_photos > 0 ) {
    	        	if( !is_preview_started ) {
    	    	    	startCameraPreview();
    	        	}
    	        }
    	        else {
    		        phase = PHASE_NORMAL;

    				if(success ) {
    					if( is_preview_started ) {

    						camera_controller.stopPreview();
    						is_preview_started = false;
    					}
    	    			setPreviewPaused(true);
    				}
    				else {
    	            	if( !is_preview_started ) {
    		    	    	startCameraPreview();
    	            	}
    	        		applicationInterface.cameraInOperation(false);

    				}
    	        }
    			if( camera_controller != null && focus_value != null && ( focus_value.equals("focus_mode_continuous_picture") || focus_value.equals("focus_mode_continuous_video") ) ) {

    				camera_controller.cancelAutoFocus(); // needed to restart continuous focusing
    			}

    	        if( remaining_burst_photos == -1 || remaining_burst_photos > 0 ) {
    	        	if( remaining_burst_photos > 0 )
    	        		remaining_burst_photos--;
    	        }
			}


    	};
		CameraController.ErrorCallback errorCallback = new CameraController.ErrorCallback() {
			public void onError() {

        		count_cameraTakePicture--;
				phase = PHASE_NORMAL;
	            startCameraPreview();
	    		applicationInterface.cameraInOperation(false);
    	    }
		};

    }

    private void tryAutoFocus(final boolean startup, final boolean manual) {
    	if( camera_controller == null ) {

		}
		else if( !this.has_surface ) {

		}
		else if( !this.is_preview_started ) {

		}

		else if( !(manual && this.is_video) && this.isTakingPhotoOrOnTimer() ) {

		}
		else {

	        if( camera_controller.supportsAutoFocus() ) {

				if( !using_android_l ) {
					set_flash_value_after_autofocus = "";
					String old_flash_value = camera_controller.getFlashValue();
	    			// getFlashValue() may return "" if flash not supported!
					if( startup && old_flash_value.length() > 0 && !old_flash_value.equals("flash_off") && !old_flash_value.equals("flash_torch") ) {
	    				set_flash_value_after_autofocus = old_flash_value;
	        			camera_controller.setFlashValue("flash_off");
	    			}

				}
    			CameraController.AutoFocusCallback autoFocusCallback = new CameraController.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success) {

						autoFocusCompleted(manual, success, false);
					}
		        };
	
				this.focus_success = FOCUS_WAITING;

	    		this.focus_complete_time = -1;
	    		this.successfully_focused = false;
    			camera_controller.autoFocus(autoFocusCallback);
    			count_cameraAutoFocus++;
    			this.focus_started_time = System.currentTimeMillis();

	        }
	        else if( has_focus_area ) {

				focus_success = FOCUS_SUCCESS;
				focus_complete_time = System.currentTimeMillis();

	        }
		}
    }
    
    private void cancelAutoFocus() {

        if( camera_controller != null ) {
			camera_controller.cancelAutoFocus();
    		autoFocusCompleted(false, false, true);
        }
    }
    
    private void ensureFlashCorrect() {

		if( set_flash_value_after_autofocus.length() > 0 && camera_controller != null ) {

			camera_controller.setFlashValue(set_flash_value_after_autofocus);
			set_flash_value_after_autofocus = "";
		}
    }
    
    private void autoFocusCompleted(boolean manual, boolean success, boolean cancelled) {

		if( cancelled ) {
			focus_success = FOCUS_DONE;
		}
		else {
			focus_success = success ? FOCUS_SUCCESS : FOCUS_FAILED;
			focus_complete_time = System.currentTimeMillis();
		}
		if( manual && !cancelled ) {
			successfully_focused = true;
			successfully_focused_time = focus_complete_time;
		}
		ensureFlashCorrect();
		if( this.using_face_detection && !cancelled ) {
			if( camera_controller != null ) {
				camera_controller.cancelAutoFocus();
			}
		}
		synchronized(this) {
			if( take_photo_after_autofocus ) {

				take_photo_after_autofocus = false;
				takePhotoWhenFocused();
			}
		}

    }
    
    public void startCameraPreview() {

		//if( camera != null && !is_taking_photo && !is_preview_started ) {
		if( camera_controller != null && !this.isTakingPhotoOrOnTimer() && !is_preview_started ) {

			{
				camera_controller.setRecordingHint(this.is_video);
			}
			setPreviewFps();
    		try {
    			camera_controller.startPreview();
		    	count_cameraStartPreview++;
    		}
    		catch(CameraControllerException e) {
    			e.printStackTrace();
    			applicationInterface.onFailedStartPreview();
    			return;
    		}
			this.is_preview_started = true;

			if( this.using_face_detection ) {
				camera_controller.startFaceDetection();
				faces_detected = null;
			}
		}
		this.setPreviewPaused(false);
		this.setupContinuousFocusMove();

    }

    private void setPreviewPaused(boolean paused) {

	    if( paused ) {
	    	this.phase = PHASE_PREVIEW_PAUSED;
		}
		else {
	    	this.phase = PHASE_NORMAL;
			applicationInterface.cameraInOperation(false);
		}
    }
    
    public boolean hasLevelAngle() {
    	return this.has_level_angle;
    }
    
    public double getLevelAngle() {
    	return this.level_angle;
    }
    
    public double getOrigLevelAngle() {
    	return this.orig_level_angle;
    }

    public void onMagneticSensorChanged(SensorEvent event) {
    	this.has_geomagnetic = true;
    	for(int i=0;i<3;i++) {
    		//this.geomagnetic[i] = event.values[i];
    		this.geomagnetic[i] = sensor_alpha * this.geomagnetic[i] + (1.0f-sensor_alpha) * event.values[i];
    	}
    	calculateGeoDirection();
    }
    
    private void calculateGeoDirection() {
    	if( !this.has_gravity || !this.has_geomagnetic ) {
    		return;
    	}
    	if( !SensorManager.getRotationMatrix(this.deviceRotation, this.deviceInclination, this.gravity, this.geomagnetic) ) {
    		return;
    	}
        SensorManager.remapCoordinateSystem(this.deviceRotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, this.cameraRotation);
    	this.has_geo_direction = true;
    	SensorManager.getOrientation(cameraRotation, geo_direction);

    }
    
    public CameraController.Size getCurrentPictureSize() {
    	if( current_size_index == -1 || sizes == null )
    		return null;
    	return sizes.get(current_size_index);
    }
    
	public List<String> getSupportedFlashValues() {
		return supported_flash_values;
	}

    public int getCameraId() {
        if( camera_controller == null )
			return 0;
        return camera_controller.getCameraId();
    }
    
    public void onResume() {

		this.app_is_paused = false;
		this.openCamera();
    }

    public void onPause() {

		this.app_is_paused = true;
		this.closeCamera();
    }
    

	public void onSaveInstanceState(Bundle state) {

	}

	public void showToast(final ToastBoxer clear_toast, final int message_id) {
    	showToast(clear_toast, getResources().getString(message_id));
    }

    public void showToast(final ToastBoxer clear_toast, final String message) {
    	showToast(clear_toast, message, 32);
    }

    public void showToast(final ToastBoxer clear_toast, final String message, final int offset_y_dp) {
		if( !applicationInterface.getShowToastsPref() ) {
			return;
		}
    	
		class RotatedTextView extends View {
			private String [] lines = null;
			private Paint paint = new Paint();
			private Rect bounds = new Rect();
			private Rect sub_bounds = new Rect();
			private RectF rect = new RectF();

			public RotatedTextView(String text, Context context) {
				super(context);

				this.lines = text.split("\n");
			}

			@Override 
			protected void onDraw(Canvas canvas) {
				final float scale = Preview.this.getResources().getDisplayMetrics().density;
				paint.setTextSize(14 * scale + 0.5f); // convert dps to pixels
				paint.setShadowLayer(1, 0, 1, Color.BLACK);
				boolean first_line = true;

				for(String line : lines) {
					paint.getTextBounds(line, 0, line.length(), sub_bounds);

					if( first_line ) {
						bounds.set(sub_bounds);
						first_line = false;
					}
					else {
						bounds.top = Math.min(sub_bounds.top, bounds.top);
						bounds.bottom = Math.max(sub_bounds.bottom, bounds.bottom);
						bounds.left = Math.min(sub_bounds.left, bounds.left);
						bounds.right = Math.max(sub_bounds.right, bounds.right);
					}
				}

				int height = bounds.bottom - bounds.top + 2;
				bounds.bottom += ((lines.length-1) * height)/2;
				bounds.top -= ((lines.length-1) * height)/2;
				final int padding = (int) (14 * scale + 0.5f);
				final int offset_y = (int) (offset_y_dp * scale + 0.5f);
				canvas.save();
				canvas.rotate(ui_rotation, canvas.getWidth()/2.0f, canvas.getHeight()/2.0f);

				rect.left = canvas.getWidth()/2 - bounds.width()/2 + bounds.left - padding;
				rect.top = canvas.getHeight()/2 + bounds.top - padding + offset_y;
				rect.right = canvas.getWidth()/2 - bounds.width()/2 + bounds.right + padding;
				rect.bottom = canvas.getHeight()/2 + bounds.bottom + padding + offset_y;

				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.rgb(50, 50, 50));
				//canvas.drawRect(rect, paint);
				final float radius = (24 * scale + 0.5f);
				canvas.drawRoundRect(rect, radius, radius, paint);

				paint.setColor(Color.WHITE);
				int ypos = canvas.getHeight()/2 + offset_y - ((lines.length-1) * height)/2;
				for(String line : lines) {
					canvas.drawText(line, canvas.getWidth()/2 - bounds.width()/2, ypos, paint);
					ypos += height;
				}
				canvas.restore();
			} 
		}

		final Activity activity = (Activity)this.getContext();
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Toast toast = null;
				if( clear_toast != null && clear_toast.toast != null && clear_toast.toast == last_toast ) {
					toast = clear_toast.toast;
				}
				else {
					if( clear_toast != null && clear_toast.toast != null ) {

						clear_toast.toast.cancel();
					}
					toast = new Toast(activity);

					if( clear_toast != null )
						clear_toast.toast = toast;
				}
				View text = new RotatedTextView(message, activity);
				toast.setView(text);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();
				last_toast = toast;
			}
		});
	}

	
	public int getUIRotation() {
		return this.ui_rotation;
	}


	public boolean isVideo() {
		return is_video;
	}
	
	public boolean isVideoRecording() {
		return video_recorder != null && video_start_time_set;
	}

    public CameraController getCameraController() {
    	return this.camera_controller;
    }
    
    public CameraControllerManager getCameraControllerManager() {
    	return this.camera_controller_manager;
    }

    public boolean supportsFlash() {
    	return this.supported_flash_values != null;
    }
    
    public boolean isTakingPhotoOrOnTimer() {
    	return this.phase == PHASE_TAKING_PHOTO || this.phase == PHASE_TIMER;
    }
    
    public boolean isOnTimer() {
    	return this.phase == PHASE_TIMER;
    }

    public boolean isPreviewPaused() {
    	return this.phase == PHASE_PREVIEW_PAUSED;
    }

    private boolean recentlyFocused() {
    	return this.successfully_focused && System.currentTimeMillis() < this.successfully_focused_time + 5000;
    }
}
