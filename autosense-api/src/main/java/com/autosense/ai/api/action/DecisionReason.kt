package com.autosense.ai.api.action

enum class DecisionReason {
    ALLOWED,
    UNKNOWN_INTENT,
    LOW_CONFIDENCE,
    UNSUPPORTED_ACTION,
    INVALID_CONTEXT
}