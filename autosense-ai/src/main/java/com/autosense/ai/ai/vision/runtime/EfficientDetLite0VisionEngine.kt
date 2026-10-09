package com.autosense.ai.ai.vision.runtime

import android.content.Context
import com.autosense.ai.ai.vision.preprocessing.VisionPreprocessor
import com.autosense.ai.ai.vision.preprocessing.Yuv420RgbPreprocessor
import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.vision.BoundingBox
import com.autosense.ai.api.vision.Detection
import com.autosense.ai.api.vision.VisionEngine
import com.autosense.ai.api.vision.VisionResult

/**
 * Android implementation of the public vision API backed by EfficientDet-Lite0.
 *
 * The caller owns [CameraFrame] lifecycle. The supplied label resolver must map
 * the model's foreground class IDs to the labels used by the application.
 */
class EfficientDetLite0VisionEngine private constructor(
    private val labelForClass: (Int) -> String,
    private val preprocessor: VisionPreprocessor,
    private val model: VisionModel
) : VisionEngine, AutoCloseable {

    constructor(
        context: Context,
        labelForClass: (Int) -> String
    ) : this(
        labelForClass = labelForClass,
        preprocessor = Yuv420RgbPreprocessor(),
        model = LiteRtVisionModel(AndroidLiteRtModelRunner(context))
    )

    internal constructor(
        context: Context,
        labelForClass: (Int) -> String,
        preprocessor: VisionPreprocessor,
        model: VisionModel
    ) : this(labelForClass, preprocessor, model)

    /** The LiteRT runner owns reusable buffers, so inference calls are serialized. */
    @Synchronized
    override fun process(frame: CameraFrame): VisionResult {
        val preprocessed = preprocessor.preprocess(frame)
        val modelOutput = model.run(
            VisionModelInput(
                image = preprocessed.image,
                transform = preprocessed.transform
            )
        )

        val detections = modelOutput.detections.map { detection ->
            Detection(
                label = labelForClass(detection.classId),
                confidence = detection.confidence,
                boundingBox = BoundingBox(
                    left = detection.left,
                    top = detection.top,
                    right = detection.right,
                    bottom = detection.bottom
                )
            )
        }

        return VisionResult(
            timestampNanos = frame.timestampNanos,
            inferenceTimeMs = modelOutput.inferenceTimeMs,
            detections = detections
        )
    }

    @Synchronized
    override fun close() {
        model.close()
    }
}
