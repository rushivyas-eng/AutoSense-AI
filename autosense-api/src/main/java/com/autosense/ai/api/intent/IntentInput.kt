package com.autosense.ai.api.intent

data class IntentInput(
    val source: IntentSource,
    val content: String,
    val timestampNanos: Long
)
