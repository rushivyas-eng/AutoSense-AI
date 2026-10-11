package com.autosense.ai.ai.vision.runtime

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EfficientDetLite0RawOutputDiagnosticsTest {

    @Test
    fun reportRawClassificationAndBoxStatistics() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val inputSize =
            EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS

        // The runner expects UINT8 RGB bytes.
        // This test deliberately uses a uniform gray image.
        val input = ByteArray(inputSize) {
            GRAY_VALUE.toByte()
        }

        AndroidLiteRtModelRunner(context).use { runner ->
            val output = runner.run(input)

            val expectedClassificationSize =
                EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                        EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

            val expectedBoxSize =
                EfficientDetLite0ModelContract.BOX_OUTPUT_ANCHORS *
                        EfficientDetLite0ModelContract.BOX_OUTPUT_VALUES

            assertEquals(
                expectedClassificationSize,
                output.classificationScores.size
            )

            assertEquals(
                expectedBoxSize,
                output.boxRegression.size
            )

            assertTrue(
                "Classification outputs must be finite",
                output.classificationScores.all { it.isFinite() }
            )

            assertTrue(
                "Box outputs must be finite",
                output.boxRegression.all { it.isFinite() }
            )

            reportStatistics(
                name = "classification",
                values = output.classificationScores
            )

            reportStatistics(
                name = "box_regression",
                values = output.boxRegression
            )

            reportPerAnchorStatistics(output.classificationScores)
            reportThresholdCounts(output.classificationScores)

            Log.i(
                TAG,
                "Raw inference completed: " +
                        "latency=${output.inferenceTimeMs}ms, " +
                        "inputBytes=${input.size}"
            )
        }
    }

    private fun reportStatistics(
        name: String,
        values: FloatArray
    ) {
        assertTrue("$name output must not be empty", values.isNotEmpty())

        var min = Float.POSITIVE_INFINITY
        var max = Float.NEGATIVE_INFINITY
        var sum = 0.0
        var positiveCount = 0
        var negativeCount = 0
        var zeroCount = 0

        values.forEach { value ->
            if (value < min) min = value
            if (value > max) max = value

            sum += value.toDouble()

            when {
                value > 0f -> positiveCount++
                value < 0f -> negativeCount++
                else -> zeroCount++
            }
        }

        Log.i(
            TAG,
            "$name: count=${values.size}, " +
                    "min=$min, max=$max, " +
                    "mean=${sum / values.size}, " +
                    "positive=$positiveCount, " +
                    "negative=$negativeCount, " +
                    "zero=$zeroCount"
        )
    }

    private fun reportPerAnchorStatistics(
        logits: FloatArray
    ) {
        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        val numAnchors =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS

        var minimumBestLogit = Float.POSITIVE_INFINITY
        var maximumBestLogit = Float.NEGATIVE_INFINITY
        var sumBestLogits = 0.0

        val classCounts = IntArray(numClasses)
        val bestLogits = FloatArray(numAnchors)

        for (anchorIndex in 0 until numAnchors) {
            val offset = anchorIndex * numClasses

            var bestClass = 0
            var bestLogit = Float.NEGATIVE_INFINITY

            for (classId in 0 until numClasses) {
                val value = logits[offset + classId]

                if (value > bestLogit) {
                    bestLogit = value
                    bestClass = classId
                }
            }

            bestLogits[anchorIndex] = bestLogit
            classCounts[bestClass]++

            if (bestLogit < minimumBestLogit) {
                minimumBestLogit = bestLogit
            }

            if (bestLogit > maximumBestLogit) {
                maximumBestLogit = bestLogit
            }

            sumBestLogits += bestLogit.toDouble()
        }

        val sortedBestLogits = bestLogits.sortedArray()

        fun percentile(fraction: Double): Float {
            val index = (
                    fraction * (sortedBestLogits.size - 1)
                    ).toInt()

            return sortedBestLogits[index]
        }

        val dominantClasses = classCounts.indices
            .filter { classCounts[it] > 0 }
            .sortedByDescending { classCounts[it] }
            .take(10)
            .joinToString { classId ->
                "$classId:${classCounts[classId]}"
            }

        Log.i(
            TAG,
            "Per-anchor best logits: " +
                    "min=$minimumBestLogit, " +
                    "p50=${percentile(0.50)}, " +
                    "p90=${percentile(0.90)}, " +
                    "p99=${percentile(0.99)}, " +
                    "max=$maximumBestLogit, " +
                    "mean=${sumBestLogits / numAnchors}"
        )

        Log.i(
            TAG,
            "Top predicted class IDs per anchor: $dominantClasses"
        )
    }

    private fun reportThresholdCounts(
        logits: FloatArray
    ) {
        val numClasses =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES

        val numAnchors =
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS

        val thresholds = floatArrayOf(
            0.01f,
            0.05f,
            0.10f,
            0.25f,
            0.50f,
            0.75f,
            0.90f
        )

        val counts = IntArray(thresholds.size)

        for (anchorIndex in 0 until numAnchors) {
            val offset = anchorIndex * numClasses

            var bestClass = 0
            var bestLogit = Float.NEGATIVE_INFINITY

            for (classId in 0 until numClasses) {
                val value = logits[offset + classId]

                if (value > bestLogit) {
                    bestLogit = value
                    bestClass = classId
                }
            }

            // Match the current score processor's background convention.
            if (bestClass == 0) continue

            val confidence = sigmoid(bestLogit)

            thresholds.forEachIndexed { index, threshold ->
                if (confidence >= threshold) {
                    counts[index]++
                }
            }
        }

        val report = thresholds.indices.joinToString {
            ">=${thresholds[it]}:${counts[it]}"
        }

        Log.i(TAG, "Foreground anchor counts by threshold: $report")
    }

    private fun sigmoid(value: Float): Float {
        return if (value >= 0f) {
            (1.0 / (1.0 + kotlin.math.exp(-value.toDouble()))).toFloat()
        } else {
            val exponential = kotlin.math.exp(value.toDouble())
            (exponential / (1.0 + exponential)).toFloat()
        }
    }

    private companion object {
        const val TAG = "AutoSenseRawDiagnostics"
        const val GRAY_VALUE = 120
    }
}