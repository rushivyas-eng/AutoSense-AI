package com.autosense.ai.camera

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

internal class AndroidCameraDescriptorProvider(
    private val cameraManager: CameraManager
) {

    fun getDescriptors(): List<CameraDescriptor> {
        return cameraManager.cameraIdList.map { cameraId ->
            val characteristics =
                cameraManager.getCameraCharacteristics(
                    cameraId
                )

            CameraDescriptor(
                cameraId = cameraId,
                facing = mapFacing(
                    characteristics.get(
                        CameraCharacteristics.LENS_FACING
                    )
                )
            )
        }
    }

    private fun mapFacing(
        lensFacing: Int?
    ): CameraFacing {
        return when (lensFacing) {
            CameraCharacteristics.LENS_FACING_BACK ->
                CameraFacing.BACK

            CameraCharacteristics.LENS_FACING_FRONT ->
                CameraFacing.FRONT

            CameraCharacteristics.LENS_FACING_EXTERNAL ->
                CameraFacing.EXTERNAL

            else ->
                CameraFacing.UNKNOWN
        }
    }
}