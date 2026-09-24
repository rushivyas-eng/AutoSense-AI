package com.autosense.ai.api.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentInputTest {

    @Test
    fun intentInput_preservesValues() {
        val input = IntentInput(
            source = IntentSource.VOICE,
            content = "turn on the cabin light",
            timestampNanos = 123_456_789L
        )

        assertEquals(IntentSource.VOICE, input.source)
        assertEquals("turn on the cabin light", input.content)
        assertEquals(123_456_789L, input.timestampNanos)
    }
}