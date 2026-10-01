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
        callback: CameraDeviceProvider.Callback
    ) {
        cameraManager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {

                override fun onOpened(
                    camera: CameraDevice
                ) {
                    callback.onOpened(camera)
                }

                override fun onDisconnected(
                    camera: CameraDevice
                ) {
                    callback.onDisconnected(camera)
                }

                override fun onError(
                    camera: CameraDevice,
                    error: Int
                ) {
                    callback.onError(
                        camera,
                        error
                    )
                }
            },
            handler
        )
    }

    override fun createCaptureSession(
        camera: CameraDevice,
        surface: Surface,
        callback: CameraDeviceProvider.CaptureSessionCallback
    ) {
        camera.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(
                    session: CameraCaptureSession
                ) {
                    callback.onConfigured(session)
                }

                override fun onConfigureFailed(
                    session: CameraCaptureSession
                ) {
                    callback.onConfigureFailed(session)
                }
            },
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