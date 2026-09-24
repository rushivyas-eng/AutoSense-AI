package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionReasonTest {

    @Test
    fun decisionReason_containsExpectedValues() {
        assertEquals(
            DecisionReason.ALLOWED,
            DecisionReason.valueOf("ALLOWED")
        )

        assertEquals(
            DecisionReason.UNKNOWN_INTENT,
            DecisionReason.valueOf("UNKNOWN_INTENT")
        )

        assertEquals(
            DecisionReason.LOW_CONFIDENCE,
            DecisionReason.valueOf("LOW_CONFIDENCE")
        )

        assertEquals(
            DecisionReason.UNSUPPORTED_ACTION,
            DecisionReason.valueOf("UNSUPPORTED_ACTION")
        )

        assertEquals(
            DecisionReason.INVALID_CONTEXT,
            DecisionReason.valueOf("INVALID_CONTEXT")
        )
    }
}