package com.autosense.ai.ai.vision.runtime

internal interface VisionModel : AutoCloseable {

    fun run(input: VisionModelInput): VisionModelOutput
}