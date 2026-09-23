package com.autosense.ai.api.vision

data class Detection(
    val label: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)
