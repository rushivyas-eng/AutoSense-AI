package com.autosense.ai.api.vehicle

import org.junit.Assert.assertEquals
import org.junit.Test

class IgnitionStateTest {

    @Test
    fun ignitionState_containsExpectedStates() {
        assertEquals(IgnitionState.OFF, IgnitionState.valueOf("OFF"))
        assertEquals(IgnitionState.ACC, IgnitionState.valueOf("ACC"))
        assertEquals(IgnitionState.ON, IgnitionState.valueOf("ON"))
        assertEquals(IgnitionState.UNKNOWN, IgnitionState.valueOf("UNKNOWN"))
    }
}