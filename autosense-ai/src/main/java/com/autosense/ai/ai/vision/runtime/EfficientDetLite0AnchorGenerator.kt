package com.autosense.ai.ai.vision.runtime

import kotlin.math.pow
import kotlin.math.sqrt

internal class EfficientDetLite0AnchorGenerator(
    private val config: EfficientDetLite0PostprocessingConfig =
        EfficientDetLite0PostprocessingConfig()
) {

    fun generate(): FloatArray {
        val anchorsPerLocation =
            config.numScales * config.aspectRatios.size

        val totalAnchors =
            (config.minLevel..config.maxLevel).sumOf { level ->
                featureMapHeight(level) *
                        featureMapWidth(level) *
                        anchorsPerLocation
            }

        require(totalAnchors == config.numAnchors) {
            "Generated anchor count $totalAnchors does not match " +
                    "expected ${config.numAnchors}"
        }

        val anchors = FloatArray(
            totalAnchors * VALUES_PER_ANCHOR
        )

        var anchorIndex = 0

        for (level in config.minLevel..config.maxLevel) {
            val featureHeight = featureMapHeight(level)
            val featureWidth = featureMapWidth(level)

            val strideY =
                config.imageHeight.toFloat() / featureHeight

            val strideX =
                config.imageWidth.toFloat() / featureWidth

            /*
             * EfficientDet anchor ordering is:
             *
             *   level
             *     -> scale
             *       -> aspect ratio
             *         -> feature-map row
             *           -> feature-map column
             *
             * This ordering must match the model output ordering.
             */
            for (scaleIndex in 0 until config.numScales) {
                val octaveScale =
                    scaleIndex.toFloat() / config.numScales

                val scale =
                    2.0.pow(octaveScale.toDouble()).toFloat()

                val baseAnchorWidth =
                    config.anchorScale * strideX * scale

                val baseAnchorHeight =
                    config.anchorScale * strideY * scale

                for (aspectRatio in config.aspectRatios) {
                    val aspectX = sqrt(aspectRatio)
                    val aspectY = 1.0f / aspectX

                    val anchorWidth =
                        baseAnchorWidth * aspectX

                    val anchorHeight =
                        baseAnchorHeight * aspectY

                    val halfWidth =
                        anchorWidth / 2.0f

                    val halfHeight =
                        anchorHeight / 2.0f

                    for (y in 0 until featureHeight) {
                        val centerY =
                            (y + 0.5f) * strideY

                        for (x in 0 until featureWidth) {
                            val centerX =
                                (x + 0.5f) * strideX

                            anchors[anchorIndex++] =
                                centerY - halfHeight

                            anchors[anchorIndex++] =
                                centerX - halfWidth

                            anchors[anchorIndex++] =
                                centerY + halfHeight

                            anchors[anchorIndex++] =
                                centerX + halfWidth
                        }
                    }
                }
            }
        }

        return anchors
    }

    private fun featureMapHeight(level: Int): Int {
        val stride = 1 shl level

        return (
                config.imageHeight + stride - 1
                ) / stride
    }

    private fun featureMapWidth(level: Int): Int {
        val stride = 1 shl level

        return (
                config.imageWidth + stride - 1
                ) / stride
    }

    companion object {
        private const val VALUES_PER_ANCHOR = 4
    }
}