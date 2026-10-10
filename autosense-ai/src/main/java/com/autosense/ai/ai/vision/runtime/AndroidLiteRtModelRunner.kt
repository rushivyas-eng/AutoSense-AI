package com.autosense.ai.ai.vision.runtime

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class AndroidLiteRtModelRunner(
    context: Context
) : LiteRtModelRunner {

    private val interpreter: Interpreter

    private val inputBuffer: ByteBuffer

    private val classificationScores: Array<Array<FloatArray>>

    private val boxRegression: Array<Array<FloatArray>>

    private val classificationOutputIndex: Int

    private val boxOutputIndex: Int

    init {
        val modelBuffer = loadModelBuffer(context)

        interpreter = Interpreter(
            modelBuffer,
            Interpreter.Options().setNumThreads(4)
        )

        try {
            validateInputTensor()

            classificationOutputIndex =
                findOutputIndex(
                    expectedName =
                        EfficientDetLite0ModelContract
                            .CLASSIFICATION_OUTPUT_TENSOR_NAME,
                    expectedShape = intArrayOf(
                        1,
                        EfficientDetLite0ModelContract
                            .CLASSIFICATION_OUTPUT_ANCHORS,
                        EfficientDetLite0ModelContract
                            .CLASSIFICATION_OUTPUT_CLASSES
                    )
                )

            boxOutputIndex =
                findOutputIndex(
                    expectedName =
                        EfficientDetLite0ModelContract
                            .BOX_OUTPUT_TENSOR_NAME,
                    expectedShape = intArrayOf(
                        1,
                        EfficientDetLite0ModelContract
                            .BOX_OUTPUT_ANCHORS,
                        EfficientDetLite0ModelContract
                            .BOX_OUTPUT_VALUES
                    )
                )

            require(classificationOutputIndex != boxOutputIndex) {
                "Classification and box outputs resolved to the same index"
            }

            classificationScores = Array(1) {
                Array(
                    EfficientDetLite0ModelContract
                        .CLASSIFICATION_OUTPUT_ANCHORS
                ) {
                    FloatArray(
                        EfficientDetLite0ModelContract
                            .CLASSIFICATION_OUTPUT_CLASSES
                    )
                }
            }

            boxRegression = Array(1) {
                Array(
                    EfficientDetLite0ModelContract
                        .BOX_OUTPUT_ANCHORS
                ) {
                    FloatArray(
                        EfficientDetLite0ModelContract
                            .BOX_OUTPUT_VALUES
                    )
                }
            }

            inputBuffer = ByteBuffer.allocateDirect(
                EfficientDetLite0ModelContract.INPUT_WIDTH *
                        EfficientDetLite0ModelContract.INPUT_HEIGHT *
                        EfficientDetLite0ModelContract.INPUT_CHANNELS
            ).order(ByteOrder.nativeOrder())
        } catch (exception: Exception) {
            interpreter.close()
            throw exception
        }
    }

    override fun run(
        input: ByteArray
    ): EfficientDetLite0RawOutput {
        val expectedInputSize =
            EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS

        require(input.size == expectedInputSize) {
            "Unexpected LiteRT input size: ${input.size}; " +
                    "expected $expectedInputSize"
        }

        inputBuffer.clear()
        inputBuffer.put(input)
        inputBuffer.rewind()

        val outputs = mutableMapOf<Int, Any>(
            classificationOutputIndex to classificationScores,
            boxOutputIndex to boxRegression
        )

        val startNanos = System.nanoTime()

        interpreter.runForMultipleInputsOutputs(
            arrayOf(inputBuffer),
            outputs
        )

        val inferenceTimeMs =
            (System.nanoTime() - startNanos) / 1_000_000L

        val flatClassificationScores = FloatArray(
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES
        )

        var classificationOffset = 0

        for (anchorScores in classificationScores[0]) {
            anchorScores.copyInto(
                destination = flatClassificationScores,
                destinationOffset = classificationOffset
            )

            classificationOffset += anchorScores.size
        }

        val flatBoxRegression = FloatArray(
            EfficientDetLite0ModelContract.BOX_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.BOX_OUTPUT_VALUES
        )

        var boxOffset = 0

        for (boxValues in boxRegression[0]) {
            boxValues.copyInto(
                destination = flatBoxRegression,
                destinationOffset = boxOffset
            )

            boxOffset += boxValues.size
        }

        return EfficientDetLite0RawOutput(
            classificationScores = flatClassificationScores,
            boxRegression = flatBoxRegression,
            inferenceTimeMs = inferenceTimeMs
        )
    }

    private fun validateInputTensor() {
        val inputTensor = interpreter.getInputTensor(0)

        val expectedShape = intArrayOf(
            1,
            EfficientDetLite0ModelContract.INPUT_HEIGHT,
            EfficientDetLite0ModelContract.INPUT_WIDTH,
            EfficientDetLite0ModelContract.INPUT_CHANNELS
        )

        require(inputTensor.name() ==
                EfficientDetLite0ModelContract.INPUT_TENSOR_NAME
        ) {
            "Unexpected input tensor name: ${inputTensor.name()}"
        }

        require(inputTensor.dataType() == DataType.UINT8) {
            "Expected UINT8 input, got ${inputTensor.dataType()}"
        }

        require(inputTensor.shape().contentEquals(expectedShape)) {
            "Unexpected input tensor shape: " +
                    "${inputTensor.shape().contentToString()}; " +
                    "expected ${expectedShape.contentToString()}"
        }
    }

    private fun findOutputIndex(
        expectedName: String,
        expectedShape: IntArray
    ): Int {
        val matchingIndices = (0 until interpreter.outputTensorCount)
            .filter { index ->
                interpreter.getOutputTensor(index).name() == expectedName
            }

        require(matchingIndices.size == 1) {
            "Expected exactly one output named $expectedName; " +
                    "found ${matchingIndices.size}"
        }

        val outputIndex = matchingIndices.single()
        val tensor = interpreter.getOutputTensor(outputIndex)

        require(tensor.dataType() == DataType.FLOAT32) {
            "Expected FLOAT32 output for $expectedName, " +
                    "got ${tensor.dataType()}"
        }

        require(tensor.shape().contentEquals(expectedShape)) {
            "Unexpected shape for $expectedName: " +
                    "${tensor.shape().contentToString()}; " +
                    "expected ${expectedShape.contentToString()}"
        }

        return outputIndex
    }

    private fun loadModelBuffer(
        context: Context
    ): ByteBuffer {
        val modelBytes = context.assets.open(
            EfficientDetLite0ModelContract.MODEL_ASSET_NAME
        ).use { inputStream ->
            inputStream.readBytes()
        }

        return ByteBuffer.allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())
            .apply {
                put(modelBytes)
                rewind()
            }
    }

    override fun close() {
        interpreter.close()
    }
}