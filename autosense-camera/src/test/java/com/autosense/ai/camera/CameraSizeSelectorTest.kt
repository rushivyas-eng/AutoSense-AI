package com.autosense.ai.camera

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraSizeSelectorTest {

    @Test
    fun exactMatch_isSelected() {
        val sizes = listOf(
            CameraOutputSize(1920, 1080),
            CameraOutputSize(1280, 720),
            CameraOutputSize(640, 480)
        )

        val result =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(1280, 720),
            result
        )
    }

    @Test
    fun closestArea_isSelectedWhenExactMatchMissing() {
        val sizes = listOf(
            CameraOutputSize(1920, 1080),
            CameraOutputSize(1280, 800),
            CameraOutputSize(640, 480)
        )

        val result =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(1280, 800),
            result
        )
    }

    @Test
    fun emptySizes_throws() {
        try {
            CameraSizeSelector.select(
                outputSizes = emptyList(),
                requestedWidth = 1280,
                requestedHeight = 720
            )
        } catch (exception: IllegalArgumentException) {
            assertEquals(
                "Camera does not support YUV_420_888 output",
                exception.message
            )
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun requestedSize_largerThanAvailable_selectsClosestArea() {
        val sizes = listOf(
            CameraOutputSize(640, 480),
            CameraOutputSize(1280, 720)
        )

        val result =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1920,
                requestedHeight = 1080
            )

        assertEquals(
            CameraOutputSize(1280, 720),
            result
        )
    }
}