package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionTypeTest {

    @Test
    fun actionType_containsExpectedValues() {
        assertEquals(
            ActionType.TURN_ON_CABIN_LIGHT,
            ActionType.valueOf("TURN_ON_CABIN_LIGHT")
        )

        assertEquals(
            ActionType.TURN_OFF_CABIN_LIGHT,
            ActionType.valueOf("TURN_OFF_CABIN_LIGHT")
        )
    }
}