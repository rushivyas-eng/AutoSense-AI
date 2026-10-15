package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class EfficientDetLite0ScoreProcessorTest {

    private val contract = EfficientDetLite0ModelContract

    @Test
    fun process_usesModelScoreDirectly() {
        val scores = scoresForSingleAnchor(
            classId = 5,
            score = 0.8f
        )

        val result = processor().process(scores)

        assertEquals(5, result[0]?.classId)
        assertEquals(0.8f, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_selectsHighestScoringClass() {
        val scores = scoresForSingleAnchor(
            classId = 5,
            score = 0.8f
        )

        scores[0] = 0.2f
        scores[5] = 0.8f
        scores[10] = 0.4f

        val result = processor().process(scores)

        assertEquals(5, result[0]?.classId)
        assertEquals(0.8f, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_includesClassZeroAsValidForegroundClass() {
        val scores = scoresForSingleAnchor(
            classId = 0,
            score = 0.95f
        )

        val result = processor().process(scores)

        assertNotNull(result[0])
        assertEquals(0, result[0]?.classId)
        assertEquals(0.95f, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_acceptsScoreEqualToThreshold() {
        val threshold = 0.5f
        val scores = scoresForSingleAnchor(
            classId = 5,
            score = threshold
        )

        val result = processor(threshold).process(scores)

        assertNotNull(result[0])
        assertEquals(threshold, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_excludesScoreBelowThreshold() {
        val scores = scoresForSingleAnchor(
            classId = 5,
            score = 0.49f
        )

        val result = processor(0.5f).process(scores)

        assertNull(result[0])
    }

    @Test
    fun process_handlesAnchorsIndependently() {
        val scores = FloatArray(expectedClassificationSize())

        scores[5] = 0.9f

        val secondAnchorOffset =
            contract.CLASSIFICATION_OUTPUT_CLASSES

        scores[secondAnchorOffset + 2] = 0.8f

        val result = processor(0.5f).process(scores)

        assertEquals(5, result[0]?.classId)
        assertEquals(0.9f, result[0]?.score ?: 0f, 0.0001f)

        assertEquals(2, result[1]?.classId)
        assertEquals(0.8f, result[1]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_tiesSelectFirstClass() {
        val scores = FloatArray(expectedClassificationSize())

        scores[2] = 0.8f
        scores[3] = 0.8f

        val result = processor(0.5f).process(scores)

        assertEquals(2, result[0]?.classId)
        assertEquals(0.8f, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_excludesAnchorWhenAllScoresAreZeroAndThresholdIsPositive() {
        val scores = FloatArray(expectedClassificationSize())

        val result = processor(0.5f).process(scores)

        assertNull(result[0])
    }

    @Test
    fun process_preservesHighestClassWhenClassZeroHasLowerScore() {
        val scores = FloatArray(expectedClassificationSize())

        scores[0] = 0.3f
        scores[89] = 0.95f

        val result = processor(0.5f).process(scores)

        assertEquals(89, result[0]?.classId)
        assertEquals(0.95f, result[0]?.score ?: 0f, 0.0001f)
    }

    @Test
    fun process_rejectsScoresAboveOne() {
        val scores = FloatArray(expectedClassificationSize())
        scores[0] = 1.1f

        assertIllegalArgument {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsNegativeScores() {
        val scores = FloatArray(expectedClassificationSize())
        scores[0] = -0.1f

        assertIllegalArgument {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsNaNScores() {
        val scores = FloatArray(expectedClassificationSize())
        scores[0] = Float.NaN

        assertIllegalArgument {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsInfiniteScores() {
        val scores = FloatArray(expectedClassificationSize())
        scores[0] = Float.POSITIVE_INFINITY

        assertIllegalArgument {
            processor().process(scores)
        }
    }

    @Test
    fun process_rejectsIncorrectArraySize() {
        assertIllegalArgument {
            processor().process(FloatArray(1))
        }
    }

    @Test
    fun constructor_rejectsNegativeThreshold() {
        assertIllegalArgument {
            processor(-0.1f)
        }
    }

    @Test
    fun constructor_rejectsThresholdAboveOne() {
        assertIllegalArgument {
            processor(1.1f)
        }
    }

    private fun processor(
        threshold: Float =
            EfficientDetLite0ScoreProcessor.DEFAULT_SCORE_THRESHOLD
    ) = EfficientDetLite0ScoreProcessor(threshold)

    private fun expectedClassificationSize(): Int =
        contract.CLASSIFICATION_OUTPUT_ANCHORS *
                contract.CLASSIFICATION_OUTPUT_CLASSES

    private fun scoresForSingleAnchor(
        classId: Int,
        score: Float
    ): FloatArray {
        val scores = FloatArray(expectedClassificationSize())

        scores[classId] = score

        return scores
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}