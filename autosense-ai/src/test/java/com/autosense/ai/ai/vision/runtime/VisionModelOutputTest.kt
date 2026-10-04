package com.autosense.ai.ai.vision.runtime

import org.junit.Test
import kotlin.test.assertFailsWith

class VisionModelOutputTest {

    @Test
    fun validOutput_isAccepted() {
        VisionModelOutput(
            detections = emptyList(),
            inferenceTimeMs = 0L
        )
    }

    @Test
    fun negativeInferenceTime_isRejected() {
        assertFailsWith<IllegalArgumentException> {
            VisionModelOutput(
                detections = emptyList(),
                inferenceTimeMs = -1L
            )
        }
    }
}