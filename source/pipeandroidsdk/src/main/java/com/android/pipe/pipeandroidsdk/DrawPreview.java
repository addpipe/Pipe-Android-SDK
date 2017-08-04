package com.android.pipe.pipeandroidsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.widget.ImageButton;

public class DrawPreview {

	private RecordVideoActivity main_activity = null;
	private MyApplicationInterface applicationInterface = null;

	private Paint p = new Paint();
	private RectF face_rect = new RectF();
	private RectF draw_rect = new RectF();
	private int [] gui_location = new int[2];

	private float stroke_width = 0.0f;

	private Rect location_dest = new Rect();
	
	private Bitmap last_thumbnail = null;
	private boolean thumbnail_anim = false;
	private long thumbnail_anim_start_ms = -1;

    private boolean taking_picture = false;
    
	private boolean continuous_focus_moving = false;
	private long continuous_focus_moving_ms = 0;

	public DrawPreview(RecordVideoActivity main_activity, MyApplicationInterface applicationInterface) {
		this.main_activity = main_activity;
		this.applicationInterface = applicationInterface;

		p.setAntiAlias(true);
        p.setStrokeCap(Paint.Cap.ROUND);
		final float scale = getContext().getResources().getDisplayMetrics().density;
		this.stroke_width = (float) (0.5f * scale + 0.5f);
		p.setStrokeWidth(stroke_width);
	}

	private Context getContext() {
    	return main_activity;
    }

	public void cameraInOperation(boolean in_operation) {
    	if( in_operation && !main_activity.getPreview().isVideo() ) {
    		taking_picture = true;
    	}
    	else {
    		taking_picture = false;
    	}
    }

	public void onContinuousFocusMove(boolean start) {

		if( start ) {
			if( !continuous_focus_moving ) {
				continuous_focus_moving = true;
				continuous_focus_moving_ms = System.currentTimeMillis();
			}
		}
	}

	public void clearContinuousFocusMove() {

		continuous_focus_moving = false;
		continuous_focus_moving_ms = 0;
	}

	private boolean getTakePhotoBorderPref() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    	return sharedPreferences.getBoolean(PreferenceKeys.getTakePhotoBorderPreferenceKey(), true);
    }
    
    private int getAngleHighlightColor() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		String color = sharedPreferences.getString(PreferenceKeys.getShowAngleHighlightColorPreferenceKey(), "#14e715");
		return Color.parseColor(color);
    }

    private String getTimeStringFromSeconds(long time) {
    	int secs = (int)(time % 60);
    	time /= 60;
    	int mins = (int)(time % 60);
    	time /= 60;
    	long hours = time;
    	String time_s = hours + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs);
    	return time_s;
    }

	public void onDrawPreview(Canvas canvas) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		Preview preview  = main_activity.getPreview();
		CameraController camera_controller = preview.getCameraController();
		int ui_rotation = preview.getUIRotation();
		boolean has_level_angle = preview.hasLevelAngle();
		double level_angle = preview.getLevelAngle();

		boolean ui_placement_right = main_activity.getMainUI().getUIPlacementRight();
		if( main_activity.getMainUI().inImmersiveMode() ) {
			String immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
			if( immersive_mode.equals("immersive_mode_everything") ) {
				return;
			}
		}
		final float scale = getContext().getResources().getDisplayMetrics().density;
		String preference_grid = sharedPreferences.getString(PreferenceKeys.getShowGridPreferenceKey(), "preference_grid_none");
		if( camera_controller != null && taking_picture && getTakePhotoBorderPref() ) {
			p.setColor(Color.WHITE);
			p.setStyle(Paint.Style.STROKE);
			float this_stroke_width = (float) (5.0f * scale + 0.5f); // convert dps to pixels
			p.setStrokeWidth(this_stroke_width);
			canvas.drawRect(0.0f, 0.0f, canvas.getWidth(), canvas.getHeight(), p);
			p.setStyle(Paint.Style.FILL); // reset
			p.setStrokeWidth(stroke_width); // reset
		}
		if( camera_controller != null && preference_grid.equals("preference_grid_3x3") ) {
			p.setColor(Color.WHITE);
			canvas.drawLine(canvas.getWidth()/3.0f, 0.0f, canvas.getWidth()/3.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(2.0f*canvas.getWidth()/3.0f, 0.0f, 2.0f*canvas.getWidth()/3.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight()/3.0f, canvas.getWidth()-1.0f, canvas.getHeight()/3.0f, p);
			canvas.drawLine(0.0f, 2.0f*canvas.getHeight()/3.0f, canvas.getWidth()-1.0f, 2.0f*canvas.getHeight()/3.0f, p);
		}
		else if( camera_controller != null && preference_grid.equals("preference_grid_phi_3x3") ) {
			p.setColor(Color.WHITE);
			canvas.drawLine(canvas.getWidth()/2.618f, 0.0f, canvas.getWidth()/2.618f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(1.618f*canvas.getWidth()/2.618f, 0.0f, 1.618f*canvas.getWidth()/2.618f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight()/2.618f, canvas.getWidth()-1.0f, canvas.getHeight()/2.618f, p);
			canvas.drawLine(0.0f, 1.618f*canvas.getHeight()/2.618f, canvas.getWidth()-1.0f, 1.618f*canvas.getHeight()/2.618f, p);
		}
		else if( camera_controller != null && preference_grid.equals("preference_grid_4x2") ) {
			p.setColor(Color.GRAY);
			canvas.drawLine(canvas.getWidth()/4.0f, 0.0f, canvas.getWidth()/4.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(canvas.getWidth()/2.0f, 0.0f, canvas.getWidth()/2.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(3.0f*canvas.getWidth()/4.0f, 0.0f, 3.0f*canvas.getWidth()/4.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight()/2.0f, canvas.getWidth()-1.0f, canvas.getHeight()/2.0f, p);
			p.setColor(Color.WHITE);
			int crosshairs_radius = (int) (20 * scale + 0.5f); // convert dps to pixels
			canvas.drawLine(canvas.getWidth()/2.0f, canvas.getHeight()/2.0f - crosshairs_radius, canvas.getWidth()/2.0f, canvas.getHeight()/2.0f + crosshairs_radius, p);
			canvas.drawLine(canvas.getWidth()/2.0f - crosshairs_radius, canvas.getHeight()/2.0f, canvas.getWidth()/2.0f + crosshairs_radius, canvas.getHeight()/2.0f, p);
		}
		else if( camera_controller != null && preference_grid.equals("preference_grid_crosshair") ) {
			p.setColor(Color.WHITE);
			canvas.drawLine(canvas.getWidth()/2.0f, 0.0f, canvas.getWidth()/2.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(0.0f, canvas.getHeight()/2.0f, canvas.getWidth()-1.0f, canvas.getHeight()/2.0f, p);
		}
		else if( camera_controller != null && ( preference_grid.equals("preference_grid_golden_spiral_right") || preference_grid.equals("preference_grid_golden_spiral_left") || preference_grid.equals("preference_grid_golden_spiral_upside_down_right") || preference_grid.equals("preference_grid_golden_spiral_upside_down_left") ) ) {
			canvas.save();
			if( preference_grid.equals("preference_grid_golden_spiral_left") ) {
				canvas.scale(-1.0f, 1.0f, canvas.getWidth()*0.5f, canvas.getHeight()*0.5f);
			}
			else if( preference_grid.equals("preference_grid_golden_spiral_right") ) {
				// no transformation needed
			}
			else if( preference_grid.equals("preference_grid_golden_spiral_upside_down_left") ) {
				canvas.rotate(180.0f, canvas.getWidth()*0.5f, canvas.getHeight()*0.5f);
			}
			else if( preference_grid.equals("preference_grid_golden_spiral_upside_down_right") ) {
				canvas.scale(1.0f, -1.0f, canvas.getWidth()*0.5f, canvas.getHeight()*0.5f);
			}
			p.setColor(Color.WHITE);
			p.setStyle(Paint.Style.STROKE);
			int fibb = 34;
			int fibb_n = 21;
			int left = 0, top = 0;
			int full_width = canvas.getWidth();
			int full_height = canvas.getHeight();
			int width = (int)(full_width*((double)fibb_n)/(double)(fibb));
			int height = full_height;
			
			for(int count=0;count<2;count++) {
				canvas.save();
				draw_rect.set(left, top, left+width, top+height);
				canvas.clipRect(draw_rect);
				canvas.drawRect(draw_rect, p);
				draw_rect.set(left, top, left+2*width, top+2*height);
				canvas.drawOval(draw_rect, p);
				canvas.restore();
				
				int old_fibb = fibb;
				fibb = fibb_n;
				fibb_n = old_fibb - fibb;
	
				left += width;
				full_width = full_width - width;
				width = full_width;
				height = (int)(height*((double)fibb_n)/(double)(fibb));

				canvas.save();
				draw_rect.set(left, top, left+width, top+height);
				canvas.clipRect(draw_rect);
				canvas.drawRect(draw_rect, p);
				draw_rect.set(left-width, top, left+width, top+2*height);
				canvas.drawOval(draw_rect, p);
				canvas.restore();
	
				old_fibb = fibb;
				fibb = fibb_n;
				fibb_n = old_fibb - fibb;
	
				top += height;
				full_height = full_height - height;
				height = full_height;
				width = (int)(width*((double)fibb_n)/(double)(fibb));
				left += full_width - width;

				canvas.save();
				draw_rect.set(left, top, left+width, top+height);
				canvas.clipRect(draw_rect);
				canvas.drawRect(draw_rect, p);
				draw_rect.set(left-width, top-height, left+width, top+height);
				canvas.drawOval(draw_rect, p);
				canvas.restore();
	
				old_fibb = fibb;
				fibb = fibb_n;
				fibb_n = old_fibb - fibb;
	
				full_width = full_width - width;
				width = full_width;
				left -= width;
				height = (int)(height*((double)fibb_n)/(double)(fibb));
				top += full_height - height;

				canvas.save();
				draw_rect.set(left, top, left+width, top+height);
				canvas.clipRect(draw_rect);
				canvas.drawRect(draw_rect, p);
				draw_rect.set(left, top-height, left+2*width, top+height);
				canvas.drawOval(draw_rect, p);
				canvas.restore();

				old_fibb = fibb;
				fibb = fibb_n;
				fibb_n = old_fibb - fibb;

				full_height = full_height - height;
				height = full_height;
				top -= height;
				width = (int)(width*((double)fibb_n)/(double)(fibb));
			}
			
			canvas.restore();
			p.setStyle(Paint.Style.FILL); // reset
		}
		else if( camera_controller != null && ( preference_grid.equals("preference_grid_golden_triangle_1") || preference_grid.equals("preference_grid_golden_triangle_2") ) ) {
			p.setColor(Color.WHITE);
			double theta = Math.atan2(canvas.getWidth(), canvas.getHeight());
			double dist = canvas.getHeight() * Math.cos(theta);
			float dist_x = (float)(dist * Math.sin(theta));
			float dist_y = (float)(dist * Math.cos(theta));
			if( preference_grid.equals("preference_grid_golden_triangle_1") ) {
				canvas.drawLine(0.0f, canvas.getHeight()-1.0f, canvas.getWidth()-1.0f, 0.0f, p);
				canvas.drawLine(0.0f, 0.0f, dist_x, canvas.getHeight()-dist_y, p);
				canvas.drawLine(canvas.getWidth()-1.0f-dist_x, dist_y-1.0f, canvas.getWidth()-1.0f, canvas.getHeight()-1.0f, p);
			}
			else {
				canvas.drawLine(0.0f, 0.0f, canvas.getWidth()-1.0f, canvas.getHeight()-1.0f, p);
				canvas.drawLine(canvas.getWidth()-1.0f, 0.0f, canvas.getWidth()-1.0f-dist_x, canvas.getHeight()-dist_y, p);
				canvas.drawLine(dist_x, dist_y-1.0f, 0.0f, canvas.getHeight()-1.0f, p);
			}
		}
		else if( camera_controller != null && preference_grid.equals("preference_grid_diagonals") ) {
			p.setColor(Color.WHITE);
			canvas.drawLine(0.0f, 0.0f, canvas.getHeight()-1.0f, canvas.getHeight()-1.0f, p);
			canvas.drawLine(canvas.getHeight()-1.0f, 0.0f, 0.0f, canvas.getHeight()-1.0f, p);
			int diff = canvas.getWidth() - canvas.getHeight();
			if( diff > 0 ) {
				canvas.drawLine(diff, 0.0f, diff+canvas.getHeight()-1.0f, canvas.getHeight()-1.0f, p);
				canvas.drawLine(diff+canvas.getHeight()-1.0f, 0.0f, diff, canvas.getHeight()-1.0f, p);
			}
		}

		if( preview.isVideo() || sharedPreferences.getString(PreferenceKeys.getPreviewSizePreferenceKey(), "preference_preview_size_wysiwyg").equals("preference_preview_size_wysiwyg") ) {
			String preference_crop_guide = sharedPreferences.getString(PreferenceKeys.getShowCropGuidePreferenceKey(), "crop_guide_none");
			if( camera_controller != null && preview.getTargetRatio() > 0.0 && !preference_crop_guide.equals("crop_guide_none") ) {
				p.setStyle(Paint.Style.STROKE);
				p.setColor(Color.rgb(255, 235, 59)); // Yellow 500
				double crop_ratio = -1.0;
				if( preference_crop_guide.equals("crop_guide_1") ) {
					crop_ratio = 1.0;
				}
				else if( preference_crop_guide.equals("crop_guide_1.25") ) {
					crop_ratio = 1.25;
				}
				else if( preference_crop_guide.equals("crop_guide_1.33") ) {
					crop_ratio = 1.33333333;
				}
				else if( preference_crop_guide.equals("crop_guide_1.4") ) {
					crop_ratio = 1.4;
				}
				else if( preference_crop_guide.equals("crop_guide_1.5") ) {
					crop_ratio = 1.5;
				}
				else if( preference_crop_guide.equals("crop_guide_1.78") ) {
					crop_ratio = 1.77777778;
				}
				else if( preference_crop_guide.equals("crop_guide_1.85") ) {
					crop_ratio = 1.85;
				}
				else if( preference_crop_guide.equals("crop_guide_2.33") ) {
					crop_ratio = 2.33333333;
				}
				else if( preference_crop_guide.equals("crop_guide_2.35") ) {
					crop_ratio = 2.35006120; // actually 1920:817
				}
				else if( preference_crop_guide.equals("crop_guide_2.4") ) {
					crop_ratio = 2.4;
				}
				if( crop_ratio > 0.0 && Math.abs(preview.getTargetRatio() - crop_ratio) > 1.0e-5 ) {

					int left = 1, top = 1, right = canvas.getWidth()-1, bottom = canvas.getHeight()-1;
					if( crop_ratio > preview.getTargetRatio() ) {
						double new_hheight = ((double)canvas.getWidth()) / (2.0f*crop_ratio);
						top = (int)(canvas.getHeight()/2 - (int)new_hheight);
						bottom = (int)(canvas.getHeight()/2 + (int)new_hheight);
					}
					else {
						double new_hwidth = (((double)canvas.getHeight()) * crop_ratio) / 2.0f;
						left = (int)(canvas.getWidth()/2 - (int)new_hwidth);
						right = (int)(canvas.getWidth()/2 + (int)new_hwidth);
					}
					canvas.drawRect(left, top, right, bottom, p);
				}
				p.setStyle(Paint.Style.FILL); // reset
			}
		}

		if( camera_controller != null && this.thumbnail_anim && last_thumbnail != null ) {
			long time = System.currentTimeMillis() - this.thumbnail_anim_start_ms;
			final long duration = 500;
			if( time > duration ) {
				this.thumbnail_anim = false;
			}
		}
		
		canvas.save();
		canvas.rotate(ui_rotation, canvas.getWidth()/2.0f, canvas.getHeight()/2.0f);

		int text_y = (int) (20 * scale + 0.5f); // convert dps to pixels

		int text_base_y = 0;
		if( ui_rotation == ( ui_placement_right ? 0 : 180 ) ) {
			text_base_y = canvas.getHeight() - (int)(0.5*text_y);
		}
		else if( ui_rotation == ( ui_placement_right ? 180 : 0 ) ) {
			text_base_y = canvas.getHeight() - (int)(2.5*text_y); // leave room for GUI icons
		}
		else if( ui_rotation == 90 || ui_rotation == 270 ) {
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);

			view.getLocationOnScreen(gui_location);
			int view_left = gui_location[0];
			preview.getView().getLocationOnScreen(gui_location);
			int this_left = gui_location[0];
			int diff_x = view_left - ( this_left + canvas.getWidth()/2 );
			int max_x = canvas.getWidth();
			if( ui_rotation == 90 ) {
				max_x -= (int)(2.5*text_y);
			}
			if( canvas.getWidth()/2 + diff_x > max_x ) {
				diff_x = max_x - canvas.getWidth()/2;
			}
			text_base_y = canvas.getHeight()/2 + diff_x - (int)(0.5*text_y);
		}
		final int top_y = (int) (5 * scale + 0.5f); // convert dps to pixels
		final int location_size = (int) (20 * scale + 0.5f); // convert dps to pixels

		final String ybounds_text = getContext().getResources().getString(R.string.zoom) + getContext().getResources().getString(R.string.angle) + getContext().getResources().getString(R.string.direction);
		final double close_angle = 1.0f;
		if( camera_controller != null && !preview.isPreviewPaused() ) {

			boolean draw_angle = has_level_angle && sharedPreferences.getBoolean(PreferenceKeys.getShowAnglePreferenceKey(), true);
		}
		else if( camera_controller == null ) {

			p.setColor(Color.WHITE);
			p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
			p.setTextAlign(Paint.Align.CENTER);
			int pixels_offset = (int) (20 * scale + 0.5f); // convert dps to pixels
			canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_1), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f, p);
			canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_2), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f + pixels_offset, p);
			canvas.drawText(getContext().getResources().getString(R.string.failed_to_open_camera_3), canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f + 2*pixels_offset, p);
		}
		if( camera_controller != null && sharedPreferences.getBoolean(PreferenceKeys.getShowISOPreferenceKey(), true) ) {
			p.setTextSize(14 * scale + 0.5f); // convert dps to pixels
			p.setTextAlign(Paint.Align.LEFT);
			int location_x = (int) (50 * scale + 0.5f); // convert dps to pixels
			int location_y = top_y + (int) (32 * scale + 0.5f); // convert dps to pixels

			if( ui_rotation == 90 || ui_rotation == 270 ) {
				int diff = canvas.getWidth() - canvas.getHeight();
				location_x += diff/2;
				location_y -= diff/2;
			}
			if( ui_rotation == 90 ) {
				location_y = canvas.getHeight() - location_y - location_size;
			}
			if( ui_rotation == 180 ) {
				location_x = canvas.getWidth() - location_x;
				p.setTextAlign(Paint.Align.RIGHT);
			}
			String string = "";
			if( camera_controller.captureResultHasIso() ) {
				int iso = camera_controller.captureResultIso();
				if( string.length() > 0 )
					string += " ";
			}
			if( camera_controller.captureResultHasExposureTime() ) {
				long exposure_time = camera_controller.captureResultExposureTime();
				if( string.length() > 0 )
					string += " ";
			}



		}
		
		boolean store_location = sharedPreferences.getBoolean(PreferenceKeys.getLocationPreferenceKey(), false);
		if( store_location ) {
			int location_x = (int) (20 * scale + 0.5f); // convert dps to pixels
			int location_y = top_y;
			if( ui_rotation == 90 || ui_rotation == 270 ) {
				int diff = canvas.getWidth() - canvas.getHeight();
				location_x += diff/2;
				location_y -= diff/2;
			}
			if( ui_rotation == 90 ) {
				location_y = canvas.getHeight() - location_y - location_size;
			}
			if( ui_rotation == 180 ) {
				location_x = canvas.getWidth() - location_x - location_size;
			}
			location_dest.set(location_x, location_y, location_x + location_size, location_y + location_size);

		}


		canvas.restore();
		
		if( camera_controller != null && !preview.isPreviewPaused() && has_level_angle && sharedPreferences.getBoolean(PreferenceKeys.getShowAngleLinePreferenceKey(), false) ) {
			int radius_dps = (ui_rotation == 90 || ui_rotation == 270) ? 60 : 80;
			int radius = (int) (radius_dps * scale + 0.5f); // convert dps to pixels
			double angle = - preview.getOrigLevelAngle();
			// see http://android-developers.blogspot.co.uk/2010/09/one-screen-turn-deserves-another.html
		    int rotation = main_activity.getWindowManager().getDefaultDisplay().getRotation();
		    switch (rotation) {
	    	case Surface.ROTATION_90:
	    	case Surface.ROTATION_270:
	    		angle += 90.0;
	    		break;
    		default:
    			break;
		    }

			int cx = canvas.getWidth()/2;
			int cy = canvas.getHeight()/2;
			
			boolean is_level = false;
			if( Math.abs(level_angle) <= close_angle ) { // n.b., use level_angle, not angle or orig_level_angle
				is_level = true;
			}
			
			if( is_level ) {
				radius = (int)(radius * 1.2);
			}

			canvas.save();
			canvas.rotate((float)angle, cx, cy);

			final int line_alpha = 96;
			p.setStyle(Paint.Style.FILL);
			float hthickness = (0.5f * scale + 0.5f); // convert dps to pixels
			p.setColor(Color.BLACK);
			p.setAlpha(64);

			draw_rect.set(cx - radius - hthickness, cy - 2*hthickness, cx + radius + hthickness, cy + 2*hthickness);
			canvas.drawRoundRect(draw_rect, 2*hthickness, 2*hthickness, p);
			// draw the vertical crossbar
			draw_rect.set(cx - 2*hthickness, cy - radius/2 - hthickness, cx + 2*hthickness, cy + radius/2 + hthickness);
			canvas.drawRoundRect(draw_rect, hthickness, hthickness, p);

			if( is_level ) {
				p.setColor(getAngleHighlightColor());
			}
			else {
				p.setColor(Color.WHITE);
			}
			p.setAlpha(line_alpha);
			draw_rect.set(cx - radius, cy - hthickness, cx + radius, cy + hthickness);
			canvas.drawRoundRect(draw_rect, hthickness, hthickness, p);
			
			draw_rect.set(cx - hthickness, cy - radius/2, cx + hthickness, cy + radius/2);
			canvas.drawRoundRect(draw_rect, hthickness, hthickness, p);

			if( is_level ) {

				p.setColor(Color.BLACK);
				p.setAlpha(64);
				draw_rect.set(cx - radius - hthickness, cy - 7*hthickness, cx + radius + hthickness, cy - 3*hthickness);
				canvas.drawRoundRect(draw_rect, 2*hthickness, 2*hthickness, p);

				p.setColor(getAngleHighlightColor());
				p.setAlpha(line_alpha);
				draw_rect.set(cx - radius, cy - 6*hthickness, cx + radius, cy - 4*hthickness);
				canvas.drawRoundRect(draw_rect, hthickness, hthickness, p);
			}
			p.setAlpha(255);
			p.setStyle(Paint.Style.FILL); // reset

			canvas.restore();
		}

		if( camera_controller != null && continuous_focus_moving ) {
			long dt = System.currentTimeMillis() - continuous_focus_moving_ms;
			final long length = 1000;
			if( dt <= length ) {
				float frac = ((float)dt) / (float)length;
				float pos_x = canvas.getWidth()/2.0f;
				float pos_y = canvas.getHeight()/2.0f;
				float min_radius = (float) (40 * scale + 0.5f); // convert dps to pixels
				float max_radius = (float) (60 * scale + 0.5f); // convert dps to pixels
				float radius = 0.0f;
				if( frac < 0.5f ) {
					float alpha = frac*2.0f;
					radius = (1.0f-alpha) * min_radius + alpha * max_radius;
				}
				else {
					float alpha = (frac-0.5f)*2.0f;
					radius = (1.0f-alpha) * max_radius + alpha * min_radius;
				}

				p.setStyle(Paint.Style.STROKE);
				canvas.drawCircle(pos_x, pos_y, radius, p);
				p.setStyle(Paint.Style.FILL); // reset
			}
			else {
				continuous_focus_moving = false;
			}
		}

    }
}
