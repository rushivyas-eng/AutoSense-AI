package com.autosense.ai.ai.vision.preprocessing

internal data class VisionInputImage(
    val width: Int,
    val height: Int,
    val pixels: ByteArray
) {
    init {
        require(width > 0) {
            "width must be greater than zero"
        }

        require(height > 0) {
            "height must be greater than zero"
        }

        require(pixels.size == width * height * CHANNEL_COUNT) {
            "RGB pixel buffer size does not match image dimensions"
        }
    }

    companion object {
        const val CHANNEL_COUNT = 3
    }
}
