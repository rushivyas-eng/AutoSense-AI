package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionInputImage
import com.autosense.ai.ai.vision.preprocessing.VisionTransform

internal data class VisionModelInput(
    val image: VisionInputImage,
    val transform: VisionTransform
)
