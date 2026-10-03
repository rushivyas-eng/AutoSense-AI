package com.autosense.ai.ai.vision.preprocessing

import com.autosense.ai.api.camera.FrameData
import java.nio.ByteBuffer

internal class Bt601Yuv420RgbConverter : Yuv420RgbConverter {

    override fun convert(
        frameData: FrameData,
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        cropLeft: Int,
        cropTop: Int,
        cropWidth: Int,
        cropHeight: Int,
        outputWidth: Int,
        outputHeight: Int,
        output: ByteArray
    ) {
        require(sourceWidth > 0)
        require(sourceHeight > 0)

        require(sourceWidth % 2 == 0) {
            "YUV_420_888 source width must be even"
        }

        require(sourceHeight % 2 == 0) {
            "YUV_420_888 source height must be even"
        }

        require(rotationDegrees == 0 ||
                rotationDegrees == 90 ||
                rotationDegrees == 180 ||
                rotationDegrees == 270
        )

        require(cropWidth > 0)
        require(cropHeight > 0)

        require(outputWidth > 0)
        require(outputHeight > 0)

        require(output.size == outputWidth * outputHeight * 3) {
            "Output RGB buffer size does not match dimensions"
        }

        require(frameData.planes.size == 3) {
            "YUV_420_888 requires exactly three planes"
        }

        val yPlane = PlaneReader(frameData.planes[0])
        val uPlane = PlaneReader(frameData.planes[1])
        val vPlane = PlaneReader(frameData.planes[2])

        require(yPlane.pixelStride == 1) {
            "YUV_420_888 Y plane pixel stride must be 1"
        }

        require(uPlane.pixelStride == vPlane.pixelStride) {
            "YUV_420_888 U/V pixel strides must match"
        }

        require(uPlane.rowStride == vPlane.rowStride) {
            "YUV_420_888 U/V row strides must match"
        }

        var outputIndex = 0

        for (outputY in 0 until outputHeight) {
            val cropY =
                ((outputY + 0.5) * cropHeight / outputHeight)
                    .toInt()
                    .coerceIn(0, cropHeight - 1)

            val orientedY = cropTop + cropY

            for (outputX in 0 until outputWidth) {
                val cropX =
                    ((outputX + 0.5) * cropWidth / outputWidth)
                        .toInt()
                        .coerceIn(0, cropWidth - 1)

                val orientedX = cropLeft + cropX

                val sourceCoordinates =
                    mapOrientedToSource(
                        sourceWidth = sourceWidth,
                        sourceHeight = sourceHeight,
                        rotationDegrees = rotationDegrees,
                        orientedX = orientedX,
                        orientedY = orientedY
                    )

                val sourceX = sourceCoordinates.first
                val sourceY = sourceCoordinates.second

                val yValue =
                    yPlane.read(
                        x = sourceX,
                        y = sourceY
                    )

                val chromaX = sourceX / 2
                val chromaY = sourceY / 2

                val uValue =
                    uPlane.read(
                        x = chromaX,
                        y = chromaY
                    )

                val vValue =
                    vPlane.read(
                        x = chromaX,
                        y = chromaY
                    )

                val rgb =
                    convertYuvToRgb(
                        y = yValue,
                        u = uValue,
                        v = vValue
                    )

                output[outputIndex++] = rgb.first.toByte()
                output[outputIndex++] = rgb.second.toByte()
                output[outputIndex++] = rgb.third.toByte()
            }
        }
    }

    private fun mapOrientedToSource(
        sourceWidth: Int,
        sourceHeight: Int,
        rotationDegrees: Int,
        orientedX: Int,
        orientedY: Int
    ): Pair<Int, Int> {

        return when (rotationDegrees) {
            0 -> {
                Pair(
                    orientedX,
                    orientedY
                )
            }

            90 -> {
                Pair(
                    orientedY,
                    sourceHeight - 1 - orientedX
                )
            }

            180 -> {
                Pair(
                    sourceWidth - 1 - orientedX,
                    sourceHeight - 1 - orientedY
                )
            }

            270 -> {
                Pair(
                    sourceWidth - 1 - orientedY,
                    orientedX
                )
            }

            else -> {
                error(
                    "Unsupported rotation: $rotationDegrees"
                )
            }
        }
    }

    private fun convertYuvToRgb(
        y: Int,
        u: Int,
        v: Int
    ): Triple<Int, Int, Int> {

        val c = (y - 16).coerceAtLeast(0)
        val d = u - 128
        val e = v - 128

        val red =
            ((298 * c + 409 * e + 128) shr 8)
                .coerceIn(0, 255)

        val green =
            ((298 * c - 100 * d - 208 * e + 128) shr 8)
                .coerceIn(0, 255)

        val blue =
            ((298 * c + 516 * d + 128) shr 8)
                .coerceIn(0, 255)

        return Triple(
            red,
            green,
            blue
        )
    }

    private class PlaneReader(
        plane: com.autosense.ai.api.camera.FramePlane
    ) {

        private val buffer: ByteBuffer =
            plane.buffer.duplicate()

        val rowStride: Int = plane.rowStride
        val pixelStride: Int = plane.pixelStride

        private val basePosition: Int =
            buffer.position()

        init {
            require(rowStride > 0) {
                "Plane row stride must be greater than zero"
            }

            require(pixelStride > 0) {
                "Plane pixel stride must be greater than zero"
            }
        }

        fun read(
            x: Int,
            y: Int
        ): Int {

            require(x >= 0)
            require(y >= 0)

            val offset =
                basePosition +
                        y * rowStride +
                        x * pixelStride

            require(offset < buffer.limit()) {
                "Plane buffer does not contain requested sample"
            }

            return buffer.get(offset).toInt() and 0xFF
        }
    }
}