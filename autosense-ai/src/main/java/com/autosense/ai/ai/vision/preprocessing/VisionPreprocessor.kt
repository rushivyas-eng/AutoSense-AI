package com.autosense.ai.ai.vision.preprocessing

import com.autosense.ai.api.camera.CameraFrame

internal interface VisionPreprocessor {

    fun preprocess(
        frame: CameraFrame
    ): PreprocessedVisionInput
}