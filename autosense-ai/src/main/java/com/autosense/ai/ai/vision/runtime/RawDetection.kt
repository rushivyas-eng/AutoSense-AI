package com.autosense.ai.ai.vision.runtime

internal data class RawDetection(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
