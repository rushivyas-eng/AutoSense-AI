package com.autosense.ai.ai.vision.runtime

internal data class EfficientDetLite0PostprocessingConfig(
    val imageWidth: Int = EfficientDetLite0ModelContract.INPUT_WIDTH,
    val imageHeight: Int = EfficientDetLite0ModelContract.INPUT_HEIGHT,
    val minLevel: Int = 3,
    val maxLevel: Int = 7,
    val numScales: Int = 3,
    val aspectRatios: List<Float> = listOf(
        1.0f,
        2.0f,
        0.5f
    ),
    val anchorScale: Float = 3.0f,
    val numClasses: Int = EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_CLASSES,
    val numAnchors: Int = EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS,
    val maxDetections: Int = 100
) {

    init {
        require(imageWidth > 0) {
            "imageWidth must be greater than zero"
        }

        require(imageHeight > 0) {
            "imageHeight must be greater than zero"
        }

        require(minLevel <= maxLevel) {
            "minLevel must not be greater than maxLevel"
        }

        require(numScales > 0) {
            "numScales must be greater than zero"
        }

        require(aspectRatios.isNotEmpty()) {
            "aspectRatios must not be empty"
        }

        require(aspectRatios.all { it > 0f }) {
            "aspectRatios must contain only positive values"
        }

        require(anchorScale > 0f) {
            "anchorScale must be greater than zero"
        }

        require(numClasses > 0) {
            "numClasses must be greater than zero"
        }

        require(numAnchors > 0) {
            "numAnchors must be greater than zero"
        }

        require(maxDetections > 0) {
            "maxDetections must be greater than zero"
        }
    }
}