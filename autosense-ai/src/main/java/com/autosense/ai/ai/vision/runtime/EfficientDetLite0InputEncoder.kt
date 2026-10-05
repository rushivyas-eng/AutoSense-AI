package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionInputImage

internal class EfficientDetLite0InputEncoder {

    fun encode(image: VisionInputImage): ByteArray {
        require(
            image.width == EfficientDetLite0ModelContract.INPUT_WIDTH
        ) {
            "Unexpected input width: ${image.width}"
        }

        require(
            image.height == EfficientDetLite0ModelContract.INPUT_HEIGHT
        ) {
            "Unexpected input height: ${image.height}"
        }

        require(
            image.pixels.size ==
                    EfficientDetLite0ModelContract.INPUT_WIDTH *
                    EfficientDetLite0ModelContract.INPUT_HEIGHT *
                    EfficientDetLite0ModelContract.INPUT_CHANNELS
        ) {
            "Unexpected input pixel count"
        }

        /*
         * VisionInputImage already stores RGB pixels as UINT8-compatible
         * values in the range 0..255.
         *
         * LiteRT's TensorBuffer.writeInt8() is also used for UINT8 tensors.
         * The model's quantization parameters describe how the runtime
         * interprets these values:
         *
         *   real = (uint8 - 127) * 0.0078125
         *
         * No additional normalization is performed here.
         */
        return image.pixels.copyOf()
    }
}