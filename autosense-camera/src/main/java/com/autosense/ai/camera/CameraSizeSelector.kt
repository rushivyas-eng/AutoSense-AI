package com.autosense.ai.camera

import kotlin.math.abs

internal object CameraSizeSelector {

    fun select(
        outputSizes: List<CameraOutputSize>,
        requestedWidth: Int,
        requestedHeight: Int
    ): CameraOutputSize {

        require(outputSizes.isNotEmpty()) {
            "Camera does not support YUV_420_888 output"
        }

        return outputSizes.firstOrNull { size ->
            size.width == requestedWidth &&
                    size.height == requestedHeight
        } ?: outputSizes.minBy { size ->
            abs(
                size.width * size.height -
                        requestedWidth * requestedHeight
            )
        }
    }
}