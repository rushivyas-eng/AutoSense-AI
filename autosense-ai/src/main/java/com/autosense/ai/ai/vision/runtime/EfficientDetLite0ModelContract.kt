package com.autosense.ai.ai.vision.runtime

internal object EfficientDetLite0ModelContract {

    const val MODEL_ASSET_NAME = "efficientdet_lite0_int8.tflite"

    const val INPUT_TENSOR_NAME = "serving_default_images:0"
    const val INPUT_TENSOR_INDEX = 0

    const val INPUT_WIDTH = 320
    const val INPUT_HEIGHT = 320
    const val INPUT_CHANNELS = 3

    const val INPUT_QUANTIZATION_SCALE = 0.0078125f
    const val INPUT_QUANTIZATION_ZERO_POINT = 127

    const val CLASSIFICATION_OUTPUT_TENSOR_NAME =
        "StatefulPartitionedCall:1"
    const val CLASSIFICATION_OUTPUT_TENSOR_INDEX = 593

    const val CLASSIFICATION_OUTPUT_ANCHORS = 19206
    const val CLASSIFICATION_OUTPUT_CLASSES = 90

    const val BOX_OUTPUT_TENSOR_NAME =
        "StatefulPartitionedCall:0"
    const val BOX_OUTPUT_TENSOR_INDEX = 596

    const val BOX_OUTPUT_ANCHORS = 19206
    const val BOX_OUTPUT_VALUES = 4

    const val OUTPUT_COUNT = 2

    init {
        require(INPUT_WIDTH > 0)
        require(INPUT_HEIGHT > 0)
        require(INPUT_CHANNELS == 3)

        require(INPUT_QUANTIZATION_SCALE > 0f)
        require(INPUT_QUANTIZATION_ZERO_POINT in 0..255)

        require(CLASSIFICATION_OUTPUT_ANCHORS == BOX_OUTPUT_ANCHORS)
        require(CLASSIFICATION_OUTPUT_CLASSES > 0)
        require(BOX_OUTPUT_VALUES == 4)
    }
}