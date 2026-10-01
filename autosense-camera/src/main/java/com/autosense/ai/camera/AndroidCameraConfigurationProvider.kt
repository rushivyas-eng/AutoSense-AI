package com.autosense.ai.camera

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

internal class AndroidCameraConfigurationProvider(
    private val cameraManager: CameraManager,
    private val cameraId: String,
    private val requestedWidth: Int,
    private val requestedHeight: Int
) : CameraConfigurationProvider {

    override fun getConfiguration(): CameraConfiguration {
        val characteristics =
            cameraManager.getCameraCharacteristics(
                cameraId
            )

        val sensorOrientation =
            characteristics.get(
                CameraCharacteristics.SENSOR_ORIENTATION
            ) ?: 0

        val outputSizes =
            characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )?.getOutputSizes(
                ImageFormat.YUV_420_888
            ) ?: emptyArray()

        val cameraOutputSizes =
            outputSizes.map { size ->
                CameraOutputSize(
                    width = size.width,
                    height = size.height
                )
            }

        val selectedSize =
            CameraSizeSelector.select(
                outputSizes = cameraOutputSizes,
                requestedWidth = requestedWidth,
                requestedHeight = requestedHeight
            )

        return CameraConfiguration(
            cameraId = cameraId,
            width = selectedSize.width,
            height = selectedSize.height,
            rotationDegrees = sensorOrientation
        )
    }
}