package com.autosense.ai.api.voice

import com.autosense.ai.api.audio.AudioFormat
import com.autosense.ai.api.audio.AudioFrame
import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceEngineTest {

    @Test
    fun engine_processesAudioFrame() {
        val frame = AudioFrame(
            timestampNanos = 123_456_789L,
            sequenceNumber = 1L,
            sampleRateHz = 16_000,
            channelCount = 1,
            format = AudioFormat.PCM_16_BIT,
            data = byteArrayOf(0x01, 0x02)
        )

        val expectedResult = VoiceResult(
            timestampNanos = frame.timestampNanos,
            inferenceTimeMs = 100L,
            transcript = "turn on the light",
            confidence = 0.91f
        )

        val engine = FakeVoiceEngine(expectedResult)

        val result = engine.process(frame)

        assertEquals(expectedResult, result)
    }

    private class FakeVoiceEngine(
        private val result: VoiceResult
    ) : VoiceEngine {

        override fun process(frame: AudioFrame): VoiceResult {
            return result
        }
    }
}