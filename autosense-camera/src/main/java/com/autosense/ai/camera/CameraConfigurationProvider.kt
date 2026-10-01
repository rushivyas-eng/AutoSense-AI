package com.autosense.ai.camera

internal interface CameraConfigurationProvider {

    fun getConfiguration(): CameraConfiguration
}