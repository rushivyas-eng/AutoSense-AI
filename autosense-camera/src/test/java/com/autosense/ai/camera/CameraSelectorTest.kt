package com.autosense.ai.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraSelectorTest {

    @Test
    fun backCamera_isPreferred() {
        val cameras = listOf(
            CameraDescriptor(
                cameraId = "0",
                facing = CameraFacing.FRONT
            ),
            CameraDescriptor(
                cameraId = "1",
                facing = CameraFacing.BACK
            )
        )

        val result =
            CameraSelector.select(cameras)

        assertEquals(
            CameraDescriptor(
                cameraId = "1",
                facing = CameraFacing.BACK
            ),
            result
        )
    }

    @Test
    fun firstBackCamera_isSelectedWhenMultipleBackCamerasExist() {
        val cameras = listOf(
            CameraDescriptor(
                cameraId = "0",
                facing = CameraFacing.BACK
            ),
            CameraDescriptor(
                cameraId = "1",
                facing = CameraFacing.BACK
            )
        )

        val result =
            CameraSelector.select(cameras)

        assertEquals(
            "0",
            result.cameraId
        )
    }

    @Test
    fun firstCamera_isSelectedWhenNoBackCameraExists() {
        val cameras = listOf(
            CameraDescriptor(
                cameraId = "0",
                facing = CameraFacing.FRONT
            ),
            CameraDescriptor(
                cameraId = "1",
                facing = CameraFacing.EXTERNAL
            )
        )

        val result =
            CameraSelector.select(cameras)

        assertEquals(
            "0",
            result.cameraId
        )
    }

    @Test
    fun unknownCamera_canBeSelectedAsFallback() {
        val cameras = listOf(
            CameraDescriptor(
                cameraId = "0",
                facing = CameraFacing.UNKNOWN
            )
        )

        val result =
            CameraSelector.select(cameras)

        assertEquals(
            "0",
            result.cameraId
        )
    }

    @Test
    fun emptyCameraList_throws() {
        try {
            CameraSelector.select(
                emptyList()
            )
        } catch (exception: IllegalArgumentException) {
            assertEquals(
                "No camera is available",
                exception.message
            )
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }
}