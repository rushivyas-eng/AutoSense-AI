package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionTransform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EfficientDetLite0PostprocessorTest {

    @Test
    fun process_keepsClassZeroAndMapsDetectionToSourceCoordinates() {
        val contract = EfficientDetLite0ModelContract

        val scores = FloatArray(
            contract.CLASSIFICATION_OUTPUT_ANCHORS *
                    contract.CLASSIFICATION_OUTPUT_CLASSES
        )

        val boxes = FloatArray(
            contract.BOX_OUTPUT_ANCHORS *
                    contract.BOX_OUTPUT_VALUES
        )

        // Anchor 0: class 0 is a valid foreground class.
        scores[0] = 0.99f

        // Anchor 1: class 1 is another valid foreground class.
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
            "Valid foreground predictions should survive postprocessing",
            detections.isNotEmpty()
        )

        assertTrue(
            "Class 0 should remain a valid detection",
            detections.any { it.classId == 0 }
        )

        assertTrue(
            "Class 1 should remain a valid detection",
            detections.any { it.classId == 1 }
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

        val classZeroDetection =
            detections.first { it.classId == 0 }

        assertEquals(
            0.99f,
            classZeroDetection.confidence,
            0.001f
        )
    }
}