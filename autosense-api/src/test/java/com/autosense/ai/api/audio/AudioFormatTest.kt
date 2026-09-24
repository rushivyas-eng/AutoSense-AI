package com.autosense.ai.api.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFormatTest {

    @Test
    fun audioFormat_containsExpectedFormat() {
        assertEquals(AudioFormat.PCM_16_BIT, AudioFormat.valueOf("PCM_16_BIT"))
    }
}