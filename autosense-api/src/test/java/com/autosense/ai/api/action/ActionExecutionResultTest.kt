package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionExecutionResultTest {

    @Test
    fun executionResult_preservesSuccessfulExecution() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 100L
        )

        val result = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.SUCCESS,
            timestampNanos = 150L
        )

        assertEquals(action, result.action)
        assertEquals(ActionExecutionStatus.SUCCESS, result.status)
        assertEquals(150L, result.timestampNanos)
    }

    @Test
    fun executionResult_preservesFailedExecution() {
        val action = Action(
            type = ActionType.TURN_OFF_CABIN_LIGHT,
            timestampNanos = 200L
        )

        val result = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.FAILED,
            timestampNanos = 250L
        )

        assertEquals(action, result.action)
        assertEquals(ActionExecutionStatus.FAILED, result.status)
        assertEquals(250L, result.timestampNanos)
    }

    @Test
    fun executionResult_supportsUnsupportedAction() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 300L
        )

        val result = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.UNSUPPORTED,
            timestampNanos = 350L
        )

        assertEquals(action, result.action)
        assertEquals(ActionExecutionStatus.UNSUPPORTED, result.status)
        assertEquals(350L, result.timestampNanos)
    }

    @Test
    fun executionResult_supportsValueEquality() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 400L
        )

        val result1 = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.SUCCESS,
            timestampNanos = 450L
        )

        val result2 = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.SUCCESS,
            timestampNanos = 450L
        )

        assertEquals(result1, result2)
    }
}