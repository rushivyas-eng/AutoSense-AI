package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.Surface

internal interface CameraDeviceProvider {

    interface Callback {

        fun onOpened(
            camera: CameraDevice
        )

        fun onDisconnected(
            camera: CameraDevice
        )

        fun onError(
            camera: CameraDevice,
            error: Int
        )
    }

    interface CaptureSessionCallback {

        fun onConfigured(
            session: CameraCaptureSession
        )

        fun onConfigureFailed(
            session: CameraCaptureSession
        )
    }

    fun openCamera(
        cameraId: String,
        callback: Callback
    )

    fun createCaptureSession(
        camera: CameraDevice,
        surface: Surface,
        callback: CaptureSessionCallback
    )

    fun startRepeatingCapture(
        camera: CameraDevice,
        session: CameraCaptureSession,
        surface: Surface
    )
}