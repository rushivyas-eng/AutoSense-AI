package com.autosense.ai.ai.vision.runtime

import android.content.Context
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import com.google.ai.edge.litert.TensorType

/**
 * Android-specific LiteRT implementation for EfficientDet-Lite0.
 *
 * Tensor buffers are allocated once and reused for each inference.
 *
 * Input and output buffers are addressed by tensor name so the runtime
 * does not depend on the ordering of buffers returned by the model.
 */
internal class AndroidLiteRtModelRunner(
    context: Context
) : LiteRtModelRunner {

    private val model: CompiledModel

    private val inputBuffers: Map<String, TensorBuffer>
    private val outputBuffers: Map<String, TensorBuffer>

    init {
        model = CompiledModel.create(
            context.assets,
            EfficientDetLite0ModelContract.MODEL_ASSET_NAME,
            CompiledModel.Options.CPU
        )

        try {
            validateTensorContract()

            inputBuffers = mapOf(
                EfficientDetLite0ModelContract.INPUT_TENSOR_NAME to
                        model.createInputBuffer(
                            EfficientDetLite0ModelContract.INPUT_TENSOR_NAME
                        )
            )

            outputBuffers = mapOf(
                EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_TENSOR_NAME to
                        model.createOutputBuffer(
                            EfficientDetLite0ModelContract
                                .CLASSIFICATION_OUTPUT_TENSOR_NAME
                        ),

                EfficientDetLite0ModelContract.BOX_OUTPUT_TENSOR_NAME to
                        model.createOutputBuffer(
                            EfficientDetLite0ModelContract.BOX_OUTPUT_TENSOR_NAME
                        )
            )
        } catch (exception: Exception) {
            model.close()
            throw exception
        }
    }

    override fun run(input: ByteArray): EfficientDetLite0RawOutput {
        val expectedInputSize =
            EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS

        require(input.size == expectedInputSize) {
            "Unexpected LiteRT input size: ${input.size}; " +
                    "expected $expectedInputSize"
        }

        val inputBuffer = requireNotNull(
            inputBuffers[
                EfficientDetLite0ModelContract.INPUT_TENSOR_NAME
            ]
        ) {
            "EfficientDet input buffer is unavailable"
        }

        inputBuffer.writeInt8(input)

        val startNanos = System.nanoTime()

        model.run(
            inputBuffers,
            outputBuffers
        )

        val inferenceTimeMs =
            (System.nanoTime() - startNanos) / 1_000_000L

        val classificationScores = requireNotNull(
            outputBuffers[
                EfficientDetLite0ModelContract
                    .CLASSIFICATION_OUTPUT_TENSOR_NAME
            ]
        ) {
            "Classification output buffer is unavailable"
        }.readFloat()

        val boxRegression = requireNotNull(
            outputBuffers[
                EfficientDetLite0ModelContract.BOX_OUTPUT_TENSOR_NAME
            ]
        ) {
            "Box-regression output buffer is unavailable"
        }.readFloat()

        return EfficientDetLite0RawOutput(
            classificationScores = classificationScores,
            boxRegression = boxRegression,
            inferenceTimeMs = inferenceTimeMs
        )
    }

    /**
     * Verifies the tensor names, shapes, and output element types against
     * the model contract before allocating reusable inference buffers.
     *
     * LiteRT exposes tensor dimensions through TensorType.layout.
     */
    private fun validateTensorContract() {
        val contract = EfficientDetLite0ModelContract

        val inputType = model.getInputTensorType(
            contract.INPUT_TENSOR_NAME
        )

        val expectedInputShape = listOf(
            1,
            contract.INPUT_HEIGHT,
            contract.INPUT_WIDTH,
            contract.INPUT_CHANNELS
        )

        require(inputType.layout?.dimensions == expectedInputShape) {
            "Unexpected input tensor shape for " +
                    "${contract.INPUT_TENSOR_NAME}: " +
                    "${inputType.layout?.dimensions}; " +
                    "expected $expectedInputShape"
        }

        val classificationType = model.getOutputTensorType(
            contract.CLASSIFICATION_OUTPUT_TENSOR_NAME
        )

        val expectedClassificationShape = listOf(
            1,
            contract.CLASSIFICATION_OUTPUT_ANCHORS,
            contract.CLASSIFICATION_OUTPUT_CLASSES
        )

        require(
            classificationType.elementType ==
                    TensorType.ElementType.FLOAT
        ) {
            "Classification output must be FLOAT32, got " +
                    classificationType.elementType
        }

        require(
            classificationType.layout?.dimensions ==
                    expectedClassificationShape
        ) {
            "Unexpected classification tensor shape: " +
                    "${classificationType.layout?.dimensions}; " +
                    "expected $expectedClassificationShape"
        }

        val boxType = model.getOutputTensorType(
            contract.BOX_OUTPUT_TENSOR_NAME
        )

        val expectedBoxShape = listOf(
            1,
            contract.BOX_OUTPUT_ANCHORS,
            contract.BOX_OUTPUT_VALUES
        )

        require(boxType.elementType == TensorType.ElementType.FLOAT) {
            "Box-regression output must be FLOAT32, got " +
                    boxType.elementType
        }

        require(boxType.layout?.dimensions == expectedBoxShape) {
            "Unexpected box-regression tensor shape: " +
                    "${boxType.layout?.dimensions}; " +
                    "expected $expectedBoxShape"
        }
    }

    override fun close() {
        outputBuffers.values.forEach { it.close() }
        inputBuffers.values.forEach { it.close() }
        model.close()
    }
}