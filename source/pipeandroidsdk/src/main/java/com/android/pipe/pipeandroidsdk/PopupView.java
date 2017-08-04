package com.android.pipe.pipeandroidsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class PopupView extends LinearLayout {
	private static final String TAG = "PopupView";
	public static final float ALPHA_BUTTON_SELECTED = 1.0f;
	public static final float ALPHA_BUTTON = 0.6f;

	private Map<String, View> popup_buttons = new Hashtable<String, View>();

	public PopupView(Context context) {
		super(context);

		this.setOrientation(LinearLayout.VERTICAL);

		final RecordVideoActivity main_activity = (RecordVideoActivity)this.getContext();
		final Preview preview = main_activity.getPreview();
        List<String> supported_flash_values = preview.getSupportedFlashValues();
    	addButtonOptionsToPopup(supported_flash_values, R.array.flash_icons, R.array.flash_values, getResources().getString(R.string.flash_mode), preview.getCurrentFlashValue(), "TEST_FLASH", new ButtonOptionsPopupListener() {
			@Override
			public void onClick(String option) {
				preview.updateFlash(option);
		    	main_activity.getMainUI().setPopupIcon();
				main_activity.closePopup();
			}
		});
	}

    private abstract class ButtonOptionsPopupListener {
		public abstract void onClick(String option);
    }

    private void addButtonOptionsToPopup(List<String> supported_options, int icons_id, int values_id, String string, String current_value, String test_key, final ButtonOptionsPopupListener listener) {
		if (!string.equals("Flash Mode")) {
			return;
		}

    	if( supported_options != null ) {
	    	final long time_s = System.currentTimeMillis();
        	LinearLayout ll2 = new LinearLayout(this.getContext());
            ll2.setOrientation(LinearLayout.HORIZONTAL);

        	String [] icons = icons_id != -1 ? getResources().getStringArray(icons_id) : null;
        	String [] values = values_id != -1 ? getResources().getStringArray(values_id) : null;

			final float scale = getResources().getDisplayMetrics().density;
			int total_width = 280;
			{
				Activity activity = (Activity)this.getContext();
			    Display display = activity.getWindowManager().getDefaultDisplay();
			    DisplayMetrics outMetrics = new DisplayMetrics();
			    display.getMetrics(outMetrics);

			    int dpHeight = (int)(outMetrics.heightPixels / scale);

    			dpHeight -= 50;
    			if( total_width > dpHeight )
    				total_width = dpHeight;
			}

			int button_width_dp = total_width/supported_options.size();
			boolean use_scrollview = false;
			if( button_width_dp < 40 ) {
				button_width_dp = 40;
				use_scrollview = true;
			}
			View current_view = null;

			for(final String supported_option : supported_options) {

        		int resource = -1;
        		if( icons != null && values != null ) {
            		int index = -1;
            		for(int i=0;i<values.length && index==-1;i++) {
            			if( values[i].equals(supported_option) )
            				index = i;
            		}

            		if( index != -1 ) {
            			resource = getResources().getIdentifier(icons[index], null, this.getContext().getApplicationContext().getPackageName());
            		}
        		}

        		String button_string = "";

    			if( string.equalsIgnoreCase("ISO") && supported_option.length() >= 4 && supported_option.substring(0, 4).equalsIgnoreCase("ISO_") ) {
        			button_string = string + "\n" + supported_option.substring(4);
    			}
    			else if( string.equalsIgnoreCase("ISO") && supported_option.length() >= 3 && supported_option.substring(0, 3).equalsIgnoreCase("ISO") ) {
    				button_string = string + "\n" + supported_option.substring(3);
    			}
    			else {
    				button_string = string + "\n" + supported_option;
    			}

        		View view = null;
        		if( resource != -1 ) {
        			ImageButton image_button = new ImageButton(this.getContext());

        			view = image_button;
        			ll2.addView(view);


        			final RecordVideoActivity main_activity = (RecordVideoActivity)this.getContext();
        			Bitmap bm = main_activity.getPreloadedBitmap(resource);
        			if( bm != null )
        				image_button.setImageBitmap(bm);
        			else {

        			}

        			image_button.setScaleType(ScaleType.FIT_CENTER);
        			final int padding = (int) (10 * scale + 0.5f);
        			view.setPadding(padding, padding, padding, padding);
        		}
        		else {
        			Button button = new Button(this.getContext());
        			button.setBackgroundColor(Color.TRANSPARENT);
        			view = button;
        			ll2.addView(view);

        			button.setText(button_string);
        			button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.0f);
        			button.setTextColor(Color.WHITE);
        			final int padding = (int) (0 * scale + 0.5f); // convert dps to pixels
        			view.setPadding(padding, padding, padding, padding);
        		}

    			ViewGroup.LayoutParams params = view.getLayoutParams();
    			params.width = (int) (button_width_dp * scale + 0.5f); // convert dps to pixels
    			params.height = (int) (50 * scale + 0.5f); // convert dps to pixels
    			view.setLayoutParams(params);

    			view.setContentDescription(button_string);
    			if( supported_option.equals(current_value) ) {
    				view.setAlpha(ALPHA_BUTTON_SELECTED);
    				current_view = view;
    			}
    			else {
    				view.setAlpha(ALPHA_BUTTON);
    			}

    			view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						listener.onClick(supported_option);
					}
    			});
    			this.popup_buttons.put(test_key + "_" + supported_option, view);

    		}

			if( use_scrollview ) {

	        	final HorizontalScrollView scroll = new HorizontalScrollView(this.getContext());
	        	scroll.addView(ll2);
	        	{
	    			ViewGroup.LayoutParams params = new LayoutParams(
	    					(int) (total_width * scale + 0.5f), // convert dps to pixels
	    			        LayoutParams.WRAP_CONTENT);
	    			scroll.setLayoutParams(params);
	        	}
	        	this.addView(scroll);
	        	if( current_view != null ) {
	        		final View final_current_view = current_view;
	        		this.getViewTreeObserver().addOnGlobalLayoutListener(
	        			new OnGlobalLayoutListener() {
							@Override
							public void onGlobalLayout() {
				        		scroll.scrollTo(final_current_view.getLeft(), 0);
							}
	        			}
	        		);
	        	}
			}
			else {

	    		this.addView(ll2);
			}

        }
    }



    private abstract class ArrayOptionsPopupListener {
		public abstract int onClickPrev();
		public abstract int onClickNext();
    }

    public View getPopupButton(String key) {

    	return popup_buttons.get(key);
    }
}
