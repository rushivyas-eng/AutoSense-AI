package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.exp

class EfficientDetLite0ScoreProcessorTest {

    @Test
    fun process_appliesSigmoidToWinningLogit() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 5,
            score = 0.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.0f
            )

        val result = processor.process(scores)

        assertEquals(
            5,
            result[0]?.classId
        )

        assertEquals(
            0.5f,
            result[0]?.score ?: Float.NaN,
            EPSILON
        )
    }

    @Test
    fun process_selectsHighestLogitBeforeApplyingSigmoid() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 5,
            score = 2.0f
        )

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 12,
            score = 3.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.0f
            )

        val result = processor.process(scores)

        assertEquals(
            12,
            result[0]?.classId
        )

        assertEquals(
            sigmoid(3.0),
            result[0]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )
    }

    @Test
    fun process_negativeLogitProducesConfidenceBelowHalf() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 3,
            score = -2.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.0f
            )

        val result = processor.process(scores)

        assertEquals(
            3,
            result[0]?.classId
        )

        assertEquals(
            sigmoid(-2.0),
            result[0]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )
    }

    @Test
    fun process_positiveLogitProducesConfidenceAboveHalf() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 3,
            score = 2.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.0f
            )

        val result = processor.process(scores)

        assertEquals(
            3,
            result[0]?.classId
        )

        assertEquals(
            sigmoid(2.0),
            result[0]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )
    }

    @Test
    fun process_appliesThresholdAfterSigmoid() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 7,
            score = 0.0f
        )

        /*
         * sigmoid(0) = 0.5
         *
         * Threshold is 0.6, so the candidate must be rejected.
         */
        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.6f
            )

        val result = processor.process(scores)

        assertNull(result[0])
    }

    @Test
    fun process_acceptsConfidenceExactlyAtThreshold() {
        val scores = createScores()

        val logit =
            kotlin.math.ln(0.6 / 0.4).toFloat()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 7,
            score = logit
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.6f
            )

        val result = processor.process(scores)

        assertEquals(
            7,
            result[0]?.classId
        )

        assertEquals(
            0.6f,
            result[0]?.score ?: Float.NaN,
            EPSILON
        )
    }

    @Test
    fun process_handlesMultipleAnchorsIndependently() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 2,
            score = 1.0f
        )

        setScore(
            scores = scores,
            anchorIndex = 1,
            classId = 8,
            score = 2.0f
        )

        setScore(
            scores = scores,
            anchorIndex = 2,
            classId = 15,
            score = -1.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.0f
            )

        val result = processor.process(scores)

        assertEquals(
            2,
            result[0]?.classId
        )

        assertEquals(
            sigmoid(1.0),
            result[0]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )

        assertEquals(
            8,
            result[1]?.classId
        )

        assertEquals(
            sigmoid(2.0),
            result[1]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )

        assertEquals(
            15,
            result[2]?.classId
        )

        assertEquals(
            sigmoid(-1.0),
            result[2]?.score?.toDouble() ?: Double.NaN,
            EPSILON_DOUBLE
        )
    }

    @Test
    fun process_usesFirstClassWhenLogitsAreEqual() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 4,
            score = 0.75f
        )

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 9,
            score = 0.75f
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.5f
            )

        val result = processor.process(scores)

        assertEquals(
            4,
            result[0]?.classId
        )
    }

    @Test
    fun process_allZeroLogitsProduceHalfConfidence() {
        /*
         * This test intentionally uses every class = 0.
         * Therefore class 0 wins the tie.
         */
        val scores = FloatArray(
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES
        )

        val processor =
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 0.5f
            )

        val result = processor.process(scores)

        assertEquals(
            0,
            result[0]?.classId
        )

        assertEquals(
            0.5f,
            result[0]?.score ?: Float.NaN,
            EPSILON
        )
    }

    @Test
    fun process_largePositiveLogitApproachesOne() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 2,
            score = 100.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor()

        val result = processor.process(scores)

        assertEquals(
            1.0f,
            result[0]?.score ?: Float.NaN,
            EPSILON
        )
    }

    @Test
    fun process_largeNegativeLogitApproachesZero() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 2,
            score = -100.0f
        )

        val processor =
            EfficientDetLite0ScoreProcessor()

        val result = processor.process(scores)

        assertEquals(
            2,
            result[0]?.classId
        )

        assertEquals(
            0.0f,
            result[0]?.score ?: Float.NaN,
            EPSILON
        )
    }

    @Test
    fun process_rejectsInvalidScoreArraySize() {
        try {
            EfficientDetLite0ScoreProcessor()
                .process(FloatArray(10))
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun process_rejectsNaNLogit() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 2,
            score = Float.NaN
        )

        try {
            EfficientDetLite0ScoreProcessor()
                .process(scores)
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun process_rejectsInfiniteLogit() {
        val scores = createScores()

        setScore(
            scores = scores,
            anchorIndex = 0,
            classId = 2,
            score = Float.POSITIVE_INFINITY
        )

        try {
            EfficientDetLite0ScoreProcessor()
                .process(scores)
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun constructor_rejectsNegativeThreshold() {
        try {
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = -0.1f
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun constructor_rejectsThresholdAboveOne() {
        try {
            EfficientDetLite0ScoreProcessor(
                scoreThreshold = 1.1f
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    private fun createScores(): FloatArray {
        /*
         * Give every unspecified class a very low but finite logit.
         *
         * This ensures that a test-specific logit is actually the
         * winning class without introducing infinities into the
         * model output.
         */
        return FloatArray(
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES
        ) {
            UNSPECIFIED_LOGIT
        }
    }

    private fun setScore(
        scores: FloatArray,
        anchorIndex: Int,
        classId: Int,
        score: Float
    ) {
        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        scores[
            anchorIndex * numClasses + classId
        ] = score
    }

    private fun sigmoid(value: Double): Double {
        return if (value >= 0.0) {
            1.0 / (1.0 + exp(-value))
        } else {
            val exponential = exp(value)
            exponential / (1.0 + exponential)
        }
    }

    companion object {
        private const val EPSILON = 0.0001f
        private const val EPSILON_DOUBLE = 0.0001
        private const val UNSPECIFIED_LOGIT = -200.0f
    }
}