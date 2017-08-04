package com.android.pipe.pipeandroidsdk;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ZoomControls;

public class MainUI {
	private static final String TAG = "MainUI";

	private RecordVideoActivity main_activity = null;

	private boolean popup_view_is_open = false;
    private PopupView popup_view = null;

    private int current_orientation = 0;
	private boolean ui_placement_right = true;

	private boolean immersive_mode = false;
    private boolean show_gui = true;

	public MainUI(RecordVideoActivity main_activity) {
		this.main_activity = main_activity;
	}


    public void setTakePhotoIcon() {
		if( main_activity.getPreview() != null ) {
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);
			int resource = 0;
			int content_description = 0;
			if( main_activity.getPreview().isVideo() ) {
				resource = main_activity.getPreview().isVideoRecording() ? R.drawable.take_video_recording : R.drawable.take_video_selector;
				content_description = main_activity.getPreview().isVideoRecording() ? R.string.stop_video : R.string.start_video;
			}
			else {
				resource = R.drawable.take_photo_selector;
				content_description = R.string.take_photo;
			}
			view.setImageResource(resource);
			view.setContentDescription( main_activity.getResources().getString(content_description) );
			view.setTag(resource); // for testing
		}
    }


    public void setSwitchCameraContentDescription() {
		if( main_activity.getPreview() != null && main_activity.getPreview().canSwitchCamera() ) {
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.switch_camera);
			int content_description = 0;
			int cameraId = main_activity.getNextCameraId();
		    if( main_activity.getPreview().getCameraControllerManager().isFrontFacing( cameraId ) ) {
				content_description = R.string.switch_to_front_camera;
		    }
		    else {
				content_description = R.string.switch_to_back_camera;
		    }
			view.setContentDescription( main_activity.getResources().getString(content_description) );
		}
    }

    public boolean getUIPlacementRight() {
    	return this.ui_placement_right;
    }

    public void setImmersiveMode(final boolean immersive_mode) {

    	this.immersive_mode = immersive_mode;
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
				final int visibility = immersive_mode ? View.GONE : View.VISIBLE;

			    View switchCameraButton = (View) main_activity.findViewById(R.id.switch_camera);
			    View popupButton = (View) main_activity.findViewById(R.id.popup);
			    if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 )
			    	switchCameraButton.setVisibility(visibility);

				if (!main_activity.getPreview().supportsFlash())
		    		popupButton.setVisibility(View.GONE);
				else
					popupButton.setVisibility(View.VISIBLE);



        		String pref_immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
        		if( pref_immersive_mode.equals("immersive_mode_everything") ) {
    			    View takePhotoButton = (View) main_activity.findViewById(R.id.take_photo);
    			    takePhotoButton.setVisibility(visibility);
        		}
				if( !immersive_mode ) {
					showGUI(show_gui);
				}
			}
		});
    }
    
    public boolean inImmersiveMode() {
    	return immersive_mode;
    }

    public void showGUI(final boolean show) {

		this.show_gui = show;
		if( inImmersiveMode() )
			return;
		if( show && main_activity.usingKitKatImmersiveMode() ) {
			main_activity.initImmersiveMode();
		}
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
		    	final int visibility = show ? View.VISIBLE : View.GONE;
			    View switchCameraButton = (View) main_activity.findViewById(R.id.switch_camera);
			    View popupButton = (View) main_activity.findViewById(R.id.popup);

			    if( !show ) {
			    	closePopup();
			    }

			    if( !main_activity.getPreview().isVideo() || !main_activity.getPreview().supportsFlash() )
			    	popupButton.setVisibility(View.GONE);
				else
					popupButton.setVisibility(visibility);

				if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 ) {
					switchCameraButton.setVisibility(visibility);
				}

			}
		});
    }
    
    public void setPopupIcon() {

		ImageButton popup = (ImageButton)main_activity.findViewById(R.id.popup);
		String flash_value = main_activity.getPreview().getCurrentFlashValue();

    	if( flash_value != null && flash_value.equals("flash_off") ) {
    		popup.setImageResource(R.drawable.popup_flash_off);
    	}
    	else if( flash_value != null && flash_value.equals("flash_torch") ) {
    		popup.setImageResource(R.drawable.popup_flash_torch);
    	}
		else if( flash_value != null && flash_value.equals("flash_auto") ) {
    		popup.setImageResource(R.drawable.popup_flash_auto);
    	}
    	else if( flash_value != null && flash_value.equals("flash_on") ) {
    		popup.setImageResource(R.drawable.popup_flash_on);
    	}
    	else if( flash_value != null && flash_value.equals("flash_red_eye") ) {
    		popup.setImageResource(R.drawable.popup_flash_red_eye);
    	}
    	else {
    		popup.setImageResource(R.drawable.popup);
    	}
    }

    public void closePopup() {

		View popupButton = (View) main_activity.findViewById(R.id.popup);
		popupButton.setVisibility(View.VISIBLE);

		if( popupIsOpen() ) {
			ViewGroup popup_container = (ViewGroup)main_activity.findViewById(R.id.popup_container);
			popup_container.removeAllViews();
			popup_view_is_open = false;
			destroyPopup();
			main_activity.initImmersiveMode();
		}
    }

    public boolean popupIsOpen() {
    	return popup_view_is_open;
    }
    
    public void destroyPopup() {
		if( popupIsOpen() ) {
			closePopup();
		}
		popup_view = null;
    }

    public void togglePopupSettings() {
		View popupButton = (View) main_activity.findViewById(R.id.popup);
		popupButton.setVisibility(View.GONE);

		final ViewGroup popup_container = (ViewGroup)main_activity.findViewById(R.id.popup_container);
		if( popupIsOpen() ) {
			closePopup();
			return;
		}
		if( main_activity.getPreview().getCameraController() == null ) {

			return;
		}

		main_activity.getPreview().cancelTimer();

    	final long time_s = System.currentTimeMillis();

    	{
			popup_container.setBackgroundColor(0x767272);
			popup_container.setAlpha(1);
		}

    	if( popup_view == null ) {
    		popup_view = new PopupView(main_activity);
    	}
		popup_container.addView(popup_view);
		popup_view_is_open = true;
		

		popup_container.getViewTreeObserver().addOnGlobalLayoutListener( 
			new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
			    public void onGlobalLayout() {

		            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
		            	popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		            } else {
		            	popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		            }

		    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		    		String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
		    		boolean ui_placement_right = ui_placement.equals("ui_right");
		            ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, ui_placement_right ? 0.0f : 1.0f);
		    		animation.setDuration(100);
		    		popup_container.setAnimation(animation);
		        }
			}
		);
    }

    public View getPopupButton(String key) {
    	return popup_view.getPopupButton(key);
    }
}
