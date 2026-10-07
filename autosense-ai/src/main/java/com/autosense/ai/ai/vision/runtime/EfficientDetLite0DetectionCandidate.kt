package com.autosense.ai.ai.vision.runtime

internal data class EfficientDetLite0DetectionCandidate(
    val anchorIndex: Int,
    val classId: Int,
    val score: Float,
    val box: FloatArray
) {
    init {
        require(anchorIndex >= 0) {
            "anchorIndex must be non-negative"
        }

        require(classId >= 0) {
            "classId must be non-negative"
        }

        require(score in 0.0f..1.0f) {
            "score must be between 0 and 1"
        }

        require(box.size == VALUES_PER_BOX) {
            "box must contain exactly 4 values"
        }

        require(box.all { it.isFinite() }) {
            "box must contain only finite values"
        }

        require(box[0] < box[2]) {
            "box must have positive height"
        }

        require(box[1] < box[3]) {
            "box must have positive width"
        }
    }

    companion object {
        private const val VALUES_PER_BOX = 4
    }
}