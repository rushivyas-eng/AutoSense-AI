package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class EfficientDetLite0ScoreProcessorTest {

    @Test
    fun process_usesModelScoreDirectly() {
        val scores = createScores()

        setScore(scores, anchorIndex = 0, classId = 5, score = 0.8f)

        val result = processor().process(scores)

        assertEquals(5, result[0]?.classId)
        assertEquals(0.8f, result[0]?.score ?: Float.NaN, EPSILON)
    }

    @Test
    fun process_selectsHighestScoringClass() {
        val scores = createScores()

        setScore(scores, 0, 5, 0.6f)
        setScore(scores, 0, 12, 0.8f)
        setScore(scores, 0, 20, 0.7f)

        val result = processor().process(scores)

        assertEquals(12, result[0]?.classId)
        assertEquals(0.8f, result[0]?.score ?: Float.NaN, EPSILON)
    }

    @Test
    fun process_acceptsScoreExactlyAtThreshold() {
        val scores = createScores()
        setScore(scores, 0, 7, 0.6f)

        val result = processor(threshold = 0.6f).process(scores)

        assertEquals(7, result[0]?.classId)
        assertEquals(0.6f, result[0]?.score ?: Float.NaN, EPSILON)
    }

    @Test
    fun process_rejectsScoreBelowThreshold() {
        val scores = createScores()
        setScore(scores, 0, 7, 0.59f)

        val result = processor(threshold = 0.6f).process(scores)

        assertNull(result[0])
    }

    @Test
    fun process_handlesMultipleAnchorsIndependently() {
        val scores = createScores()

        setScore(scores, 0, 2, 0.7f)
        setScore(scores, 1, 8, 0.9f)
        setScore(scores, 2, 15, 0.4f)

        val result = processor(threshold = 0.5f).process(scores)

        assertEquals(2, result[0]?.classId)
        assertEquals(0.7f, result[0]?.score ?: Float.NaN, EPSILON)

        assertEquals(8, result[1]?.classId)
        assertEquals(0.9f, result[1]?.score ?: Float.NaN, EPSILON)

        assertNull(result[2])
    }

    @Test
    fun process_usesFirstClassWhenScoresAreEqual() {
        val scores = createScores()

        setScore(scores, 0, 4, 0.75f)
        setScore(scores, 0, 9, 0.75f)

        val result = processor(threshold = 0.5f).process(scores)

        assertEquals(4, result[0]?.classId)
        assertEquals(0.75f, result[0]?.score ?: Float.NaN, EPSILON)
    }

    @Test
    fun process_excludesBackgroundClass() {
        val scores = createScores()

        setScore(scores, 0, 0, 0.9f)
        setScore(scores, 0, 5, 0.8f)

        val result = processor().process(scores)

        // The highest-scoring class is background, so this anchor
        // must not produce a detection.
        assertNull(result[0])
    }

    @Test
    fun process_excludesAnchorWhenAllScoresAreZero() {
        val scores = FloatArray(expectedScoreCount())

        val result = processor(threshold = 0.5f).process(scores)

        assertNull(result[0])
    }

    @Test
    fun process_preservesHighestForegroundClass() {
        val scores = createScores()

        setScore(scores, 0, 0, 0.3f)
        setScore(scores, 0, 89, 0.95f)

        val result = processor().process(scores)

        assertEquals(89, result[0]?.classId)
        assertEquals(0.95f, result[0]?.score ?: Float.NaN, EPSILON)
    }

    @Test
    fun process_rejectsScoreAboveOne() {
        val scores = createScores()
        setScore(scores, 0, 2, 1.1f)

        assertThrows(IllegalArgumentException::class.java) {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsNegativeScore() {
        val scores = createScores()
        setScore(scores, 0, 2, -0.1f)

        assertThrows(IllegalArgumentException::class.java) {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsNaNScore() {
        val scores = createScores()
        setScore(scores, 0, 2, Float.NaN)

        assertThrows(IllegalArgumentException::class.java) {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsInfiniteScore() {
        val scores = createScores()
        setScore(scores, 0, 2, Float.POSITIVE_INFINITY)

        assertThrows(IllegalArgumentException::class.java) {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsInvalidScoreArraySize() {
        assertThrows(IllegalArgumentException::class.java) {
            processor().process(FloatArray(10))
        }
    }

    @Test
    fun constructor_rejectsNegativeThreshold() {
        assertThrows(IllegalArgumentException::class.java) {
            processor(threshold = -0.1f)
        }
    }

    @Test
    fun constructor_rejectsThresholdAboveOne() {
        assertThrows(IllegalArgumentException::class.java) {
            processor(threshold = 1.1f)
        }
    }

    private fun processor(
        threshold: Float = 0.0f
    ) = EfficientDetLite0ScoreProcessor(
        scoreThreshold = threshold
    )

    private fun createScores(): FloatArray =
        FloatArray(expectedScoreCount()) {
            UNSPECIFIED_SCORE
        }

    private fun expectedScoreCount(): Int =
        EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

    private fun setScore(
        scores: FloatArray,
        anchorIndex: Int,
        classId: Int,
        score: Float
    ) {
        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        scores[anchorIndex * numClasses + classId] = score
    }

    private companion object {
        const val EPSILON = 0.0001f
        const val UNSPECIFIED_SCORE = 0.0f
    }
}