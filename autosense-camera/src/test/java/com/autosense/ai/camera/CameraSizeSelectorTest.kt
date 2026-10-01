package com.autosense.ai.camera

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CameraSizeSelectorTest {

    @Test
    fun exactResolution_isPreferred() {
        val sizes = listOf(
            CameraOutputSize(1920, 1080),
            CameraOutputSize(1280, 720),
            CameraOutputSize(640, 480)
        )

        val selected =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(1280, 720),
            selected
        )
    }

    @Test
    fun sameAspectRatio_isPreferredOverDifferentAspectRatio() {
        val sizes = listOf(
            CameraOutputSize(1280, 960),
            CameraOutputSize(1920, 1080),
            CameraOutputSize(1024, 768)
        )

        val selected =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(1920, 1080),
            selected
        )
    }

    @Test
    fun sameAspectRatio_selectsClosestArea() {
        val sizes = listOf(
            CameraOutputSize(2560, 1440),
            CameraOutputSize(1920, 1080),
            CameraOutputSize(640, 360)
        )

        val selected =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(640, 360),
            selected
        )
    }

    @Test
    fun differentAspectRatio_isUsedWhenNoSameAspectRatioExists() {
        val sizes = listOf(
            CameraOutputSize(1280, 960),
            CameraOutputSize(640, 480),
            CameraOutputSize(1920, 1440)
        )

        val selected =
            CameraSizeSelector.select(
                outputSizes = sizes,
                requestedWidth = 1280,
                requestedHeight = 720
            )

        assertEquals(
            CameraOutputSize(1280, 960),
            selected
        )
    }

    @Test
    fun emptyOutputSizes_throws() {
        assertFailsWith<IllegalArgumentException> {
            CameraSizeSelector.select(
                outputSizes = emptyList(),
                requestedWidth = 1280,
                requestedHeight = 720
            )
        }
    }

    @Test
    fun invalidRequestedWidth_throws() {
        assertFailsWith<IllegalArgumentException> {
            CameraSizeSelector.select(
                outputSizes = listOf(
                    CameraOutputSize(1280, 720)
                ),
                requestedWidth = 0,
                requestedHeight = 720
            )
        }
    }

    @Test
    fun invalidRequestedHeight_throws() {
        assertFailsWith<IllegalArgumentException> {
            CameraSizeSelector.select(
                outputSizes = listOf(
                    CameraOutputSize(1280, 720)
                ),
                requestedWidth = 1280,
                requestedHeight = 0
            )
        }
    }
}