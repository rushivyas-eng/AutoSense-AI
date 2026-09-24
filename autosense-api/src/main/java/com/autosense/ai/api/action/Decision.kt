package com.autosense.ai.api.action

data class Decision(
    val status: DecisionStatus,
    val reason: DecisionReason,
    val action: Action?,
    val timestampNanos: Long
)
