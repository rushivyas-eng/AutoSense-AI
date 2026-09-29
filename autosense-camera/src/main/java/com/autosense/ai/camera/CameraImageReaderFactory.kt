package com.autosense.ai.camera

internal interface CameraImageReaderFactory {

    fun create(
        width: Int,
        height: Int,
        onImageAvailable: (android.media.Image) -> Unit
    ): CameraImageReaderHandle
}