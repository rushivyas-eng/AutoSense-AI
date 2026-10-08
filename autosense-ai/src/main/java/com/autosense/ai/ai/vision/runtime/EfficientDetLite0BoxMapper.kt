package com.autosense.ai.ai.vision.runtime

import com.autosense.ai.ai.vision.preprocessing.VisionTransform
import com.autosense.ai.api.vision.BoundingBox
import kotlin.math.max
import kotlin.math.min

internal class EfficientDetLite0BoxMapper {

    fun map(
        box: FloatArray,
        transform: VisionTransform
    ): BoundingBox {
        require(box.size == 4) {
            "box must contain exactly 4 values"
        }

        require(box.all { it.isFinite() }) {
            "box must contain only finite values"
        }

        require(box[0] < box[2]) {
            "box must have positive height"
        }

        require(box[1] < box[3]) {
            "box must have positive width"
        }

        /*
         * EfficientDet box coordinates are expressed in the
         * model-input pixel coordinate system.
         *
         * For Lite0:
         *
         *     outputWidth  = 320
         *     outputHeight = 320
         *
         * The decoder therefore produces boxes such as:
         *
         *     [80, 60, 240, 260]
         *
         * rather than normalized [0..1] coordinates.
         */

        val modelTop = box[0]
        val modelLeft = box[1]
        val modelBottom = box[2]
        val modelRight = box[3]

        /*
         * Convert model-input coordinates into coordinates
         * within the cropped oriented image.
         *
         * We intentionally do not reject coordinates outside
         * the model frame. Bounding-box regression can produce
         * boxes extending beyond the image. They are clipped
         * after mapping into source coordinates.
         */
        val cropTop =
            modelTop /
                    transform.outputHeight.toFloat() *
                    transform.cropHeight

        val cropLeft =
            modelLeft /
                    transform.outputWidth.toFloat() *
                    transform.cropWidth

        val cropBottom =
            modelBottom /
                    transform.outputHeight.toFloat() *
                    transform.cropHeight

        val cropRight =
            modelRight /
                    transform.outputWidth.toFloat() *
                    transform.cropWidth

        /*
         * Move from crop-local coordinates into the
         * oriented-image coordinate system.
         */
        val orientedTop =
            transform.cropTop + cropTop

        val orientedLeft =
            transform.cropLeft + cropLeft

        val orientedBottom =
            transform.cropTop + cropBottom

        val orientedRight =
            transform.cropLeft + cropRight

        /*
         * Transform all four corners from the oriented image
         * back into the original camera image.
         *
         * Using all four corners makes the 90/270 degree
         * axis transformation explicit and avoids assumptions
         * about width/height ordering.
         */
        val topLeft =
            inverseRotate(
                x = orientedLeft,
                y = orientedTop,
                transform = transform
            )

        val topRight =
            inverseRotate(
                x = orientedRight,
                y = orientedTop,
                transform = transform
            )

        val bottomLeft =
            inverseRotate(
                x = orientedLeft,
                y = orientedBottom,
                transform = transform
            )

        val bottomRight =
            inverseRotate(
                x = orientedRight,
                y = orientedBottom,
                transform = transform
            )

        val sourceLeft =
            min(
                min(topLeft.first, topRight.first),
                min(bottomLeft.first, bottomRight.first)
            )

        val sourceRight =
            max(
                max(topLeft.first, topRight.first),
                max(bottomLeft.first, bottomRight.first)
            )

        val sourceTop =
            min(
                min(topLeft.second, topRight.second),
                min(bottomLeft.second, bottomRight.second)
            )

        val sourceBottom =
            max(
                max(topLeft.second, topRight.second),
                max(bottomLeft.second, bottomRight.second)
            )

        return BoundingBox(
            left = normalizeAndClamp(
                sourceLeft,
                transform.sourceWidth
            ),
            top = normalizeAndClamp(
                sourceTop,
                transform.sourceHeight
            ),
            right = normalizeAndClamp(
                sourceRight,
                transform.sourceWidth
            ),
            bottom = normalizeAndClamp(
                sourceBottom,
                transform.sourceHeight
            )
        )
    }

    private fun inverseRotate(
        x: Float,
        y: Float,
        transform: VisionTransform
    ): Pair<Float, Float> {
        return when (transform.rotationDegrees) {
            0 -> {
                x to y
            }

            /*
             * Clockwise 90-degree rotation:
             *
             * oriented:
             *     width  = sourceHeight
             *     height = sourceWidth
             *
             * Inverse:
             *     sourceX = orientedY
             *     sourceY = sourceHeight - orientedX
             */
            90 -> {
                y to
                        (transform.sourceHeight - x)
            }

            180 -> {
                (transform.sourceWidth - x) to
                        (transform.sourceHeight - y)
            }

            /*
             * Clockwise 270-degree rotation:
             * equivalent to counter-clockwise 90 degrees.
             *
             * Inverse:
             *     sourceX = sourceWidth - orientedY
             *     sourceY = orientedX
             */
            270 -> {
                (transform.sourceWidth - y) to x
            }

            else -> {
                error(
                    "Unsupported rotation: " +
                            transform.rotationDegrees
                )
            }
        }
    }

    private fun normalizeAndClamp(
        coordinate: Float,
        dimension: Int
    ): Float {
        return (
                coordinate / dimension.toFloat()
                ).coerceIn(0.0f, 1.0f)
    }
}