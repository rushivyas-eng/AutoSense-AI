package com.autosense.ai.ai.vision.runtime

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VisionModelDetectionTest {

    @Test
    fun validDetection_isAccepted() {
        val detection = VisionModelDetection(
            classId = 3,
            confidence = 0.92f,
            left = 0.1f,
            top = 0.2f,
            right = 0.8f,
            bottom = 0.9f
        )

        assertEquals(3, detection.classId)
        assertEquals(0.92f, detection.confidence)
    }

    @Test
    fun negativeClassId_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            VisionModelDetection(
                classId = -1,
                confidence = 0.9f,
                left = 0.1f,
                top = 0.1f,
                right = 0.8f,
                bottom = 0.8f
            )
        }
    }

    @Test
    fun invalidConfidence_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            VisionModelDetection(
                classId = 1,
                confidence = 1.1f,
                left = 0.1f,
                top = 0.1f,
                right = 0.8f,
                bottom = 0.8f
            )
        }
    }

    @Test
    fun invalidBoundingBox_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            VisionModelDetection(
                classId = 1,
                confidence = 0.9f,
                left = 0.8f,
                top = 0.1f,
                right = 0.2f,
                bottom = 0.8f
            )
        }
    }
}