package com.android.pipe.pipeandroidsdk;

import android.hardware.Camera;
import android.util.Log;


@SuppressWarnings("deprecation")
public class CameraControllerManager1 extends CameraControllerManager {
	private static final String TAG = "CameraControllerManager1";
	public int getNumberOfCameras() {
		return Camera.getNumberOfCameras();
	}

	public boolean isFrontFacing(int cameraId) {
	    try {
		    Camera.CameraInfo camera_info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, camera_info);
			return (camera_info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
	    }
	    catch(RuntimeException e) {
	    	// Had a report of this crashing on Galaxy Nexus - may be device specific issue, see http://stackoverflow.com/questions/22383708/java-lang-runtimeexception-fail-to-get-camera-info
	    	// but good to catch it anyway
	    	e.printStackTrace();
	    	return false;
	    }
	}
}
