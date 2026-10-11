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
            "scoreThreshold must be between 0 and 1"
        }
    }

    fun process(
        classificationScores: FloatArray
    ): Array<EfficientDetLite0ClassScore?> {
        val expectedSize =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        require(classificationScores.size == expectedSize) {
            "Classification score size ${classificationScores.size} " +
                    "does not match expected size $expectedSize"
        }

        val result =
            arrayOfNulls<EfficientDetLite0ClassScore>(
                EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS
            )

        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

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
                    "Classification score must be between 0 and 1; " +
                            "got $score"
                }

                if (score > bestScore) {
                    bestScore = score
                    bestClassId = classId
                }
            }

            if (bestClassId == BACKGROUND_CLASS_ID) {
                continue
            }

            if (bestScore >= scoreThreshold) {
                result[anchorIndex] =
                    EfficientDetLite0ClassScore(
                        classId = bestClassId,
                        score = bestScore
                    )
            }
        }

        return result
    }

    companion object {
        private const val BACKGROUND_CLASS_ID = 0

        /*
         * The model's classification output already passes through
         * a LOGISTIC operator. Do not apply sigmoid a second time.
         *
         * Keep the model post-processing threshold configurable.
         * Application-level detection policy can apply a stricter
         * threshold later.
         */
        const val DEFAULT_SCORE_THRESHOLD = 0.0f
    }
}