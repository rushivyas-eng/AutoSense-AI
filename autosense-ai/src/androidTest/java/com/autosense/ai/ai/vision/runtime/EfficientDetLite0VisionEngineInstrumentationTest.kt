package com.autosense.ai.ai.vision.runtime

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameData
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class EfficientDetLite0VisionEngineInstrumentationTest {

    @Test
    fun syntheticFrame_runsCompleteVisionPipeline() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val timestampNanos = 123_456_789L

        val frame = createSyntheticFrame(
            width = FRAME_WIDTH,
            height = FRAME_HEIGHT,
            timestampNanos = timestampNanos
        )

        EfficientDetLite0VisionEngine(
            context = context,
            labelForClass = EfficientDetLite0LabelMap::getLabel
        ).use { engine ->

            frame.use { inputFrame ->
                val result = engine.process(inputFrame)

                // The result must correspond to the input frame.
                assertEquals(
                    "Frame timestamp must be preserved",
                    timestampNanos,
                    result.timestampNanos
                )

                assertTrue(
                    "Inference duration must be non-negative",
                    result.inferenceTimeMs >= 0
                )

                // Validate every detection returned by postprocessing.
                result.detections.forEachIndexed { index, detection ->
                    assertTrue(
                        "Detection $index must have a non-blank label",
                        detection.label.isNotBlank()
                    )

                    assertTrue(
                        "Detection $index confidence must be finite",
                        detection.confidence.isFinite()
                    )

                    assertTrue(
                        "Detection $index confidence must be in [0, 1]",
                        detection.confidence in 0f..1f
                    )

                    val box = detection.boundingBox

                    assertTrue(
                        "Detection $index left must be finite",
                        box.left.isFinite()
                    )

                    assertTrue(
                        "Detection $index top must be finite",
                        box.top.isFinite()
                    )

                    assertTrue(
                        "Detection $index right must be finite",
                        box.right.isFinite()
                    )

                    assertTrue(
                        "Detection $index bottom must be finite",
                        box.bottom.isFinite()
                    )

                    assertTrue(
                        "Detection $index left must be within the frame",
                        box.left >= 0f
                    )

                    assertTrue(
                        "Detection $index top must be within the frame",
                        box.top >= 0f
                    )

                    assertTrue(
                        "Detection $index right must be within the frame",
                        box.right <= FRAME_WIDTH.toFloat()
                    )

                    assertTrue(
                        "Detection $index bottom must be within the frame",
                        box.bottom <= FRAME_HEIGHT.toFloat()
                    )

                    assertTrue(
                        "Detection $index must have positive width",
                        box.left < box.right
                    )

                    assertTrue(
                        "Detection $index must have positive height",
                        box.top < box.bottom
                    )
                }

                Log.i(
                    TAG,
                    "Pipeline validation passed: " +
                            "frame=${FRAME_WIDTH}x${FRAME_HEIGHT}, " +
                            "inference=${result.inferenceTimeMs} ms, " +
                            "detections=${result.detections.size}"
                )

                result.detections
                    .take(MAX_LOGGED_DETECTIONS)
                    .forEachIndexed { index, detection ->
                        val box = detection.boundingBox

                        Log.i(
                            TAG,
                            "Detection[$index]: " +
                                    "label=${detection.label}, " +
                                    "confidence=${detection.confidence}, " +
                                    "box=(" +
                                    "${box.left}, ${box.top}, " +
                                    "${box.right}, ${box.bottom})"
                        )
                    }
            }
        }
    }

    private fun createSyntheticFrame(
        width: Int,
        height: Int,
        timestampNanos: Long
    ): CameraFrame {
        require(width % 2 == 0)
        require(height % 2 == 0)

        // Neutral-chroma YUV frame with a constant luma value.
        // This exercises the real YUV-to-RGB preprocessing path.
        val yPlane = ByteArray(width * height) {
            LUMA_VALUE.toByte()
        }

        val chromaWidth = width / 2
        val chromaHeight = height / 2
        val chromaSize = chromaWidth * chromaHeight

        val uPlane = ByteArray(chromaSize) {
            CHROMA_NEUTRAL_VALUE.toByte()
        }

        val vPlane = ByteArray(chromaSize) {
            CHROMA_NEUTRAL_VALUE.toByte()
        }

        val frameData = SyntheticFrameData(
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

    private class SyntheticFrameData(
        override val planes: List<FramePlane>
    ) : FrameData {

        override val format: FrameFormat =
            FrameFormat.YUV_420_888

        override fun close() {
            // The synthetic frame uses in-memory ByteBuffers.
            // No external camera resource needs to be released.
        }
    }

    private companion object {
        const val TAG = "AutoSenseVisionTest"

        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480

        const val LUMA_VALUE = 120
        const val CHROMA_NEUTRAL_VALUE = 128

        const val MAX_LOGGED_DETECTIONS = 10
    }
}