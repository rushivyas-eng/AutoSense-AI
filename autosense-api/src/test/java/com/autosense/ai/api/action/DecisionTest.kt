package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DecisionTest {

    @Test
    fun allowedDecision_containsAction() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 123L
        )

        val decision = Decision(
            status = DecisionStatus.ALLOWED,
            reason = DecisionReason.ALLOWED,
            action = action,
            timestampNanos = 123L
        )

        assertEquals(DecisionStatus.ALLOWED, decision.status)
        assertEquals(DecisionReason.ALLOWED, decision.reason)
        assertEquals(action, decision.action)
        assertEquals(123L, decision.timestampNanos)
    }

    @Test
    fun rejectedDecision_containsNoAction() {
        val decision = Decision(
            status = DecisionStatus.REJECTED,
            reason = DecisionReason.LOW_CONFIDENCE,
            action = null,
            timestampNanos = 456L
        )

        assertEquals(DecisionStatus.REJECTED, decision.status)
        assertEquals(DecisionReason.LOW_CONFIDENCE, decision.reason)
        assertNull(decision.action)
        assertEquals(456L, decision.timestampNanos)
    }

    @Test
    fun decision_supportsValueEquality() {
        val action = Action(
            type = ActionType.TURN_OFF_CABIN_LIGHT,
            timestampNanos = 500L
        )

        val decision1 = Decision(
            status = DecisionStatus.ALLOWED,
            reason = DecisionReason.ALLOWED,
            action = action,
            timestampNanos = 500L
        )

        val decision2 = Decision(
            status = DecisionStatus.ALLOWED,
            reason = DecisionReason.ALLOWED,
            action = action,
            timestampNanos = 500L
        )

        assertEquals(decision1, decision2)
    }
}