package com.autosense.ai.api.vision

import com.autosense.ai.api.camera.CameraFrame

interface VisionEngine {

    fun process(frame: CameraFrame): VisionResult
}