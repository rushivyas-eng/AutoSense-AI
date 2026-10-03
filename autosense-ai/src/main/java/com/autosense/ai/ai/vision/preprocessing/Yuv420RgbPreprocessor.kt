package com.autosense.ai.ai.vision.preprocessing

import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameFormat

internal class Yuv420RgbPreprocessor(
    private val outputWidth: Int = DEFAULT_OUTPUT_WIDTH,
    private val outputHeight: Int = DEFAULT_OUTPUT_HEIGHT,
    private val rgbConverter: Yuv420RgbConverter =
        Bt601Yuv420RgbConverter()
) : VisionPreprocessor {

    override fun preprocess(
        frame: CameraFrame
    ): PreprocessedVisionInput {

        require(frame.data.format == FrameFormat.YUV_420_888) {
            "Unsupported frame format: ${frame.data.format}"
        }

        require(frame.width % 2 == 0) {
            "YUV_420_888 frame width must be even"
        }

        require(frame.height % 2 == 0) {
            "YUV_420_888 frame height must be even"
        }

        val rotation =
            normalizeRotation(
                frame.rotationDegrees
            )

        val orientedWidth =
            if (rotation == 0 || rotation == 180) {
                frame.width
            } else {
                frame.height
            }

        val orientedHeight =
            if (rotation == 0 || rotation == 180) {
                frame.height
            } else {
                frame.width
            }

        var cropSize =
            minOf(
                orientedWidth,
                orientedHeight
            )

        /*
         * YUV 4:2:0 chroma samples are arranged on
         * even-sized dimensions, so keep the crop even.
         */
        if (cropSize % 2 != 0) {
            cropSize--
        }

        require(cropSize > 0) {
            "Frame is too small for square crop"
        }

        val cropLeft =
            ((orientedWidth - cropSize) / 2)
                .let { value ->
                    value - (value % 2)
                }

        val cropTop =
            ((orientedHeight - cropSize) / 2)
                .let { value ->
                    value - (value % 2)
                }

        val pixels =
            ByteArray(
                outputWidth *
                        outputHeight *
                        VisionInputImage.CHANNEL_COUNT
            )

        rgbConverter.convert(
            frameData = frame.data,
            sourceWidth = frame.width,
            sourceHeight = frame.height,
            rotationDegrees = rotation,
            cropLeft = cropLeft,
            cropTop = cropTop,
            cropWidth = cropSize,
            cropHeight = cropSize,
            outputWidth = outputWidth,
            outputHeight = outputHeight,
            output = pixels
        )

        val image =
            VisionInputImage(
                width = outputWidth,
                height = outputHeight,
                pixels = pixels
            )

        val transform =
            VisionTransform(
                sourceWidth = frame.width,
                sourceHeight = frame.height,
                rotationDegrees = rotation,
                orientedWidth = orientedWidth,
                orientedHeight = orientedHeight,
                cropLeft = cropLeft,
                cropTop = cropTop,
                cropWidth = cropSize,
                cropHeight = cropSize,
                outputWidth = outputWidth,
                outputHeight = outputHeight
            )

        return PreprocessedVisionInput(
            image = image,
            transform = transform
        )
    }

    private fun normalizeRotation(
        degrees: Int
    ): Int {
        require(degrees % 90 == 0) {
            "Frame rotation must be a multiple of 90 degrees"
        }

        return ((degrees % 360) + 360) % 360
    }

    companion object {
        const val DEFAULT_OUTPUT_WIDTH = 320
        const val DEFAULT_OUTPUT_HEIGHT = 320
    }
}