package com.autosense.ai.ai.vision.runtime

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer

internal class AndroidLiteRtModelRunner(
    context: Context
) : LiteRtModelRunner {

    private val model: CompiledModel

    private val inputBuffers: List<TensorBuffer>
    private val outputBuffers: List<TensorBuffer>

    init {
        model = CompiledModel.create(
            context.assets,
            EfficientDetLite0ModelContract.MODEL_ASSET_NAME,
            CompiledModel.Options.CPU
        )

        inputBuffers = model.createInputBuffers()
        outputBuffers = model.createOutputBuffers()

        require(inputBuffers.size == 1) {
            "Expected exactly one LiteRT input buffer, got ${inputBuffers.size}"
        }

        require(outputBuffers.size == 2) {
            "Expected exactly two LiteRT output buffers, got ${outputBuffers.size}"
        }
    }

    override fun run(input: ByteArray): EfficientDetLite0RawOutput {
        require(
            input.size ==
                    EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS
        ) {
            "Unexpected LiteRT input size: ${input.size}"
        }

        inputBuffers[0].writeInt8(input)

        val startNanos = System.nanoTime()

        model.run(
            inputBuffers,
            outputBuffers
        )

        val inferenceTimeMs =
            (System.nanoTime() - startNanos) / 1_000_000L

        val classificationScores = outputBuffers[0].readFloat()
        val boxRegression = outputBuffers[1].readFloat()

        return EfficientDetLite0RawOutput(
            classificationScores = classificationScores,
            boxRegression = boxRegression,
            inferenceTimeMs = inferenceTimeMs
        )
    }

    override fun close() {
        outputBuffers.forEach { it.close() }
        inputBuffers.forEach { it.close() }
        model.close()
    }
}