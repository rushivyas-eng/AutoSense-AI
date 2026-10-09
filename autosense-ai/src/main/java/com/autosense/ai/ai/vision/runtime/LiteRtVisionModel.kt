package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionInputImage
import com.autosense.ai.ai.vision.preprocessing.VisionTransform

/** Connects RGB input encoding, LiteRT execution, and EfficientDet postprocessing. */
internal class LiteRtVisionModel(
    private val runner: LiteRtModelRunner,
    private val inputEncoder: EfficientDetLite0InputEncoder =
        EfficientDetLite0InputEncoder(),
    private val postprocessor: EfficientDetLite0Postprocessor =
        EfficientDetLite0Postprocessor()
) : VisionModel {

    override fun run(input: VisionModelInput): VisionModelOutput {
        val encodedInput = inputEncoder.encode(input.image)
        val rawOutput = runner.run(encodedInput)
        val detections = postprocessor.process(rawOutput, input.transform)

        return VisionModelOutput(
            detections = detections,
            inferenceTimeMs = rawOutput.inferenceTimeMs
        )
    }

    override fun close() {
        runner.close()
    }
}
