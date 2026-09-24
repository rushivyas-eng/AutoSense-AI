package com.autosense.ai.api.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentEngineTest {

    @Test
    fun engine_convertsInputToIntent() {
        val input = IntentInput(
            source = IntentSource.VOICE,
            content = "turn on the cabin light",
            timestampNanos = 123_456_789L
        )

        val expectedIntent = Intent(
            type = IntentType.TURN_ON_CABIN_LIGHT,
            confidence = 0.95f,
            timestampNanos = input.timestampNanos,
            source = input.source
        )

        val engine = FakeIntentEngine(expectedIntent)

        val result = engine.process(input)

        assertEquals(expectedIntent, result)
    }

    private class FakeIntentEngine(
        private val intent: Intent
    ) : IntentEngine {

        override fun process(input: IntentInput): Intent {
            return intent
        }
    }
}