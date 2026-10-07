package com.autosense.ai.ai.vision.runtime

import kotlin.math.exp

internal class EfficientDetLite0BoxDecoder {

    fun decode(
        rawBox: FloatArray,
        anchor: FloatArray
    ): FloatArray {
        require(rawBox.size == VALUES_PER_BOX) {
            "rawBox must contain exactly 4 values"
        }

        require(anchor.size == VALUES_PER_BOX) {
            "anchor must contain exactly 4 values"
        }

        val rawY = rawBox[0]
        val rawX = rawBox[1]
        val rawH = rawBox[2]
        val rawW = rawBox[3]

        require(rawBox.all { it.isFinite() }) {
            "rawBox must contain only finite values"
        }

        require(anchor.all { it.isFinite() }) {
            "anchor must contain only finite values"
        }

        val anchorYMin = anchor[0]
        val anchorXMin = anchor[1]
        val anchorYMax = anchor[2]
        val anchorXMax = anchor[3]

        require(anchorYMin < anchorYMax) {
            "anchor must have positive height"
        }

        require(anchorXMin < anchorXMax) {
            "anchor must have positive width"
        }

        val anchorCenterY =
            (anchorYMin + anchorYMax) / 2.0f

        val anchorCenterX =
            (anchorXMin + anchorXMax) / 2.0f

        val anchorHeight =
            anchorYMax - anchorYMin

        val anchorWidth =
            anchorXMax - anchorXMin

        val centerY =
            rawY * anchorHeight + anchorCenterY

        val centerX =
            rawX * anchorWidth + anchorCenterX

        val height =
            exp(rawH.toDouble()).toFloat() * anchorHeight

        val width =
            exp(rawW.toDouble()).toFloat() * anchorWidth

        require(height.isFinite()) {
            "Decoded height is not finite"
        }

        require(width.isFinite()) {
            "Decoded width is not finite"
        }

        return floatArrayOf(
            centerY - height / 2.0f,
            centerX - width / 2.0f,
            centerY + height / 2.0f,
            centerX + width / 2.0f
        )
    }

    companion object {
        private const val VALUES_PER_BOX = 4
    }
}