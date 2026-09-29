package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import com.autosense.ai.api.camera.CameraFrame

internal class CameraController(
    private val cameraDeviceProvider: CameraDeviceProvider,
    private val cameraImageReaderFactory: CameraImageReaderFactory,
    private val cameraId: String,
    private val width: Int,
    private val height: Int,
    private val frameAdapter: CameraFrameAdapter,
    private val frameQueue: LatestFrameQueue
) : AutoCloseable {

    private var cameraDevice: CameraDevice? = null

    private var captureSession: CameraCaptureSession? = null

    private var imageReader: CameraImageReaderHandle? = null

    private var sequenceNumber = 0L

    private var started = false

    fun start() {
        check(!started) {
            "CameraController is already started"
        }

        imageReader = cameraImageReaderFactory.create(
            width = width,
            height = height,
            onImageAvailable = ::handleImage
        )

        started = true

        cameraDeviceProvider.openCamera(
            cameraId = cameraId,
            callback = cameraStateCallback
        )
    }

    fun pollFrame(): CameraFrame? {
        check(started) {
            "CameraController is not started"
        }

        return frameQueue.poll()
    }

    fun stop() {
        if (!started) {
            return
        }

        started = false

        captureSession?.close()
        captureSession = null

        cameraDevice?.close()
        cameraDevice = null

        imageReader?.close()
        imageReader = null

        frameQueue.close()
    }

    override fun close() {
        stop()
    }

    private val cameraStateCallback =
        object : CameraDevice.StateCallback() {

            override fun onOpened(
                camera: CameraDevice
            ) {
                if (!started) {
                    camera.close()
                    return
                }

                cameraDevice = camera

                createCaptureSession(camera)
            }

            override fun onDisconnected(
                camera: CameraDevice
            ) {
                camera.close()

                if (cameraDevice === camera) {
                    cameraDevice = null
                }

                captureSession = null
            }

            override fun onError(
                camera: CameraDevice,
                error: Int
            ) {
                camera.close()

                if (cameraDevice === camera) {
                    cameraDevice = null
                }

                captureSession = null
            }
        }

    private fun createCaptureSession(
        camera: CameraDevice
    ) {
        val reader = imageReader ?: run {
            camera.close()

            if (cameraDevice === camera) {
                cameraDevice = null
            }

            return
        }

        cameraDeviceProvider.createCaptureSession(
            camera = camera,
            surface = reader.surface,
            callback = object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(
                    session: CameraCaptureSession
                ) {
                    if (!started) {
                        session.close()
                        return
                    }

                    captureSession = session

                    cameraDeviceProvider.startRepeatingCapture(
                        camera = camera,
                        session = session,
                        surface = reader.surface
                    )
                }

                override fun onConfigureFailed(
                    session: CameraCaptureSession
                ) {
                    session.close()

                    if (captureSession === session) {
                        captureSession = null
                    }
                }
            }
        )
    }

    private fun handleImage(
        image: android.media.Image
    ) {
        try {
            val frame = frameAdapter.adapt(
                image = image,
                sequenceNumber = sequenceNumber++,
                rotationDegrees = 0
            )

            frameQueue.offer(frame)
        } catch (exception: Exception) {
            image.close()
        }
    }
}