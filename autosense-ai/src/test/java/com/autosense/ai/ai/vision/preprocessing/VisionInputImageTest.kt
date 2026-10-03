package com.autosense.ai.ai.vision.preprocessing

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VisionInputImageTest {

    @Test
    fun validRgbImage_isAccepted() {
        val image =
            VisionInputImage(
                width = 2,
                height = 2,
                pixels = ByteArray(12)
            )

        assertEquals(2, image.width)
        assertEquals(2, image.height)
        assertEquals(12, image.pixels.size)
    }

    @Test
    fun invalidPixelBufferSize_throws() {
        assertFailsWith<IllegalArgumentException> {
            VisionInputImage(
                width = 2,
                height = 2,
                pixels = ByteArray(11)
            )
        }
    }
}