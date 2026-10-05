package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionInputImage
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EfficientDetLite0InputEncoderTest {

    private val encoder = EfficientDetLite0InputEncoder()

    @Test
    fun encode_preservesRgbPixels() {
        val pixels = ByteArray(320 * 320 * 3)

        pixels[0] = 0
        pixels[1] = 127
        pixels[2] = 255.toByte()

        val image = VisionInputImage(
            width = 320,
            height = 320,
            pixels = pixels
        )

        val encoded = encoder.encode(image)

        assertArrayEquals(pixels, encoded)
    }

    @Test
    fun encode_returnsExpectedSize() {
        val image = VisionInputImage(
            width = 320,
            height = 320,
            pixels = ByteArray(320 * 320 * 3)
        )

        val encoded = encoder.encode(image)

        assertEquals(
            320 * 320 * 3,
            encoded.size
        )
    }
}