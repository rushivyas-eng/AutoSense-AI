package com.autosense.ai.ai.vision.runtime

internal data class EfficientDetLite0ClassScore(
    val classId: Int,
    val score: Float
)

internal class EfficientDetLite0ScoreProcessor(
    private val scoreThreshold: Float = DEFAULT_SCORE_THRESHOLD
) {

    init {
        require(scoreThreshold in 0.0f..1.0f) {
            "Score threshold must be between 0 and 1"
        }
    }

    /**
     * Selects the highest-scoring class for each anchor.
     *
     * The model's classification output already passes through a LOGISTIC
     * operator. Therefore, scores are probabilities and must not be passed
     * through sigmoid again.
     *
     * Class IDs correspond directly to the model's label map.
     * In particular, class ID 0 is a valid class, not a background class.
     *
     * Anchors whose highest score is below the configured threshold are
     * excluded by returning null for those anchors.
     */
    fun process(
        classificationScores: FloatArray
    ): Array<EfficientDetLite0ClassScore?> {
        val expectedSize =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        require(classificationScores.size == expectedSize) {
            "Expected $expectedSize classification scores, " +
                    "but received ${classificationScores.size}"
        }

        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        val result =
            arrayOfNulls<EfficientDetLite0ClassScore>(
                EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS
            )

        for (anchorIndex in result.indices) {
            val offset = anchorIndex * numClasses

            var bestClassId = -1
            var bestScore = Float.NEGATIVE_INFINITY

            for (classId in 0 until numClasses) {
                val score = classificationScores[offset + classId]

                require(score.isFinite()) {
                    "Classification score must be finite"
                }

                require(score in 0.0f..1.0f) {
                    "Classification score must be between 0 and 1; got $score"
                }

                if (score > bestScore) {
                    bestScore = score
                    bestClassId = classId
                }
            }

            if (bestScore >= scoreThreshold) {
                result[anchorIndex] = EfficientDetLite0ClassScore(
                    classId = bestClassId,
                    score = bestScore
                )
            }
        }

        return result
    }

    companion object {
        /**
         * Leave the initial threshold permissive.
         *
         * EfficientDetLite0Nms applies its own score threshold before
         * producing final detections.
         */
        const val DEFAULT_SCORE_THRESHOLD = 0.0f
    }
}