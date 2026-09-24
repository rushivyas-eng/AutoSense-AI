package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionStatusTest {

    @Test
    fun decisionStatus_containsExpectedValues() {
        assertEquals(
            DecisionStatus.ALLOWED,
            DecisionStatus.valueOf("ALLOWED")
        )

        assertEquals(
            DecisionStatus.REJECTED,
            DecisionStatus.valueOf("REJECTED")
        )
    }
}