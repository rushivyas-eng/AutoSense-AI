package com.autosense.ai.api.vision

data class VisionResult(
    val timestampNanos: Long,
    val inferenceTimeMs: Long,
    val detections: List<Detection>
)
