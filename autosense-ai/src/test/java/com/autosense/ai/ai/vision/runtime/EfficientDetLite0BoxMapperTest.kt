package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class EfficientDetLite0BoxMapperTest {

    private val mapper =
        EfficientDetLite0BoxMapper()

    @Test
    fun map_noRotation_fullFrame() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 0,
                orientedWidth = 1280,
                orientedHeight = 720,
                cropLeft = 280,
                cropTop = 0,
                cropWidth = 720,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    0.0f,
                    0.0f,
                    320.0f,
                    320.0f
                ),
                transform = transform
            )

        assertEquals(0.21875f, result.left, 0.0001f)
        assertEquals(0.0f, result.top, 0.0001f)
        assertEquals(0.78125f, result.right, 0.0001f)
        assertEquals(1.0f, result.bottom, 0.0001f)
    }

    @Test
    fun map_noRotation_centerBox() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 0,
                orientedWidth = 1280,
                orientedHeight = 720,
                cropLeft = 280,
                cropTop = 0,
                cropWidth = 720,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    80.0f,
                    80.0f,
                    240.0f,
                    240.0f
                ),
                transform = transform
            )

        assertEquals(0.359375f, result.left, 0.0001f)
        assertEquals(0.25f, result.top, 0.0001f)
        assertEquals(0.640625f, result.right, 0.0001f)
        assertEquals(0.75f, result.bottom, 0.0001f)
    }

    @Test
    fun map_rotation90_mapsCenterCropBackToSource() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 90,
                orientedWidth = 720,
                orientedHeight = 1280,
                cropLeft = 0,
                cropTop = 280,
                cropWidth = 720,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    0.0f,
                    0.0f,
                    320.0f,
                    320.0f
                ),
                transform = transform
            )

        /*
         * Rotated image:
         *     720 x 1280
         *
         * Center crop:
         *     x = 0..720
         *     y = 280..1000
         *
         * Inverse 90-degree rotation:
         *     source x = oriented y
         *     source y = sourceHeight - oriented x
         *
         * Result:
         *     x = 280..1000
         *     y = 0..720
         */
        assertEquals(280.0f / 1280.0f, result.left, 0.0001f)
        assertEquals(0.0f, result.top, 0.0001f)
        assertEquals(1000.0f / 1280.0f, result.right, 0.0001f)
        assertEquals(1.0f, result.bottom, 0.0001f)
    }

    @Test
    fun map_rotation180_reversesBothAxes() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 180,
                orientedWidth = 1280,
                orientedHeight = 720,
                cropLeft = 280,
                cropTop = 0,
                cropWidth = 720,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    0.0f,
                    0.0f,
                    80.0f,
                    80.0f
                ),
                transform = transform
            )

        /*
         * Model:
         *     0..80 in both axes
         *
         * Crop-local:
         *     0..180 in both axes
         *
         * Oriented:
         *     x = 280..460
         *     y = 0..180
         *
         * Inverse 180:
         *     x = 820..1000
         *     y = 540..720
         */
        assertEquals(820.0f / 1280.0f, result.left, 0.0001f)
        assertEquals(540.0f / 720.0f, result.top, 0.0001f)
        assertEquals(1000.0f / 1280.0f, result.right, 0.0001f)
        assertEquals(1.0f, result.bottom, 0.0001f)
    }

    @Test
    fun map_rotation270_mapsCenterCropBackToSource() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 270,
                orientedWidth = 720,
                orientedHeight = 1280,
                cropLeft = 0,
                cropTop = 280,
                cropWidth = 720,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    0.0f,
                    0.0f,
                    320.0f,
                    320.0f
                ),
                transform = transform
            )

        /*
         * Center crop:
         *     x = 0..720
         *     y = 280..1000
         *
         * Inverse 270:
         *     source x = sourceWidth - orientedY
         *     source y = orientedX
         *
         * Result:
         *     x = 280..1000
         *     y = 0..720
         */
        assertEquals(280.0f / 1280.0f, result.left, 0.0001f)
        assertEquals(0.0f, result.top, 0.0001f)
        assertEquals(1000.0f / 1280.0f, result.right, 0.0001f)
        assertEquals(1.0f, result.bottom, 0.0001f)
    }

    @Test
    fun map_clampsCoordinatesToSourceBounds() {
        val transform =
            transform(
                sourceWidth = 1280,
                sourceHeight = 720,
                rotationDegrees = 0,
                orientedWidth = 1280,
                orientedHeight = 720,
                cropLeft = 0,
                cropTop = 0,
                cropWidth = 1280,
                cropHeight = 720
            )

        val result =
            mapper.map(
                box = floatArrayOf(
                    -40.0f,
                    -20.0f,
                    360.0f,
                    350.0f
                ),
                transform = transform
            )

        assertEquals(0.0f, result.left, 0.0001f)
        assertEquals(0.0f, result.top, 0.0001f)
        assertEquals(1.0f, result.right, 0.0001f)
        assertEquals(1.0f, result.bottom, 0.0001f)
    }

    @Test
    fun map_rejectsInvalidBoxSize() {
        val transform =
            transform(
                sourceWidth = 320,
                sourceHeight = 320,
                rotationDegrees = 0,
                orientedWidth = 320,
                orientedHeight = 320,
                cropLeft = 0,
                cropTop = 0,
                cropWidth = 320,
                cropHeight = 320
            )

        assertThrows(IllegalArgumentException::class.java) {
            mapper.map(
                box = floatArrayOf(
                    0.0f,
                    0.0f,
                    0.5f
                ),
                transform = transform
            )
        }
    }

    @Test
    fun map_rejectsNonFiniteBox() {
        val transform =
            transform(
                sourceWidth = 320,
                sourceHeight = 320,
                rotationDegrees = 0,
                orientedWidth = 320,
                orientedHeight = 320,
                cropLeft = 0,
                cropTop = 0,
                cropWidth = 320,
                cropHeight = 320
            )

        assertThrows(IllegalArgumentException::class.java) {
            mapper.map(
                box = floatArrayOf(
                    Float.NaN,
                    0.0f,
                    1.0f,
                    1.0f
                ),
                transform = transform
            )
        }
    }

    private fun transform(
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        orientedWidth: Int,
        orientedHeight: Int,
        cropLeft: Int,
        cropTop: Int,
        cropWidth: Int,
        cropHeight: Int
    ): VisionTransform {
        return VisionTransform(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            rotationDegrees = rotationDegrees,
            orientedWidth = orientedWidth,
            orientedHeight = orientedHeight,
            cropLeft = cropLeft,
            cropTop = cropTop,
            cropWidth = cropWidth,
            cropHeight = cropHeight,
            outputWidth = 320,
            outputHeight = 320
        )
    }
}