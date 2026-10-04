package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Test

class EfficientDetLite0ModelContractTest {

    @Test
    fun modelAssetName_isCorrect() {
        assertEquals(
            "efficientdet_lite0_int8.tflite",
            EfficientDetLite0ModelContract.MODEL_ASSET_NAME
        )
    }

    @Test
    fun inputContract_matchesInspectedModel() {
        assertEquals(
            "serving_default_images:0",
            EfficientDetLite0ModelContract.INPUT_TENSOR_NAME
        )
        assertEquals(
            0,
            EfficientDetLite0ModelContract.INPUT_TENSOR_INDEX
        )
        assertEquals(
            320,
            EfficientDetLite0ModelContract.INPUT_WIDTH
        )
        assertEquals(
            320,
            EfficientDetLite0ModelContract.INPUT_HEIGHT
        )
        assertEquals(
            3,
            EfficientDetLite0ModelContract.INPUT_CHANNELS
        )
    }

    @Test
    fun inputQuantization_matchesInspectedModel() {
        assertEquals(
            0.0078125f,
            EfficientDetLite0ModelContract.INPUT_QUANTIZATION_SCALE,
            0f
        )
        assertEquals(
            127,
            EfficientDetLite0ModelContract.INPUT_QUANTIZATION_ZERO_POINT
        )
    }

    @Test
    fun classificationOutput_matchesInspectedModel() {
        assertEquals(
            "StatefulPartitionedCall:1",
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_TENSOR_NAME
        )
        assertEquals(
            593,
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_TENSOR_INDEX
        )
        assertEquals(
            19206,
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS
        )
        assertEquals(
            90,
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES
        )
    }

    @Test
    fun boxOutput_matchesInspectedModel() {
        assertEquals(
            "StatefulPartitionedCall:0",
            EfficientDetLite0ModelContract.BOX_OUTPUT_TENSOR_NAME
        )
        assertEquals(
            596,
            EfficientDetLite0ModelContract.BOX_OUTPUT_TENSOR_INDEX
        )
        assertEquals(
            19206,
            EfficientDetLite0ModelContract.BOX_OUTPUT_ANCHORS
        )
        assertEquals(
            4,
            EfficientDetLite0ModelContract.BOX_OUTPUT_VALUES
        )
    }

    @Test
    fun model_hasTwoOutputs() {
        assertEquals(
            2,
            EfficientDetLite0ModelContract.OUTPUT_COUNT
        )
    }
}