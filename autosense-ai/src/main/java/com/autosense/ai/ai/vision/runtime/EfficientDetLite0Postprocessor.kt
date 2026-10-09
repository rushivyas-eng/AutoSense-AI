package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionTransform

/** Converts the model's raw tensors into detections in source-camera coordinates. */
internal class EfficientDetLite0Postprocessor(
    private val config: EfficientDetLite0PostprocessingConfig =
        EfficientDetLite0PostprocessingConfig(),
    private val anchorGenerator: EfficientDetLite0AnchorGenerator =
        EfficientDetLite0AnchorGenerator(config),
    private val scoreProcessor: EfficientDetLite0ScoreProcessor =
        EfficientDetLite0ScoreProcessor(),
    private val boxDecoder: EfficientDetLite0BoxDecoder =
        EfficientDetLite0BoxDecoder(),
    private val nms: EfficientDetLite0Nms = EfficientDetLite0Nms(
        maxDetections = config.maxDetections
    ),
    private val boxMapper: EfficientDetLite0BoxMapper =
        EfficientDetLite0BoxMapper()
) {
    fun process(
        rawOutput: EfficientDetLite0RawOutput,
        transform: VisionTransform
    ): List<VisionModelDetection> {
        val anchors = anchorGenerator.generate()
        val classScores = scoreProcessor.process(rawOutput.classificationScores)
        val candidates = ArrayList<EfficientDetLite0DetectionCandidate>()

        for (anchorIndex in classScores.indices) {
            val classScore = classScores[anchorIndex] ?: continue
            if (classScore.score < EfficientDetLite0Nms.DEFAULT_SCORE_THRESHOLD) {
                continue
            }
            val boxOffset = anchorIndex * VALUES_PER_BOX
            val rawBox = rawOutput.boxRegression.copyOfRange(
                boxOffset,
                boxOffset + VALUES_PER_BOX
            )
            val anchorOffset = anchorIndex * VALUES_PER_BOX
            val anchor = anchors.copyOfRange(
                anchorOffset,
                anchorOffset + VALUES_PER_BOX
            )
            val decodedBox = boxDecoder.decode(rawBox, anchor)

            // A box that is completely outside the model input cannot map to a
            // useful camera-space detection. Keep partial overlaps for clipping.
            if (decodedBox[0] >= config.imageHeight ||
                decodedBox[2] <= 0f ||
                decodedBox[1] >= config.imageWidth ||
                decodedBox[3] <= 0f
            ) {
                continue
            }

            candidates += EfficientDetLite0DetectionCandidate(
                anchorIndex = anchorIndex,
                classId = classScore.classId,
                score = classScore.score,
                box = decodedBox
            )
        }

        return nms.process(candidates).mapNotNull { candidate ->
            val mapped = boxMapper.map(candidate.box, transform)
            if (mapped.left >= mapped.right || mapped.top >= mapped.bottom) {
                null
            } else {
                VisionModelDetection(
                    classId = candidate.classId,
                    confidence = candidate.score,
                    left = mapped.left,
                    top = mapped.top,
                    right = mapped.right,
                    bottom = mapped.bottom
                )
            }
        }
    }

    private companion object {
        const val VALUES_PER_BOX = 4
    }
}
