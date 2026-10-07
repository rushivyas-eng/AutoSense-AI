package com.autosense.ai.ai.vision.runtime

import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

internal class EfficientDetLite0Nms(
    private val iouThreshold: Float = DEFAULT_IOU_THRESHOLD,
    private val sigma: Float = DEFAULT_SIGMA,
    private val scoreThreshold: Float = DEFAULT_SCORE_THRESHOLD,
    private val maxDetections: Int = DEFAULT_MAX_DETECTIONS
) {

    init {
        require(iouThreshold in 0.0f..1.0f) {
            "iouThreshold must be between 0 and 1"
        }

        require(sigma > 0.0f) {
            "sigma must be greater than zero"
        }

        require(scoreThreshold in 0.0f..1.0f) {
            "scoreThreshold must be between 0 and 1"
        }

        require(maxDetections > 0) {
            "maxDetections must be greater than zero"
        }
    }

    fun process(
        candidates: List<EfficientDetLite0DetectionCandidate>
    ): List<EfficientDetLite0DetectionCandidate> {
        if (candidates.isEmpty()) {
            return emptyList()
        }

        /*
         * EfficientDet's more accurate NMS path performs NMS
         * independently for each class.
         */
        val candidatesByClass =
            candidates.groupBy { it.classId }

        val results = mutableListOf<EfficientDetLite0DetectionCandidate>()

        for (classId in candidatesByClass.keys.sorted()) {
            results += processClass(
                candidatesByClass.getValue(classId)
            )
        }

        /*
         * Per-class NMS can produce more than the global maximum.
         *
         * EfficientDet combines the class-specific results and
         * selects the highest scoring detections globally.
         */
        return results
            .sortedWith(
                compareByDescending<EfficientDetLite0DetectionCandidate> {
                    it.score
                }.thenBy {
                    it.classId
                }.thenBy {
                    it.anchorIndex
                }
            )
            .take(maxDetections)
    }

    private fun processClass(
        classCandidates: List<EfficientDetLite0DetectionCandidate>
    ): List<EfficientDetLite0DetectionCandidate> {

        /*
         * Work on mutable score entries so Gaussian decay does not
         * modify the original DetectionCandidate.
         */
        val remaining =
            classCandidates
                .map {
                    ScoredCandidate(
                        candidate = it,
                        score = it.score
                    )
                }
                .toMutableList()

        val selected =
            mutableListOf<EfficientDetLite0DetectionCandidate>()

        while (remaining.isNotEmpty()) {
            /*
             * Select the current highest scoring candidate.
             *
             * Tie-breaking is deterministic:
             *   1. score descending
             *   2. anchor index ascending
             */
            remaining.sortWith(
                compareByDescending<ScoredCandidate> {
                    it.score
                }.thenBy {
                    it.candidate.anchorIndex
                }
            )

            val best =
                remaining.removeAt(0)

            if (best.score < scoreThreshold) {
                break
            }

            selected +=
                best.candidate.copy(
                    score = best.score
                )

            val bestBox =
                best.candidate.box

            val iterator =
                remaining.iterator()

            while (iterator.hasNext()) {
                val current =
                    iterator.next()

                val overlap =
                    intersectionOverUnion(
                        bestBox,
                        current.candidate.box
                    )

                if (overlap > iouThreshold) {
                    current.score =
                        gaussianDecay(
                            current.score,
                            overlap
                        )
                }

                if (current.score < scoreThreshold) {
                    iterator.remove()
                }
            }
        }

        return selected
    }

    private fun gaussianDecay(
        score: Float,
        iou: Float
    ): Float {
        /*
         * EfficientDet configuration:
         *
         *   sigma = 0.5
         *
         * TensorFlow's NonMaxSuppressionV5 uses a sigma convention
         * that is twice the EfficientDet configuration value.
         *
         * This implementation directly uses the EfficientDet-level
         * convention.
         */
        val decay =
            exp(
                -(
                        iou * iou
                        ).toDouble() / sigma.toDouble()
            ).toFloat()

        return score * decay
    }

    private fun intersectionOverUnion(
        first: FloatArray,
        second: FloatArray
    ): Float {
        val intersectionTop =
            max(first[0], second[0])

        val intersectionLeft =
            max(first[1], second[1])

        val intersectionBottom =
            min(first[2], second[2])

        val intersectionRight =
            min(first[3], second[3])

        val intersectionHeight =
            max(
                0.0f,
                intersectionBottom - intersectionTop
            )

        val intersectionWidth =
            max(
                0.0f,
                intersectionRight - intersectionLeft
            )

        val intersectionArea =
            intersectionHeight * intersectionWidth

        if (intersectionArea <= 0.0f) {
            return 0.0f
        }

        val firstArea =
            (first[2] - first[0]) *
                    (first[3] - first[1])

        val secondArea =
            (second[2] - second[0]) *
                    (second[3] - second[1])

        val unionArea =
            firstArea +
                    secondArea -
                    intersectionArea

        if (unionArea <= 0.0f) {
            return 0.0f
        }

        return intersectionArea / unionArea
    }

    private class ScoredCandidate(
        val candidate: EfficientDetLite0DetectionCandidate,
        var score: Float
    )

    companion object {
        const val DEFAULT_IOU_THRESHOLD = 0.5f
        const val DEFAULT_SIGMA = 0.5f
        const val DEFAULT_SCORE_THRESHOLD = 0.001f
        const val DEFAULT_MAX_DETECTIONS = 100
    }
}