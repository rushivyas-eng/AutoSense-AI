package com.autosense.ai.camera

import kotlin.math.abs

internal object CameraSizeSelector {

    fun select(
        outputSizes: List<CameraOutputSize>,
        requestedWidth: Int,
        requestedHeight: Int
    ): CameraOutputSize {

        require(requestedWidth > 0) {
            "requestedWidth must be greater than zero"
        }

        require(requestedHeight > 0) {
            "requestedHeight must be greater than zero"
        }

        require(outputSizes.isNotEmpty()) {
            "Camera does not support YUV_420_888 output"
        }

        val exactMatch =
            outputSizes.firstOrNull { size ->
                size.width == requestedWidth &&
                        size.height == requestedHeight
            }

        if (exactMatch != null) {
            return exactMatch
        }

        val requestedAspectRatio =
            requestedWidth.toDouble() / requestedHeight.toDouble()

        val sameAspectRatioSizes =
            outputSizes.filter { size ->
                val aspectRatio =
                    size.width.toDouble() / size.height.toDouble()

                isSameAspectRatio(
                    aspectRatio,
                    requestedAspectRatio
                )
            }

        val candidates =
            if (sameAspectRatioSizes.isNotEmpty()) {
                sameAspectRatioSizes
            } else {
                outputSizes
            }

        return candidates.minBy { size ->
            abs(
                size.width * size.height -
                        requestedWidth * requestedHeight
            )
        }
    }

    private fun isSameAspectRatio(
        first: Double,
        second: Double
    ): Boolean {
        return abs(first - second) < ASPECT_RATIO_TOLERANCE
    }

    private const val ASPECT_RATIO_TOLERANCE = 0.01
}