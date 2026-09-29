package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CameraControllerTest {

    @Test
    fun start_opensRequestedCamera() {
        val provider = FakeCameraDeviceProvider()
        val readerFactory = FakeCameraImageReaderFactory()

        val controller = createController(
            provider = provider,
            readerFactory = readerFactory
        )

        controller.start()

        assertEquals(
            "0",
            provider.openedCameraId
        )

        assertNotNull(
            provider.callback
        )

        assertNotNull(
            readerFactory.createdReader
        )

        controller.close()
    }

    @Test
    fun pollFrame_beforeStart_throws() {
        val controller = createController(
            provider = FakeCameraDeviceProvider(),
            readerFactory = FakeCameraImageReaderFactory()
        )

        try {
            controller.pollFrame()
        } catch (exception: IllegalStateException) {
            assertEquals(
                "CameraController is not started",
                exception.message
            )
            return
        }

        throw AssertionError(
            "Expected IllegalStateException"
        )
    }

    @Test
    fun start_twice_throws() {
        val controller = createController(
            provider = FakeCameraDeviceProvider(),
            readerFactory = FakeCameraImageReaderFactory()
        )

        controller.start()

        try {
            controller.start()
        } catch (exception: IllegalStateException) {
            assertEquals(
                "CameraController is already started",
                exception.message
            )

            controller.close()
            return
        }

        controller.close()

        throw AssertionError(
            "Expected IllegalStateException"
        )
    }

    @Test
    fun stop_closesImageReader() {
        val readerFactory = FakeCameraImageReaderFactory()

        val controller = createController(
            provider = FakeCameraDeviceProvider(),
            readerFactory = readerFactory
        )

        controller.start()
        controller.stop()

        assertEquals(
            1,
            readerFactory.createdReader?.closeCount
        )
    }

    @Test
    fun stop_allowsRestart() {
        val readerFactory = FakeCameraImageReaderFactory()

        val controller = createController(
            provider = FakeCameraDeviceProvider(),
            readerFactory = readerFactory
        )

        controller.start()
        controller.stop()

        controller.start()

        assertEquals(
            2,
            readerFactory.createCount
        )

        controller.close()
    }

    @Test
    fun close_beforeStart_doesNothing() {
        val provider = FakeCameraDeviceProvider()
        val readerFactory = FakeCameraImageReaderFactory()

        val controller = createController(
            provider = provider,
            readerFactory = readerFactory
        )

        controller.close()

        assertNull(
            provider.openedCameraId
        )

        assertNull(
            readerFactory.createdReader
        )
    }

    private fun createController(
        provider: CameraDeviceProvider,
        readerFactory: CameraImageReaderFactory
    ): CameraController {
        return CameraController(
            cameraDeviceProvider = provider,
            cameraImageReaderFactory = readerFactory,
            cameraId = "0",
            width = 1280,
            height = 720,
            frameAdapter = CameraFrameAdapter(),
            frameQueue = LatestFrameQueue()
        )
    }

    private class FakeCameraDeviceProvider :
        CameraDeviceProvider {

        var openedCameraId: String? = null

        var callback: CameraDevice.StateCallback? = null

        override fun openCamera(
            cameraId: String,
            callback: CameraDevice.StateCallback
        ) {
            openedCameraId = cameraId
            this.callback = callback
        }

        override fun createCaptureSession(
            camera: CameraDevice,
            surface: Surface,
            callback: CameraCaptureSession.StateCallback
        ) {
            throw UnsupportedOperationException(
                "Capture session is not required by this test"
            )
        }

        override fun startRepeatingCapture(
            camera: CameraDevice,
            session: CameraCaptureSession,
            surface: Surface
        ) {
            throw UnsupportedOperationException(
                "Repeating capture is not required by this test"
            )
        }
    }

    private class FakeCameraImageReaderFactory :
        CameraImageReaderFactory {

        var createCount = 0

        var createdReader: FakeCameraImageReader? = null

        override fun create(
            width: Int,
            height: Int,
            onImageAvailable: (android.media.Image) -> Unit
        ): CameraImageReaderHandle {
            createCount++

            return FakeCameraImageReader().also {
                createdReader = it
            }
        }
    }

    private class FakeCameraImageReader :
        CameraImageReaderHandle {

        var closeCount = 0

        override val surface: Surface
            get() = throw UnsupportedOperationException(
                "Surface is not required by this test"
            )

        override fun close() {
            closeCount++
        }
    }
}