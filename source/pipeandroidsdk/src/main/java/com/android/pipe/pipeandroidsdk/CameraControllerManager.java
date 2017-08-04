package com.android.pipe.pipeandroidsdk;

public abstract class CameraControllerManager {
	public abstract int getNumberOfCameras();
	public abstract boolean isFrontFacing(int cameraId);
}
