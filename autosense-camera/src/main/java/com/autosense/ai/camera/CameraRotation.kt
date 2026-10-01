package com.autosense.ai.camera

internal object CameraRotation {

    fun normalize(degrees: Int): Int {
        require(degrees % 90 == 0) {
            "Camera rotation must be a multiple of 90 degrees"
        }

        return ((degrees % 360) + 360) % 360
    }
}