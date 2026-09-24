package com.autosense.ai.api.action

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionExecutorTest {

    @Test
    fun executor_returnsExecutionResult() {
        val action = Action(
            type = ActionType.TURN_ON_CABIN_LIGHT,
            timestampNanos = 100L
        )

        val expectedResult = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.SUCCESS,
            timestampNanos = 150L
        )

        val executor = FakeActionExecutor(expectedResult)

        val result = executor.execute(action)

        assertEquals(expectedResult, result)
    }

    @Test
    fun executor_canReportFailedExecution() {
        val action = Action(
            type = ActionType.TURN_OFF_CABIN_LIGHT,
            timestampNanos = 200L
        )

        val expectedResult = ActionExecutionResult(
            action = action,
            status = ActionExecutionStatus.FAILED,
            timestampNanos = 250L
        )

        val executor = FakeActionExecutor(expectedResult)

        val result = executor.execute(action)

        assertEquals(ActionExecutionStatus.FAILED, result.status)
        assertEquals(action, result.action)
    }

    private class FakeActionExecutor(
        private val result: ActionExecutionResult
    ) : ActionExecutor {

        override fun execute(action: Action): ActionExecutionResult {
            return result
        }
    }
}