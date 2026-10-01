package com.autosense.ai.camera

internal object CameraSelector {

    fun select(
        cameras: List<CameraDescriptor>
    ): CameraDescriptor {

        require(cameras.isNotEmpty()) {
            "No camera is available"
        }

        return cameras.firstOrNull {
            it.facing == CameraFacing.BACK
        } ?: cameras.first()
    }
}