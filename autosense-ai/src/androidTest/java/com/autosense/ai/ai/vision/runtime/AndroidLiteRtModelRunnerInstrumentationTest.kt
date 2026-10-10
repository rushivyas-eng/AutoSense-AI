package com.autosense.ai.ai.vision.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidLiteRtModelRunnerInstrumentationTest {

    @Test
    fun modelLoadsAndRunsInference() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val inputSize =
            EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS

        // A deterministic black RGB image.
        // This tests execution, not detection accuracy.
        val input = ByteArray(inputSize)

        AndroidLiteRtModelRunner(context).use { runner ->
            val output = runner.run(input)

            assertOutputContract(output)

            assertTrue(
                "Inference duration must be non-negative",
                output.inferenceTimeMs >= 0
            )
        }
    }

    @Test
    fun repeatedInferenceProducesValidOutputs() {
        val context =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext

        val inputSize =
            EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS

        val input = ByteArray(inputSize)

        AndroidLiteRtModelRunner(context).use { runner ->
            // Warm-up: initialize any lazy runtime work before measurement.
            val warmupOutput = runner.run(input)
            assertOutputContract(warmupOutput)

            val measuredDurations = mutableListOf<Long>()

            repeat(5) {
                val output = runner.run(input)

                assertOutputContract(output)

                assertTrue(
                    "Inference duration must be non-negative",
                    output.inferenceTimeMs >= 0
                )

                measuredDurations.add(output.inferenceTimeMs)
            }

            val averageLatencyMs =
                measuredDurations.average()

            val minimumLatencyMs =
                measuredDurations.minOrNull() ?: 0L

            val maximumLatencyMs =
                measuredDurations.maxOrNull() ?: 0L

            android.util.Log.i(
                TAG,
                "Repeated inference results: " +
                        "runs=${measuredDurations.size}, " +
                        "average=${"%.2f".format(averageLatencyMs)} ms, " +
                        "minimum=$minimumLatencyMs ms, " +
                        "maximum=$maximumLatencyMs ms"
            )

            assertEquals(
                "Expected five measured inference runs",
                5,
                measuredDurations.size
            )
        }
    }

    private fun assertOutputContract(
        output: EfficientDetLite0RawOutput
    ) {
        val expectedClassificationSize =
            EfficientDetLite0ModelContract
                .CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract
                        .CLASSIFICATION_OUTPUT_CLASSES

        val expectedBoxSize =
            EfficientDetLite0ModelContract
                .BOX_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract
                        .BOX_OUTPUT_VALUES

        assertEquals(
            "Unexpected classification output size",
            expectedClassificationSize,
            output.classificationScores.size
        )

        assertEquals(
            "Unexpected bounding-box output size",
            expectedBoxSize,
            output.boxRegression.size
        )

        assertTrue(
            "Classification output contains non-finite values",
            output.classificationScores.all { it.isFinite() }
        )

        assertTrue(
            "Bounding-box output contains non-finite values",
            output.boxRegression.all { it.isFinite() }
        )
    }

    private companion object {
        const val TAG = "AutoSenseModelTest"
    }
}