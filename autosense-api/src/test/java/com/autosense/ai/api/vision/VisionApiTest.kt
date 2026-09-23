package com.autosense.ai.api.vision

import org.junit.Assert.assertEquals
import org.junit.Test

class VisionApiTest {

    @Test
    fun boundingBox_preservesCoordinates() {
        val box = BoundingBox(
            left = 0.10f,
            top = 0.20f,
            right = 0.80f,
            bottom = 0.90f
        )

        assertEquals(0.10f, box.left)
        assertEquals(0.20f, box.top)
        assertEquals(0.80f, box.right)
        assertEquals(0.90f, box.bottom)
    }

    @Test
    fun detection_preservesValues() {
        val box = BoundingBox(
            left = 0.10f,
            top = 0.20f,
            right = 0.80f,
            bottom = 0.90f
        )

        val detection = Detection(
            label = "person",
            confidence = 0.94f,
            boundingBox = box
        )

        assertEquals("person", detection.label)
        assertEquals(0.94f, detection.confidence)
        assertEquals(box, detection.boundingBox)
    }

    @Test
    fun visionResult_preservesInferenceMetadata() {
        val detection = Detection(
            label = "vehicle",
            confidence = 0.87f,
            boundingBox = BoundingBox(
                left = 0.20f,
                top = 0.10f,
                right = 0.70f,
                bottom = 0.80f
            )
        )

        val result = VisionResult(
            timestampNanos = 123_456_789L,
            inferenceTimeMs = 42L,
            detections = listOf(detection)
        )

        assertEquals(123_456_789L, result.timestampNanos)
        assertEquals(42L, result.inferenceTimeMs)
        assertEquals(1, result.detections.size)
        assertEquals(detection, result.detections[0])
    }

    @Test
    fun visionResult_supportsEmptyDetectionList() {
        val result = VisionResult(
            timestampNanos = 100L,
            inferenceTimeMs = 15L,
            detections = emptyList()
        )

        assertEquals(100L, result.timestampNanos)
        assertEquals(15L, result.inferenceTimeMs)
        assertEquals(0, result.detections.size)
    }
}