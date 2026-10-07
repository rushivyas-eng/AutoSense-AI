package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.exp

class EfficientDetLite0BoxDecoderTest {

    private val decoder = EfficientDetLite0BoxDecoder()

    @Test
    fun decode_zeroRegression_returnsAnchor() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            0.0f,
            0.0f
        )

        val result = decoder.decode(
            rawBox = rawBox,
            anchor = anchor
        )

        assertArrayEquals(
            anchor,
            result,
            EPSILON
        )
    }

    @Test
    fun decode_centerOffsetMovesBoxRelativeToAnchor() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        val rawBox = floatArrayOf(
            0.25f,
            -0.25f,
            0.0f,
            0.0f
        )

        val result = decoder.decode(
            rawBox = rawBox,
            anchor = anchor
        )

        /*
         * Anchor:
         *
         * centerY = 40
         * centerX = 50
         * height = 40
         * width  = 40
         *
         * Decoded:
         *
         * centerY = 0.25 * 40 + 40 = 50
         * centerX = -0.25 * 40 + 50 = 40
         *
         * Size remains 40 x 40.
         */
        assertArrayEquals(
            floatArrayOf(
                30.0f,
                20.0f,
                70.0f,
                60.0f
            ),
            result,
            EPSILON
        )
    }

    @Test
    fun decode_positiveSizeRegression_expandsBox() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            1.0f,
            1.0f
        )

        val result = decoder.decode(
            rawBox = rawBox,
            anchor = anchor
        )

        val expectedSize =
            exp(1.0) * 40.0

        val expectedHalfSize =
            expectedSize / 2.0

        assertEquals(
            40.0 - expectedHalfSize,
            result[0].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            50.0 - expectedHalfSize,
            result[1].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            40.0 + expectedHalfSize,
            result[2].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            50.0 + expectedHalfSize,
            result[3].toDouble(),
            EPSILON_DOUBLE
        )
    }

    @Test
    fun decode_negativeSizeRegression_shrinksBox() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            -1.0f,
            -1.0f
        )

        val result = decoder.decode(
            rawBox = rawBox,
            anchor = anchor
        )

        val expectedSize =
            exp(-1.0) * 40.0

        val expectedHalfSize =
            expectedSize / 2.0

        assertEquals(
            40.0 - expectedHalfSize,
            result[0].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            50.0 - expectedHalfSize,
            result[1].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            40.0 + expectedHalfSize,
            result[2].toDouble(),
            EPSILON_DOUBLE
        )

        assertEquals(
            50.0 + expectedHalfSize,
            result[3].toDouble(),
            EPSILON_DOUBLE
        )
    }

    @Test
    fun decode_usesAnchorDimensionsIndependentlyForWidthAndHeight() {
        val anchor = floatArrayOf(
            10.0f,
            20.0f,
            50.0f,
            100.0f
        )

        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            0.0f,
            0.0f
        )

        val result = decoder.decode(
            rawBox = rawBox,
            anchor = anchor
        )

        assertArrayEquals(
            anchor,
            result,
            EPSILON
        )
    }

    @Test
    fun decode_invalidRawBoxSize_throws() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        try {
            decoder.decode(
                rawBox = floatArrayOf(0.0f, 0.0f, 0.0f),
                anchor = anchor
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun decode_invalidAnchorSize_throws() {
        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            0.0f,
            0.0f
        )

        try {
            decoder.decode(
                rawBox = rawBox,
                anchor = floatArrayOf(
                    20.0f,
                    30.0f,
                    60.0f
                )
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun decode_invalidAnchorGeometry_throws() {
        val rawBox = floatArrayOf(
            0.0f,
            0.0f,
            0.0f,
            0.0f
        )

        try {
            decoder.decode(
                rawBox = rawBox,
                anchor = floatArrayOf(
                    60.0f,
                    30.0f,
                    20.0f,
                    70.0f
                )
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun decode_nonFiniteRawValue_throws() {
        val anchor = floatArrayOf(
            20.0f,
            30.0f,
            60.0f,
            70.0f
        )

        try {
            decoder.decode(
                rawBox = floatArrayOf(
                    Float.NaN,
                    0.0f,
                    0.0f,
                    0.0f
                ),
                anchor = anchor
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    companion object {
        private const val EPSILON = 0.0001f
        private const val EPSILON_DOUBLE = 0.0001
    }
}