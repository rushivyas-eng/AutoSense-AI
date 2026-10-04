# EfficientDet-Lite0 INT8 Model

## Overview

AutoSense AI uses the EfficientDet-Lite0 INT8 object-detection model as its V1 vision model.

The model is executed locally on-device through LiteRT. It is not accessed through a cloud inference service.

## Model Artifact

- File: `efficientdet_lite0_int8.tflite`
- Size: 4,602,795 bytes
- SHA-256: `0720BF247BD76E6594EA28FA9C6F7C5242BE774818997DBBEFFC4D460C723BB`
- Model version: 3
- Subgraphs: 1

The SHA-256 value above was calculated from the model artifact used by this project.

## Source

Official Google MediaPipe model artifact:

https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/int8/1/efficientdet_lite0.tflite

The model artifact is stored in:

`autosense-ai/src/main/assets/efficientdet_lite0_int8.tflite`

## Verified Runtime Contract

The following metadata was extracted directly from the exact model artifact used by AutoSense AI.

### Input

| Property | Value |
|---|---|
| Tensor name | `serving_default_images:0` |
| Tensor index | `0` |
| Type | `UINT8` |
| Shape | `[1, 320, 320, 3]` |
| Quantization scale | `0.0078125` |
| Quantization zero point | `127` |

The model therefore receives a 320 × 320 RGB image as a UINT8 tensor.

The model-specific quantization relationship is:

`real_value = (uint8_value - 127) × 0.0078125`

Model-specific quantization is intentionally handled by the vision runtime rather than by the camera or generic preprocessing layers.

### Classification Output

| Property | Value |
|---|---|
| Tensor name | `StatefulPartitionedCall:1` |
| Tensor index | `593` |
| Type | `FLOAT32` |
| Shape | `[1, 19206, 90]` |

This output contains classification predictions for 19,206 anchors across 90 classes.

### Box Regression Output

| Property | Value |
|---|---|
| Tensor name | `StatefulPartitionedCall:0` |
| Tensor index | `596` |
| Type | `FLOAT32` |
| Shape | `[1, 19206, 4]` |

This output contains four box-regression values for each of the 19,206 anchors.

### Output Summary

This exact model exposes **two outputs**, not the four post-processed detection outputs sometimes associated with EfficientDet/TFLite detection APIs.

```text
StatefulPartitionedCall:1
    FLOAT32
    [1, 19206, 90]
    classification predictions

StatefulPartitionedCall:0
    FLOAT32
    [1, 19206, 4]
    box regression predictions
```

Post-processing is therefore an AutoSense AI responsibility.

## EfficientDet Post-processing Configuration

The V1 post-processing configuration is:

| Property | Value |
|---|---|
| Input width | 320 |
| Input height | 320 |
| Minimum feature level | 3 |
| Maximum feature level | 7 |
| Number of scales | 3 |
| Aspect ratios | `1.0`, `2.0`, `0.5` |
| Anchor scale | `3.0` |
| Number of classes | 90 |
| Number of anchors | 19,206 |
| Maximum detections | 100 |

The 19,206 anchors correspond to the five feature levels used by the model:

```text
P3: 40 × 40 × 9 = 14,400
P4: 20 × 20 × 9 =  3,600
P5: 10 × 10 × 9 =    900
P6:  5 ×  5 × 9 =     225
P7:  3 ×  3 × 9 =      81
                         ----
                       19,206
```

## Runtime Architecture

The vision pipeline is intentionally split into separate responsibilities:

```text
CameraFrame
    |
    v
VisionPreprocessor
    |
    | 320 × 320 canonical RGB
    v
VisionModelInput
    |
    v
LiteRtVisionModel
    |
    | LiteRT CompiledModel
    | UINT8 input
    | FLOAT32 raw outputs
    v
Raw VisionModelOutput
    |
    v
EfficientDetPostprocessor
    |
    +-- anchor generation
    +-- classification processing
    +-- box decoding
    +-- confidence filtering
    +-- non-maximum suppression
    |
    v
VisionResult
```

LiteRT-specific implementation details must remain inside the `autosense-ai` implementation module and must not leak into `autosense-api`.

## Preprocessing Boundary

The generic vision preprocessing layer produces canonical RGB pixels.

It does not know that the selected model is INT8.

The model adapter is responsible for converting the canonical RGB representation into the exact tensor representation required by EfficientDet-Lite0.

This keeps the preprocessing pipeline reusable if a future model uses FLOAT32, FLOAT16, or a different input representation.

## Detection Coordinate Contract

The model operates on a 320 × 320 model input.

The public AutoSense `Detection` contract uses normalized coordinates relative to the original camera frame.

Therefore, model-space bounding boxes must be transformed back through the stored `VisionTransform` before they are exposed as `VisionResult`.

The camera frame itself is not modified by the vision model runtime.

## Post-processing Responsibility

The selected model exposes raw classification and box-regression tensors.

AutoSense AI therefore owns:

1. Anchor generation.
2. Classification score processing.
3. Bounding-box decoding.
4. Confidence filtering.
5. Non-maximum suppression.
6. Conversion to the public `Detection` representation.
7. Mapping model coordinates back to the original camera frame.

These operations will be independently unit tested before end-to-end model execution.

## Runtime

The selected Android inference runtime is:

- LiteRT `2.2.0`
- Artifact: `com.google.ai.edge.litert:litert:2.2.0`
- Initial accelerator: CPU

The implementation will use the modern `CompiledModel` API.

Input and output buffers are intended to be created once and reused across inference calls to avoid unnecessary allocation in the camera processing path.

## Performance Metrics

The vision runtime will measure at least:

- Model inference latency.
- End-to-end vision latency.
- Frames processed.
- Frames dropped.
- CPU usage.
- Memory usage.

Performance optimization must not compromise bounded processing or frame ownership/lifecycle correctness.

## Model Selection Rationale

EfficientDet-Lite0 was selected for V1 because it provides a practical object-detection workload while remaining suitable for on-device execution.

The model also gives AutoSense AI a meaningful opportunity to demonstrate:

- Camera2 frame acquisition.
- YUV-to-RGB preprocessing.
- Quantized model input handling.
- LiteRT runtime integration.
- Anchor-based object detection post-processing.
- Bounding-box coordinate transformation.
- NMS.
- Performance measurement.

## Scope and Safety

AutoSense AI is a non-safety-critical engineering and demonstration project.

Vision results are treated as perception/context signals.

They must not directly trigger safety-critical vehicle control.

Any action supported by the project must pass through the deterministic intent, decision, validation, and action-execution layers.

## Model Provenance

The model artifact is retained with the project so that builds can be reproduced against a known binary.

The artifact checksum above should be revalidated if the model file is replaced.

Model version changes must be treated as explicit engineering changes and must include re-validation of:

- tensor metadata,
- preprocessing assumptions,
- post-processing behavior,
- detection quality,
- performance characteristics.

## Verification Status

Verified against the project-local model artifact:

- [x] Model file exists.
- [x] File size verified.
- [x] SHA-256 verified.
- [x] Input tensor metadata inspected.
- [x] Classification output metadata inspected.
- [x] Box regression output metadata inspected.
- [x] Kotlin model contract tests pass.
- [x] AutoSense AI module unit tests pass.
- [x] Android debug build passes.
- [ ] Actual LiteRT inference on Android device/emulator.
- [ ] End-to-end camera-to-detection validation.
- [ ] Performance benchmark on target hardware.

The final three items require an Android runtime/device and are intentionally deferred until the runtime implementation is complete.
