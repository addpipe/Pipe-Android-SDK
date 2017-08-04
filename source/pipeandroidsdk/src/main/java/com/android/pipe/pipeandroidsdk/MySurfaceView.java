package com.android.pipe.pipeandroidsdk;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


public class MySurfaceView extends SurfaceView implements CameraSurface {

	private Preview preview = null;
	private int [] measure_spec = new int[2];
	
	@SuppressWarnings("deprecation")
	public
	MySurfaceView(Context context, Bundle savedInstanceState, Preview preview) {
		super(context);
		this.preview = preview;

		getHolder().addCallback(preview);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // deprecated
	}
	
	@Override
	public View getView() {
		return this;
	}
	
	@Override
	public void setPreviewDisplay(CameraController camera_controller) {

		try {
			camera_controller.setPreviewDisplay(this.getHolder());
		}
		catch(CameraControllerException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void setVideoRecorder(MediaRecorder video_recorder) {
    	video_recorder.setPreviewDisplay(this.getHolder().getSurface());
	}


	@Override
	public void onDraw(Canvas canvas) {
		preview.draw(canvas);
	}

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
    	preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
    	super.onMeasure(measure_spec[0], measure_spec[1]);
    }

	@Override
	public void setTransform(Matrix matrix) {
		throw new RuntimeException();
	}
}
