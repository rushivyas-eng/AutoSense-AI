package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionTest {

    @Test
    fun action_preservesValues() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 123_456_789L
        )

        assertEquals(
            ActionType.TURN_ON_CABIN_LIGHT,
            action.type
        )
        assertEquals(
            123_456_789L,
            action.timestampNanos
        )
    }

    @Test
    fun action_supportsValueEquality() {
        val action1 = Action(
            type = ActionType.TURN_OFF_CABIN_LIGHT,
            timestampNanos = 500L
        )

        val action2 = Action(
            type = ActionType.TURN_OFF_CABIN_LIGHT,
            timestampNanos = 500L
        )

        assertEquals(action1, action2)
    }
}