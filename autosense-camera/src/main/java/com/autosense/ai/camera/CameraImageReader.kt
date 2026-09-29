package com.autosense.ai.camera

import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.view.Surface

internal class CameraImageReader(
    width: Int,
    height: Int,
    maxImages: Int,
    handler: Handler,
    private val onImageAvailable: (Image) -> Unit
) : CameraImageReaderHandle {

    companion object {
        const val DEFAULT_MAX_IMAGES = 3
    }

    private val imageReader: ImageReader

    init {
        require(width > 0) {
            "width must be greater than zero"
        }

        require(height > 0) {
            "height must be greater than zero"
        }

        require(maxImages >= 2) {
            "maxImages must be at least 2 for acquireLatestImage()"
        }

        imageReader = ImageReader.newInstance(
            width,
            height,
            ImageFormat.YUV_420_888,
            maxImages
        )

        imageReader.setOnImageAvailableListener(
            { reader ->
                handleImageAvailable(reader)
            },
            handler
        )
    }

    override val surface: Surface
        get() = imageReader.surface

    private fun handleImageAvailable(
        reader: ImageReader
    ) {
        val image = try {
            reader.acquireLatestImage()
        } catch (exception: IllegalStateException) {
            return
        }

        if (image == null) {
            return
        }

        try {
            onImageAvailable(image)
        } catch (exception: Exception) {
            image.close()
            throw exception
        }
    }

    override fun close() {
        imageReader.close()
    }
}