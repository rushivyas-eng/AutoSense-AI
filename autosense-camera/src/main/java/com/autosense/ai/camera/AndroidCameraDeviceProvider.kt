package com.autosense.ai.camera

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.view.Surface

internal class AndroidCameraDeviceProvider(
    private val cameraManager: CameraManager,
    private val handler: Handler
) : CameraDeviceProvider {

    @SuppressLint("MissingPermission")
    override fun openCamera(
        cameraId: String,
        callback: CameraDevice.StateCallback
    ) {
        cameraManager.openCamera(
            cameraId,
            callback,
            handler
        )
    }

    override fun createCaptureSession(
        camera: CameraDevice,
        surface: Surface,
        callback: CameraCaptureSession.StateCallback
    ) {
        camera.createCaptureSession(
            listOf(surface),
            callback,
            handler
        )
    }

    override fun startRepeatingCapture(
        camera: CameraDevice,
        session: CameraCaptureSession,
        surface: Surface
    ) {
        val requestBuilder =
            camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )

        requestBuilder.addTarget(surface)

        session.setRepeatingRequest(
            requestBuilder.build(),
            null,
            handler
        )
    }
}