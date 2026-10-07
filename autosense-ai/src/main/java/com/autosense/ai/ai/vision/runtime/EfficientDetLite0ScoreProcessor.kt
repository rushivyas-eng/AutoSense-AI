package com.autosense.ai.ai.vision.runtime

import kotlin.math.exp

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
        classificationLogits: FloatArray
    ): Array<EfficientDetLite0ClassScore?> {
        val expectedSize =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        require(classificationLogits.size == expectedSize) {
            "Classification logit size ${classificationLogits.size} " +
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
            var bestLogit = Float.NEGATIVE_INFINITY

            for (classId in 0 until numClasses) {
                val logit =
                    classificationLogits[offset + classId]

                require(logit.isFinite()) {
                    "Classification logit must be finite"
                }

                if (logit > bestLogit) {
                    bestLogit = logit
                    bestClassId = classId
                }
            }

            val confidence =
                sigmoid(bestLogit)

            if (confidence >= scoreThreshold) {
                result[anchorIndex] =
                    EfficientDetLite0ClassScore(
                        classId = bestClassId,
                        score = confidence
                    )
            }
        }

        return result
    }

    private fun sigmoid(value: Float): Float {
        /*
         * Numerically stable sigmoid:
         *
         * sigmoid(x) = 1 / (1 + exp(-x))
         *
         * For positive values we evaluate:
         *
         *   1 / (1 + exp(-x))
         *
         * For negative values we evaluate:
         *
         *   exp(x) / (1 + exp(x))
         *
         * This avoids unnecessary overflow in exp().
         */
        return if (value >= 0.0f) {
            (
                    1.0 /
                            (1.0 + exp(-value.toDouble()))
                    ).toFloat()
        } else {
            val exponential =
                exp(value.toDouble())

            (
                    exponential /
                            (1.0 + exponential)
                    ).toFloat()
        }
    }

    companion object {
        /*
         * Keep the model post-processing threshold configurable.
         *
         * The reference EfficientDet configuration uses 0.0
         * for its NMS score threshold. Application-level detection
         * policy can apply a stricter threshold later.
         */
        const val DEFAULT_SCORE_THRESHOLD = 0.0f
    }
}