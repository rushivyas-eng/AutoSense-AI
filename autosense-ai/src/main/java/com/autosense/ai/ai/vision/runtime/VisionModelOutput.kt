package com.autosense.ai.ai.vision.runtime
internal data class VisionModelOutput(
    val detections: List<VisionModelDetection>,
    val inferenceTimeMs: Long
) {
    init {
        require(inferenceTimeMs >= 0) {
            "inferenceTimeMs must not be negative"
        }
    }
}
