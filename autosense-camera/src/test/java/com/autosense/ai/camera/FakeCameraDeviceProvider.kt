package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.Surface

internal class FakeCameraDeviceProvider : CameraDeviceProvider {

    var openedCameraId: String? = null

    var callback: CameraDeviceProvider.Callback? = null

    var captureSessionCamera: CameraDevice? = null

    var captureSessionSurface: Surface? = null

    var captureSessionCallback:
            CameraDeviceProvider.CaptureSessionCallback? = null

    var repeatingCaptureCamera: CameraDevice? = null

    var repeatingCaptureSession: CameraCaptureSession? = null

    var repeatingCaptureSurface: Surface? = null

    override fun openCamera(
        cameraId: String,
        callback: CameraDeviceProvider.Callback
    ) {
        openedCameraId = cameraId
        this.callback = callback
    }

    override fun createCaptureSession(
        camera: CameraDevice,
        surface: Surface,
        callback: CameraDeviceProvider.CaptureSessionCallback
    ) {
        captureSessionCamera = camera
        captureSessionSurface = surface
        captureSessionCallback = callback
    }

    override fun startRepeatingCapture(
        camera: CameraDevice,
        session: CameraCaptureSession,
        surface: Surface
    ) {
        repeatingCaptureCamera = camera
        repeatingCaptureSession = session
        repeatingCaptureSurface = surface
    }
}