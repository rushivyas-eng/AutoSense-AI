package com.autosense.ai.api.voice

data class VoiceResult(
    val timestampNanos: Long,
    val inferenceTimeMs: Long,
    val transcript: String,
    val confidence: Float
)
