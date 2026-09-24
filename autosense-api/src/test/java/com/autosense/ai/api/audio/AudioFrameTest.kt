package com.autosense.ai.api.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFrameTest {

    @Test
    fun audioFrame_preservesMetadataAndData() {
        val audioData = byteArrayOf(
            0x01,
            0x02,
            0x03,
            0x04
        )

        val frame = AudioFrame(
            timestampNanos = 123_456_789L,
            sequenceNumber = 42L,
            sampleRateHz = 16_000,
            channelCount = 1,
            format = AudioFormat.PCM_16_BIT,
            data = audioData
        )

        assertEquals(123_456_789L, frame.timestampNanos)
        assertEquals(42L, frame.sequenceNumber)
        assertEquals(16_000, frame.sampleRateHz)
        assertEquals(1, frame.channelCount)
        assertEquals(AudioFormat.PCM_16_BIT, frame.format)
        assertArrayEquals(audioData, frame.data)
    }

    @Test
    fun audioFrame_supportsEmptyData() {
        val frame = AudioFrame(
            timestampNanos = 100L,
            sequenceNumber = 1L,
            sampleRateHz = 16_000,
            channelCount = 1,
            format = AudioFormat.PCM_16_BIT,
            data = byteArrayOf()
        )

        assertEquals(0, frame.data.size)
    }
}