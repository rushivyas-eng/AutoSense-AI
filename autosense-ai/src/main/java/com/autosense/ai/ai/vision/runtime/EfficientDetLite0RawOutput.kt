package com.autosense.ai.ai.vision.runtime

internal data class EfficientDetLite0RawOutput(
    val classificationScores: FloatArray,
    val boxRegression: FloatArray,
    val inferenceTimeMs: Long
) {

    init {
        require(
            classificationScores.size ==
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES
        ) {
            "Classification output size does not match the EfficientDet-Lite0 contract"
        }

        require(
            boxRegression.size ==
                    EfficientDetLite0ModelContract.BOX_OUTPUT_ANCHORS *
                    EfficientDetLite0ModelContract.BOX_OUTPUT_VALUES
        ) {
            "Box regression output size does not match the EfficientDet-Lite0 contract"
        }

        require(inferenceTimeMs >= 0) {
            "inferenceTimeMs must be non-negative"
        }
    }
}