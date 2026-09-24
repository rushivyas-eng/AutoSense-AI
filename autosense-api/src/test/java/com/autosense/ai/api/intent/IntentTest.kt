package com.autosense.ai.api.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentTest {

    @Test
    fun intent_preservesValues() {
        val intent = Intent(
            type = IntentType.TURN_ON_CABIN_LIGHT,
            confidence = 0.94f,
            timestampNanos = 123_456_789L,
            source = IntentSource.VOICE
        )

        assertEquals(
            IntentType.TURN_ON_CABIN_LIGHT,
            intent.type
        )
        assertEquals(0.94f, intent.confidence)
        assertEquals(123_456_789L, intent.timestampNanos)
        assertEquals(IntentSource.VOICE, intent.source)
    }

    @Test
    fun intent_supportsUnknownValues() {
        val intent = Intent(
            type = IntentType.UNKNOWN,
            confidence = 0.0f,
            timestampNanos = 100L,
            source = IntentSource.UNKNOWN
        )

        assertEquals(IntentType.UNKNOWN, intent.type)
        assertEquals(0.0f, intent.confidence)
        assertEquals(IntentSource.UNKNOWN, intent.source)
    }

    @Test
    fun intent_supportsValueEquality() {
        val intent1 = Intent(
            type = IntentType.TURN_OFF_CABIN_LIGHT,
            confidence = 0.87f,
            timestampNanos = 500L,
            source = IntentSource.VOICE
        )

        val intent2 = Intent(
            type = IntentType.TURN_OFF_CABIN_LIGHT,
            confidence = 0.87f,
            timestampNanos = 500L,
            source = IntentSource.VOICE
        )

        assertEquals(intent1, intent2)
    }
}