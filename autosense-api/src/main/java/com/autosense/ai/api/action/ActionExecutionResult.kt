package com.autosense.ai.api.action

data class ActionExecutionResult(
    val action: Action,
    val status: ActionExecutionStatus,
    val timestampNanos: Long
)
