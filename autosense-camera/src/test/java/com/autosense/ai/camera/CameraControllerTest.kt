package com.autosense.ai.camera

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CameraControllerTest {

    @Test
    fun start_usesResolvedConfiguration() {
        val provider = FakeCameraDeviceProvider()
        val readerFactory = FakeCameraImageReaderFactory()
        val configurationProvider =
            FakeCameraConfigurationProvider()

        val controller = createController(
            provider = provider,
            readerFactory = readerFactory,
            configurationProvider = configurationProvider
        )

        controller.start()

        assertEquals(
            "camera-1",
            provider.openedCameraId
        )

        assertEquals(
            1280,
            readerFactory.createdWidth
        )

        assertEquals(
            720,
            readerFactory.createdHeight
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
            readerFactory = FakeCameraImageReaderFactory(),
            configurationProvider =
                FakeCameraConfigurationProvider()
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
            readerFactory = FakeCameraImageReaderFactory(),
            configurationProvider =
                FakeCameraConfigurationProvider()
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
            readerFactory = readerFactory,
            configurationProvider =
                FakeCameraConfigurationProvider()
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
            readerFactory = readerFactory,
            configurationProvider =
                FakeCameraConfigurationProvider()
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
            readerFactory = readerFactory,
            configurationProvider =
                FakeCameraConfigurationProvider()
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
        readerFactory: CameraImageReaderFactory,
        configurationProvider: CameraConfigurationProvider
    ): CameraController {
        return CameraController(
            cameraDeviceProvider = provider,
            cameraImageReaderFactory = readerFactory,
            cameraConfigurationProvider =
                configurationProvider,
            frameAdapter = CameraFrameAdapter(),
            frameQueue = LatestFrameQueue()
        )
    }

    private class FakeCameraConfigurationProvider :
        CameraConfigurationProvider {

        override fun getConfiguration():
                CameraConfiguration {
            return CameraConfiguration(
                cameraId = "camera-1",
                width = 1280,
                height = 720,
                rotationDegrees = 90
            )
        }
    }

    private class FakeCameraImageReaderFactory :
        CameraImageReaderFactory {

        var createCount = 0

        var createdWidth: Int? = null

        var createdHeight: Int? = null

        var createdReader: FakeCameraImageReader? = null

        override fun create(
            width: Int,
            height: Int,
            onImageAvailable:
                (android.media.Image) -> Unit
        ): CameraImageReaderHandle {

            createCount++

            createdWidth = width
            createdHeight = height

            return FakeCameraImageReader().also {
                createdReader = it
            }
        }
    }

    private class FakeCameraDeviceProvider :
        CameraDeviceProvider {

        var openedCameraId: String? = null

        var callback: CameraDeviceProvider.Callback? = null

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
            callback:
            CameraDeviceProvider.CaptureSessionCallback
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