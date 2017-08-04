package com.android.pipe.pipeandroidsdk;

import android.content.Context;
import android.graphics.Matrix;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;


public class MyTextureView extends TextureView implements CameraSurface {

	private Preview preview = null;
	private int [] measure_spec = new int[2];
	
	public MyTextureView(Context context, Bundle savedInstanceState, Preview preview) {
		super(context);
		this.preview = preview;


		this.setSurfaceTextureListener(preview);
	}
	
	@Override
	public View getView() {
		return this;
	}

	//Here endcam
	@Override
	public void setPreviewDisplay(CameraController camera_controller) {

		try {
			camera_controller.setPreviewTexture(this.getSurfaceTexture());
		}
		catch(CameraControllerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setVideoRecorder(MediaRecorder video_recorder) {
	}


    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
    	preview.getMeasureSpec(measure_spec, widthSpec, heightSpec);
    	super.onMeasure(measure_spec[0], measure_spec[1]);
    }

	@Override
	public void setTransform(Matrix matrix) {
		super.setTransform(matrix);
	}
}
