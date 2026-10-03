package com.autosense.ai.ai.vision.preprocessing

import com.autosense.ai.api.camera.FrameData

internal interface Yuv420RgbConverter {

    fun convert(
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
    )
}