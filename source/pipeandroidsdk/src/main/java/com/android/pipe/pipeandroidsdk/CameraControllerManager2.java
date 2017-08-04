package com.android.pipe.pipeandroidsdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraControllerManager2 extends CameraControllerManager {
	private static final String TAG = "CameraControllerManager2";

	private Context context = null;

	public CameraControllerManager2(Context context) {
		this.context = context;
	}

	@Override
	public int getNumberOfCameras() {
		CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
		try {
			return manager.getCameraIdList().length;
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
		catch(AssertionError e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean isFrontFacing(int cameraId) {
		CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
		try {
			String cameraIdS = manager.getCameraIdList()[cameraId];
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
			return characteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT;
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isHardwareLevelSupported(CameraCharacteristics c, int requiredLevel) {
		int deviceLevel = c.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

		if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
			return requiredLevel == deviceLevel;
		}

		return requiredLevel <= deviceLevel;
	}

	public boolean allowCamera2Support(int cameraId) {
		CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
		try {
			String cameraIdS = manager.getCameraIdList()[cameraId];
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraIdS);
			boolean supported = isHardwareLevelSupported(characteristics, CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED);
			return supported;
		}
		catch (CameraAccessException e) {
			e.printStackTrace();
		}
		return false;
	}
}
