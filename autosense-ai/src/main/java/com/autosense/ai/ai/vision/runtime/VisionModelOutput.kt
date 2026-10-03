package com.autosense.ai.ai.vision.runtime

internal data class VisionModelDetection(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    init {
        require(confidence in 0f..1f) {
            "confidence must be in [0, 1]"
        }

        require(left in 0f..1f)
        require(top in 0f..1f)
        require(right in 0f..1f)
        require(bottom in 0f..1f)

        require(left < right) {
            "left must be less than right"
        }

        require(top < bottom) {
            "top must be less than bottom"
        }
    }
}
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
