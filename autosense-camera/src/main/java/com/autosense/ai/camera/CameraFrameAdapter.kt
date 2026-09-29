package com.autosense.ai.camera

import android.graphics.ImageFormat
import android.media.Image
import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane

internal class CameraFrameAdapter {

    fun adapt(
        image: Image,
        sequenceNumber: Long,
        rotationDegrees: Int
    ): CameraFrame {

        require(image.format == ImageFormat.YUV_420_888) {
            "Unsupported image format: ${image.format}"
        }

        try {
            val planes = image.planes.map { plane ->
                FramePlane(
                    buffer = plane.buffer,
                    rowStride = plane.rowStride,
                    pixelStride = plane.pixelStride
                )
            }

            val frameData = AndroidFrameData(
                format = FrameFormat.YUV_420_888,
                planes = planes,
                closeAction = image::close
            )

            return CameraFrame(
                timestampNanos = image.timestamp,
                sequenceNumber = sequenceNumber,
                width = image.width,
                height = image.height,
                rotationDegrees = rotationDegrees,
                data = frameData
            )
        } catch (exception: Exception) {
            image.close()
            throw exception
        }
    }
}