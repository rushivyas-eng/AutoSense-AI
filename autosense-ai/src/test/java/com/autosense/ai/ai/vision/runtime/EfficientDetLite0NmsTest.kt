package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EfficientDetLite0NmsTest {

    @Test
    fun process_emptyInput_returnsEmptyList() {
        val nms = EfficientDetLite0Nms()

        val result =
            nms.process(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun process_singleCandidate_keepsCandidate() {
        val candidate =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.9f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(listOf(candidate))

        assertEquals(1, result.size)
        assertEquals(0, result[0].anchorIndex)
        assertEquals(1, result[0].classId)
        assertEquals(0.9f, result[0].score)
    }

    @Test
    fun process_nonOverlappingCandidates_keepsBoth() {
        val first =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.9f,
                box = box(
                    0.0f,
                    0.0f,
                    0.2f,
                    0.2f
                )
            )

        val second =
            candidate(
                anchorIndex = 1,
                classId = 1,
                score = 0.8f,
                box = box(
                    0.8f,
                    0.8f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(2, result.size)
    }

    @Test
    fun process_overlappingSameClass_appliesGaussianDecay() {
        val first =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.9f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val second =
            candidate(
                anchorIndex = 1,
                classId = 1,
                score = 0.8f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(2, result.size)

        assertEquals(
            0.9f,
            result[0].score,
            EPSILON
        )

        /*
         * IoU = 1
         *
         * Gaussian decay:
         *
         * exp(-1 / 0.5)
         * = exp(-2)
         */
        val expectedSecondScore =
            0.8 *
                    kotlin.math.exp(-2.0)

        assertEquals(
            expectedSecondScore,
            result[1].score.toDouble(),
            EPSILON_DOUBLE
        )
    }

    @Test
    fun process_differentClasses_doNotSuppressEachOther() {
        val first =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.9f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val second =
            candidate(
                anchorIndex = 1,
                classId = 2,
                score = 0.8f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(2, result.size)

        assertEquals(
            1,
            result[0].classId
        )

        assertEquals(
            2,
            result[1].classId
        )
    }

    @Test
    fun process_highOverlapBelowScoreThreshold_isRemoved() {
        val first =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.9f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val second =
            candidate(
                anchorIndex = 1,
                classId = 1,
                score = 0.001f,
                box = box(
                    0.0f,
                    0.0f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(1, result.size)
        assertEquals(0, result[0].anchorIndex)
    }

    @Test
    fun process_resultsAreGloballySortedByScore() {
        val first =
            candidate(
                anchorIndex = 0,
                classId = 1,
                score = 0.7f,
                box = box(
                    0.0f,
                    0.0f,
                    0.2f,
                    0.2f
                )
            )

        val second =
            candidate(
                anchorIndex = 1,
                classId = 2,
                score = 0.95f,
                box = box(
                    0.8f,
                    0.8f,
                    1.0f,
                    1.0f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(2, result.size)

        assertEquals(
            0.95f,
            result[0].score
        )

        assertEquals(
            0.7f,
            result[1].score
        )
    }

    @Test
    fun process_limitsGlobalResultsToMaximumDetections() {
        val candidates =
            (0..149).map { index ->
                candidate(
                    anchorIndex = index,
                    classId = index % 5,
                    score = 0.9f - index * 0.001f,
                    box = box(
                        top = index.toFloat(),
                        left = 0.0f,
                        bottom = index.toFloat() + 0.5f,
                        right = 0.5f
                    )
                )
            }

        val result =
            EfficientDetLite0Nms(
                maxDetections = 100
            ).process(candidates)

        assertEquals(
            100,
            result.size
        )
    }

    @Test
    fun process_equalScores_usesDeterministicAnchorOrdering() {
        val first =
            candidate(
                anchorIndex = 10,
                classId = 1,
                score = 0.8f,
                box = box(
                    0.0f,
                    0.0f,
                    0.1f,
                    0.1f
                )
            )

        val second =
            candidate(
                anchorIndex = 5,
                classId = 1,
                score = 0.8f,
                box = box(
                    0.2f,
                    0.2f,
                    0.3f,
                    0.3f
                )
            )

        val result =
            EfficientDetLite0Nms()
                .process(
                    listOf(
                        first,
                        second
                    )
                )

        assertEquals(
            5,
            result[0].anchorIndex
        )

        assertEquals(
            10,
            result[1].anchorIndex
        )
    }

    @Test
    fun constructor_rejectsInvalidIoUThreshold() {
        try {
            EfficientDetLite0Nms(
                iouThreshold = 1.1f
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun constructor_rejectsInvalidSigma() {
        try {
            EfficientDetLite0Nms(
                sigma = 0.0f
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    @Test
    fun constructor_rejectsInvalidScoreThreshold() {
        try {
            EfficientDetLite0Nms(
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
    fun constructor_rejectsInvalidMaximumDetections() {
        try {
            EfficientDetLite0Nms(
                maxDetections = 0
            )
        } catch (exception: IllegalArgumentException) {
            return
        }

        throw AssertionError(
            "Expected IllegalArgumentException"
        )
    }

    private fun candidate(
        anchorIndex: Int,
        classId: Int,
        score: Float,
        box: FloatArray
    ): EfficientDetLite0DetectionCandidate {
        return EfficientDetLite0DetectionCandidate(
            anchorIndex = anchorIndex,
            classId = classId,
            score = score,
            box = box
        )
    }

    private fun box(
        top: Float,
        left: Float,
        bottom: Float,
        right: Float
    ): FloatArray {
        return floatArrayOf(
            top,
            left,
            bottom,
            right
        )
    }

    companion object {
        private const val EPSILON = 0.0001f
        private const val EPSILON_DOUBLE = 0.0001
    }
}