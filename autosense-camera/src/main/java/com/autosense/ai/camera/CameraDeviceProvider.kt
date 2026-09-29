package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.Surface

internal interface CameraDeviceProvider {

    fun openCamera(
        cameraId: String,
        callback: CameraDevice.StateCallback
    )

    fun createCaptureSession(
        camera: CameraDevice,
        surface: Surface,
        callback: CameraCaptureSession.StateCallback
    )

    fun startRepeatingCapture(
        camera: CameraDevice,
        session: CameraCaptureSession,
        surface: Surface
    )
}