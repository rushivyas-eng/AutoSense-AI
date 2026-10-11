package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EfficientDetLite0PostprocessorTest {

    @Test
    fun process_filtersBackgroundAndMapsDetectionToSourceCoordinates() {
        val contract = EfficientDetLite0ModelContract

        val scores = FloatArray(
            contract.CLASSIFICATION_OUTPUT_ANCHORS *
                    contract.CLASSIFICATION_OUTPUT_CLASSES
        )

        val boxes = FloatArray(
            contract.BOX_OUTPUT_ANCHORS *
                    contract.BOX_OUTPUT_VALUES
        )

        // Anchor 0: background has the highest score.
        // It must not produce a detection.
        scores[0] = 0.99f

        // Anchor 1: foreground class 1 has the highest score.
        // Scores represent activated model outputs, not logits.
        val foregroundOffset =
            contract.CLASSIFICATION_OUTPUT_CLASSES

        scores[foregroundOffset] = 0.01f
        scores[foregroundOffset + 1] = 0.90f

        val transform = VisionTransform(
            sourceWidth = 640,
            sourceHeight = 480,
            rotationDegrees = 0,
            orientedWidth = 640,
            orientedHeight = 480,
            cropLeft = 80,
            cropTop = 0,
            cropWidth = 480,
            cropHeight = 480,
            outputWidth = 320,
            outputHeight = 320
        )

        val output = EfficientDetLite0RawOutput(
            classificationScores = scores,
            boxRegression = boxes,
            inferenceTimeMs = 7
        )

        val detections =
            EfficientDetLite0Postprocessor().process(output, transform)

        assertTrue(
            "The foreground prediction should survive postprocessing",
            detections.isNotEmpty()
        )

        assertTrue(
            "Background class must not produce detections",
            detections.none { it.classId == 0 }
        )

        assertTrue(
            "All horizontal coordinates must be normalized",
            detections.all {
                it.left in 0f..1f && it.right in 0f..1f
            }
        )

        assertTrue(
            "All vertical coordinates must be normalized",
            detections.all {
                it.top in 0f..1f && it.bottom in 0f..1f
            }
        )

        assertTrue(
            "Every bounding box must have positive dimensions",
            detections.all {
                it.left < it.right && it.top < it.bottom
            }
        )

        assertEquals(1, detections.first().classId)

        assertEquals(
            0.90f,
            detections.first().confidence,
            0.001f
        )
    }
}