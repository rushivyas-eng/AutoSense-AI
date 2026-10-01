package com.autosense.ai.camera

internal data class CameraConfiguration(
    val cameraId: String,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int
)