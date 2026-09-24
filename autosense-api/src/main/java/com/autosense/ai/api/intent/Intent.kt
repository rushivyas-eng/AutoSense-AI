package com.autosense.ai.api.intent

data class Intent(
    val type: IntentType,
    val confidence: Float,
    val timestampNanos: Long,
    val source: IntentSource
)
