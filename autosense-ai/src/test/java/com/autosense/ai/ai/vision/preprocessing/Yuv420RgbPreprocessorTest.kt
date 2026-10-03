package com.autosense.ai.ai.vision.preprocessing

import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameData
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane
import org.junit.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Yuv420RgbPreprocessorTest {

    @Test
    fun preprocess_1280x720_produces320x320Image() {
        val frame =
            createFrame(
                width = 1280,
                height = 720,
                rotationDegrees = 0
            )

        val preprocessor =
            Yuv420RgbPreprocessor()

        val result =
            preprocessor.preprocess(frame)

        assertEquals(
            320,
            result.image.width
        )

        assertEquals(
            320,
            result.image.height
        )

        assertEquals(
            320 * 320 * 3,
            result.image.pixels.size
        )

        assertEquals(
            720,
            result.transform.cropWidth
        )

        assertEquals(
            720,
            result.transform.cropHeight
        )

        assertEquals(
            280,
            result.transform.cropLeft
        )

        assertEquals(
            0,
            result.transform.cropTop
        )
    }

    @Test
    fun preprocess_rotation90_swapsOrientedDimensions() {
        val frame =
            createFrame(
                width = 1280,
                height = 720,
                rotationDegrees = 90
            )

        val result =
            Yuv420RgbPreprocessor()
                .preprocess(frame)

        assertEquals(
            720,
            result.transform.orientedWidth
        )

        assertEquals(
            1280,
            result.transform.orientedHeight
        )

        assertEquals(
            720,
            result.transform.cropWidth
        )

        assertEquals(
            720,
            result.transform.cropHeight
        )

        assertEquals(
            0,
            result.transform.cropLeft
        )

        assertEquals(
            280,
            result.transform.cropTop
        )
    }

    @Test
    fun preprocess_rotation270_swapsOrientedDimensions() {
        val frame =
            createFrame(
                width = 1280,
                height = 720,
                rotationDegrees = 270
            )

        val result =
            Yuv420RgbPreprocessor()
                .preprocess(frame)

        assertEquals(
            720,
            result.transform.orientedWidth
        )

        assertEquals(
            1280,
            result.transform.orientedHeight
        )

        assertEquals(
            720,
            result.transform.cropWidth
        )

        assertEquals(
            720,
            result.transform.cropHeight
        )
    }

    @Test
    fun preprocess_rotation360_normalizesToZero() {
        val frame =
            createFrame(
                width = 640,
                height = 480,
                rotationDegrees = 360
            )

        val result =
            Yuv420RgbPreprocessor()
                .preprocess(frame)

        assertEquals(
            0,
            result.transform.rotationDegrees
        )
    }

    @Test
    fun preprocess_constantBlackFrame_producesBlackRgb() {
        val frame =
            createFrame(
                width = 640,
                height = 480,
                y = 16,
                u = 128,
                v = 128
            )

        val result =
            Yuv420RgbPreprocessor()
                .preprocess(frame)

        assertTrue(
            result.image.pixels.all { byte ->
                (byte.toInt() and 0xFF) == 0
            }
        )
    }

    @Test
    fun preprocess_constantWhiteFrame_producesWhiteRgb() {
        val frame =
            createFrame(
                width = 640,
                height = 480,
                y = 235,
                u = 128,
                v = 128
            )

        val result =
            Yuv420RgbPreprocessor()
                .preprocess(frame)

        assertTrue(
            result.image.pixels.all { byte ->
                (byte.toInt() and 0xFF) >= 254
            }
        )
    }

    private fun createFrame(
        width: Int,
        height: Int,
        rotationDegrees: Int = 0,
        y: Int = 16,
        u: Int = 128,
        v: Int = 128
    ): CameraFrame {

        val yBuffer =
            ByteArray(width * height) {
                y.toByte()
            }

        val chromaWidth = width / 2
        val chromaHeight = height / 2

        val uBuffer =
            ByteArray(chromaWidth * chromaHeight) {
                u.toByte()
            }

        val vBuffer =
            ByteArray(chromaWidth * chromaHeight) {
                v.toByte()
            }

        val data =
            TestFrameData(
                planes = listOf(
                    FramePlane(
                        buffer = ByteBuffer.wrap(yBuffer),
                        rowStride = width,
                        pixelStride = 1
                    ),
                    FramePlane(
                        buffer = ByteBuffer.wrap(uBuffer),
                        rowStride = chromaWidth,
                        pixelStride = 1
                    ),
                    FramePlane(
                        buffer = ByteBuffer.wrap(vBuffer),
                        rowStride = chromaWidth,
                        pixelStride = 1
                    )
                )
            )

        return CameraFrame(
            timestampNanos = 1L,
            sequenceNumber = 1L,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
            data = data
        )
    }

    private class TestFrameData(
        override val planes: List<FramePlane>
    ) : FrameData {

        override val format: FrameFormat =
            FrameFormat.YUV_420_888

        override fun close() {
        }
    }
}