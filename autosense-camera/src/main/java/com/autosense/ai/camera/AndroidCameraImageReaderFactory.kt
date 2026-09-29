package com.autosense.ai.camera

import android.os.Handler

internal class AndroidCameraImageReaderFactory(
    private val handler: Handler
) : CameraImageReaderFactory {

    override fun create(
        width: Int,
        height: Int,
        onImageAvailable: (android.media.Image) -> Unit
    ): CameraImageReaderHandle {
        return CameraImageReader(
            width = width,
            height = height,
            maxImages = CameraImageReader.DEFAULT_MAX_IMAGES,
            handler = handler,
            onImageAvailable = onImageAvailable
        )
    }
}