package com.autosense.ai.ai.vision.runtime

data class VisionModelDetection(
    val classId: Int,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    init {
        require(classId >= 0) {
            "classId must be non-negative"
        }
        require(confidence in 0f..1f) {
            "confidence must be between 0 and 1"
        }
        require(left in 0f..1f) {
            "left must be between 0 and 1"
        }
        require(top in 0f..1f) {
            "top must be between 0 and 1"
        }
        require(right in 0f..1f) {
            "right must be between 0 and 1"
        }
        require(bottom in 0f..1f) {
            "bottom must be between 0 and 1"
        }
        require(left < right) {
            "left must be less than right"
        }
        require(top < bottom) {
            "top must be less than bottom"
        }
    }
}
