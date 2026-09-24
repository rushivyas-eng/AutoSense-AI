package com.autosense.ai.api.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentTypeTest {

    @Test
    fun intentType_containsExpectedValues() {
        assertEquals(
            IntentType.TURN_ON_CABIN_LIGHT,
            IntentType.valueOf("TURN_ON_CABIN_LIGHT")
        )

        assertEquals(
            IntentType.TURN_OFF_CABIN_LIGHT,
            IntentType.valueOf("TURN_OFF_CABIN_LIGHT")
        )

        assertEquals(
            IntentType.UNKNOWN,
            IntentType.valueOf("UNKNOWN")
        )
    }
}