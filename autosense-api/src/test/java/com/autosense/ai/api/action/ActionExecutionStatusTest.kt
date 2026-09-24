package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionExecutionStatusTest {

    @Test
    fun actionExecutionStatus_containsExpectedValues() {
        assertEquals(
            ActionExecutionStatus.SUCCESS,
            ActionExecutionStatus.valueOf("SUCCESS")
        )

        assertEquals(
            ActionExecutionStatus.FAILED,
            ActionExecutionStatus.valueOf("FAILED")
        )

        assertEquals(
            ActionExecutionStatus.UNSUPPORTED,
            ActionExecutionStatus.valueOf("UNSUPPORTED")
        )
    }
}