package com.autosense.ai.ai.vision.runtime

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameData
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Closeable
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class EfficientDetLite0RealImageInstrumentationTest {

    @Test
    fun realImage_runsVisionPipelineAndReportsDetections() {
        val instrumentation =
            InstrumentationRegistry.getInstrumentation()

        val context = instrumentation.targetContext

        val bitmap = instrumentation.context.assets
            .open(TEST_IMAGE_ASSET)
            .use { input ->
                BitmapFactory.decodeStream(input)
            } ?: error("Failed to decode $TEST_IMAGE_ASSET")

        try {
            require(bitmap.width >= 2 && bitmap.height >= 2) {
                "Test image must be at least 2x2 pixels"
            }

            val frameWidth = bitmap.width - bitmap.width % 2
            val frameHeight = bitmap.height - bitmap.height % 2

            val frame = bitmapToCameraFrame(
                bitmap = bitmap,
                width = frameWidth,
                height = frameHeight,
                timestampNanos = System.nanoTime()
            )

            frame.use { inputFrame ->
                EfficientDetLite0VisionEngine(
                    context = context,
                    labelForClass = { classId -> "class_$classId" }
                ).use { engine ->
                    val result = engine.process(inputFrame)

                    assertTrue(
                        "Inference duration must be non-negative",
                        result.inferenceTimeMs >= 0
                    )

                    result.detections.forEachIndexed { index, detection ->
                        val box = detection.boundingBox

                        assertTrue(
                            "Detection $index must have a finite confidence",
                            detection.confidence.isFinite()
                        )

                        assertTrue(
                            "Detection $index confidence must be in [0, 1]",
                            detection.confidence in 0f..1f
                        )

                        assertTrue(
                            "Detection $index must have valid horizontal bounds",
                            box.left.isFinite() &&
                                    box.right.isFinite() &&
                                    box.left >= 0f &&
                                    box.right <= 1f &&
                                    box.left < box.right
                        )

                        assertTrue(
                            "Detection $index must have valid vertical bounds",
                            box.top.isFinite() &&
                                    box.bottom.isFinite() &&
                                    box.top >= 0f &&
                                    box.bottom <= 1f &&
                                    box.top < box.bottom
                        )
                    }

                    Log.i(
                        TAG,
                        "Real-image pipeline completed: " +
                                "image=${bitmap.width}x${bitmap.height}, " +
                                "frame=${frameWidth}x${frameHeight}, " +
                                "inference=${result.inferenceTimeMs}ms, " +
                                "detections=${result.detections.size}"
                    )

                    result.detections
                        .sortedByDescending { it.confidence }
                        .take(MAX_LOGGED_DETECTIONS)
                        .forEachIndexed { index, detection ->
                            val box = detection.boundingBox

                            Log.i(
                                TAG,
                                "Detection[$index]: " +
                                        "label=${detection.label}, " +
                                        "confidence=${detection.confidence}, " +
                                        "box=(" +
                                        "${box.left},${box.top}," +
                                        "${box.right},${box.bottom})"
                            )
                        }
                }
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun bitmapToCameraFrame(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        timestampNanos: Long
    ): CameraFrame {
        require(width % 2 == 0 && height % 2 == 0)

        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        val yPlane = ByteArray(width * height)
        val chromaWidth = width / 2
        val chromaHeight = height / 2
        val chromaSize = chromaWidth * chromaHeight

        val uPlane = ByteArray(chromaSize)
        val vPlane = ByteArray(chromaSize)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = pixels[y * width + x]

                val red = (color shr 16) and 0xFF
                val green = (color shr 8) and 0xFF
                val blue = color and 0xFF

                val yValue =
                    (0.299 * red +
                            0.587 * green +
                            0.114 * blue).toInt()

                yPlane[y * width + x] =
                    yValue.coerceIn(0, 255).toByte()

                if (x % 2 == 0 && y % 2 == 0) {
                    val chromaIndex =
                        (y / 2) * chromaWidth + x / 2

                    val uValue =
                        (-0.168736 * red -
                                0.331264 * green +
                                0.5 * blue + 128).toInt()

                    val vValue =
                        (0.5 * red -
                                0.418688 * green -
                                0.081312 * blue + 128).toInt()

                    uPlane[chromaIndex] =
                        uValue.coerceIn(0, 255).toByte()

                    vPlane[chromaIndex] =
                        vValue.coerceIn(0, 255).toByte()
                }
            }
        }

        val frameData = TestFrameData(
            planes = listOf(
                FramePlane(
                    buffer = ByteBuffer.wrap(yPlane),
                    rowStride = width,
                    pixelStride = 1
                ),
                FramePlane(
                    buffer = ByteBuffer.wrap(uPlane),
                    rowStride = chromaWidth,
                    pixelStride = 1
                ),
                FramePlane(
                    buffer = ByteBuffer.wrap(vPlane),
                    rowStride = chromaWidth,
                    pixelStride = 1
                )
            )
        )

        return CameraFrame(
            timestampNanos = timestampNanos,
            sequenceNumber = 1L,
            width = width,
            height = height,
            rotationDegrees = 0,
            data = frameData
        )
    }

    private class TestFrameData(
        override val planes: List<FramePlane>
    ) : FrameData, Closeable {

        override val format = FrameFormat.YUV_420_888

        override fun close() {
            // The planes are backed by in-memory buffers.
        }
    }

    private companion object {
        const val TAG = "AutoSenseRealImageTest"
        const val TEST_IMAGE_ASSET = "vision_test.jpg"
        const val MAX_LOGGED_DETECTIONS = 20
    }
}