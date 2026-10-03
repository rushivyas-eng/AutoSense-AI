package com.autosense.ai.ai.vision.preprocessing

internal data class VisionTransform(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val rotationDegrees: Int,
    val orientedWidth: Int,
    val orientedHeight: Int,
    val cropLeft: Int,
    val cropTop: Int,
    val cropWidth: Int,
    val cropHeight: Int,
    val outputWidth: Int,
    val outputHeight: Int
) {
    init {
        require(sourceWidth > 0)
        require(sourceHeight > 0)

        require(rotationDegrees == 0 ||
                rotationDegrees == 90 ||
                rotationDegrees == 180 ||
                rotationDegrees == 270
        )

        require(orientedWidth > 0)
        require(orientedHeight > 0)

        require(cropLeft >= 0)
        require(cropTop >= 0)
        require(cropWidth > 0)
        require(cropHeight > 0)

        require(cropLeft + cropWidth <= orientedWidth)
        require(cropTop + cropHeight <= orientedHeight)

        require(outputWidth > 0)
        require(outputHeight > 0)
    }
}
