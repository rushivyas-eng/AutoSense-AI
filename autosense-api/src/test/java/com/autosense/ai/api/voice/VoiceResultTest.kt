package com.autosense.ai.api.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceResultTest {

    @Test
    fun voiceResult_preservesRecognitionMetadata() {
        val result = VoiceResult(
            timestampNanos = 123_456_789L,
            inferenceTimeMs = 250L,
            transcript = "turn on the cabin light",
            confidence = 0.92f
        )

        assertEquals(123_456_789L, result.timestampNanos)
        assertEquals(250L, result.inferenceTimeMs)
        assertEquals("turn on the cabin light", result.transcript)
        assertEquals(0.92f, result.confidence)
    }

    @Test
    fun voiceResult_supportsEmptyTranscript() {
        val result = VoiceResult(
            timestampNanos = 100L,
            inferenceTimeMs = 50L,
            transcript = "",
            confidence = 0.0f
        )

        assertEquals("", result.transcript)
        assertEquals(0.0f, result.confidence)
    }

    @Test
    fun voiceResult_supportsValueEquality() {
        val result1 = VoiceResult(
            timestampNanos = 500L,
            inferenceTimeMs = 100L,
            transcript = "hello",
            confidence = 0.85f
        )

        val result2 = VoiceResult(
            timestampNanos = 500L,
            inferenceTimeMs = 100L,
            transcript = "hello",
            confidence = 0.85f
        )

        assertEquals(result1, result2)
    }
}