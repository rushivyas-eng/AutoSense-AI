package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow
import kotlin.math.sqrt

class EfficientDetLite0AnchorGeneratorTest {

    private val generator = EfficientDetLite0AnchorGenerator()

    @Test
    fun generate_producesExpectedNumberOfAnchors() {
        val anchors = generator.generate()

        assertEquals(
            EfficientDetLite0ModelContract.CLASSIFICATION_OUTPUT_ANCHORS * 4,
            anchors.size
        )
    }

    @Test
    fun generate_producesExpectedFeatureMapSizes() {
        val config = EfficientDetLite0PostprocessingConfig()

        assertEquals(
            40,
            featureMapSize(config.imageWidth, 3)
        )

        assertEquals(
            20,
            featureMapSize(config.imageWidth, 4)
        )

        assertEquals(
            10,
            featureMapSize(config.imageWidth, 5)
        )

        assertEquals(
            5,
            featureMapSize(config.imageWidth, 6)
        )

        assertEquals(
            3,
            featureMapSize(config.imageWidth, 7)
        )
    }

    @Test
    fun generate_producesExpectedAnchorCountPerLevel() {
        val config = EfficientDetLite0PostprocessingConfig()

        val anchorsPerLocation =
            config.numScales * config.aspectRatios.size

        assertEquals(
            40 * 40 * anchorsPerLocation,
            40 * 40 * 9
        )

        assertEquals(
            20 * 20 * anchorsPerLocation,
            20 * 20 * 9
        )

        assertEquals(
            10 * 10 * anchorsPerLocation,
            10 * 10 * 9
        )

        assertEquals(
            5 * 5 * anchorsPerLocation,
            5 * 5 * 9
        )

        assertEquals(
            3 * 3 * anchorsPerLocation,
            3 * 3 * 9
        )
    }

    @Test
    fun generate_firstAnchorHasExpectedGeometry() {
        val anchors = generator.generate()

        val stride = 8.0f
        val center = stride / 2.0f

        val baseSize = 3.0f * stride
        val halfSize = baseSize / 2.0f

        assertEquals(
            center - halfSize,
            anchors[0],
            EPSILON
        )

        assertEquals(
            center - halfSize,
            anchors[1],
            EPSILON
        )

        assertEquals(
            center + halfSize,
            anchors[2],
            EPSILON
        )

        assertEquals(
            center + halfSize,
            anchors[3],
            EPSILON
        )
    }

    @Test
    fun generate_gridOrderingMatchesEfficientDet() {
        val anchors = generator.generate()

        /*
         * First configuration:
         *
         * P3
         * scale = 0
         * aspect = 1.0
         *
         * The complete 40x40 grid is emitted before
         * moving to the next aspect ratio.
         */

        val anchorsPerGrid =
            40 * 40

        val firstAnchorOffset = 0

        val secondGridAnchorOffset =
            1 * VALUES_PER_ANCHOR

        val nextAspectAnchorOffset =
            anchorsPerGrid * VALUES_PER_ANCHOR

        val firstCenterX =
            centerX(anchors, firstAnchorOffset)

        val secondCenterX =
            centerX(anchors, secondGridAnchorOffset)

        val nextAspectCenterX =
            centerX(anchors, nextAspectAnchorOffset)

        assertEquals(
            4.0f,
            firstCenterX,
            EPSILON
        )

        assertEquals(
            12.0f,
            secondCenterX,
            EPSILON
        )

        /*
         * Moving from aspect ratio 1.0 to 2.0 should
         * keep the same first grid-cell center.
         */
        assertEquals(
            4.0f,
            nextAspectCenterX,
            EPSILON
        )
    }

    @Test
    fun generate_scaleOrderingMatchesEfficientDet() {
        val anchors = generator.generate()

        val anchorsPerGrid =
            40 * 40

        val anchorsPerConfiguration =
            anchorsPerGrid

        val firstScaleOffset =
            0

        val secondScaleOffset =
            3 * anchorsPerConfiguration * VALUES_PER_ANCHOR

        /*
         * scale 0:
         *   aspect 1.0
         *   aspect 2.0
         *   aspect 0.5
         *
         * scale 1 starts after 3 configurations.
         */

        val firstWidth =
            width(anchors, firstScaleOffset)

        val secondScaleWidth =
            width(anchors, secondScaleOffset)

        val expectedScale =
            2.0.pow(1.0 / 3.0).toFloat()

        assertEquals(
            firstWidth * expectedScale,
            secondScaleWidth,
            EPSILON
        )
    }

    @Test
    fun generate_p7UsesActualFeatureMapStride() {
        val anchors = generator.generate()

        val p7StartAnchor =
            (
                    40 * 40 +
                            20 * 20 +
                            10 * 10 +
                            5 * 5
                    ) * 9

        val offset =
            p7StartAnchor * VALUES_PER_ANCHOR

        val expectedStride =
            320.0f / 3.0f

        val expectedCenter =
            expectedStride / 2.0f

        assertEquals(
            expectedCenter,
            centerX(anchors, offset),
            EPSILON
        )

        assertEquals(
            expectedCenter,
            centerY(anchors, offset),
            EPSILON
        )
    }

    @Test
    fun generate_p7GridHasThreeByThreeLocations() {
        val anchors = generator.generate()

        val p7StartAnchor =
            (
                    40 * 40 +
                            20 * 20 +
                            10 * 10 +
                            5 * 5
                    ) * 9

        val offset =
            p7StartAnchor * VALUES_PER_ANCHOR

        val stride =
            320.0f / 3.0f

        val firstCenter =
            stride / 2.0f

        val secondCenter =
            firstCenter + stride

        val thirdGridCellOffset =
            2 * VALUES_PER_ANCHOR

        val thirdCenter =
            centerX(
                anchors,
                offset + thirdGridCellOffset
            )

        assertEquals(
            firstCenter,
            centerX(anchors, offset),
            EPSILON
        )

        assertEquals(
            secondCenter,
            centerX(
                anchors,
                offset + VALUES_PER_ANCHOR
            ),
            EPSILON
        )

        assertEquals(
            thirdCenter,
            firstCenter + 2.0f * stride,
            EPSILON
        )
    }

    @Test
    fun generate_containsNoUnexpectedEmptyOutput() {
        val anchors = generator.generate()

        assertTrue(anchors.isNotEmpty())
    }

    private fun centerX(
        anchors: FloatArray,
        offset: Int
    ): Float {
        return (
                anchors[offset + 1] +
                        anchors[offset + 3]
                ) / 2.0f
    }

    private fun centerY(
        anchors: FloatArray,
        offset: Int
    ): Float {
        return (
                anchors[offset] +
                        anchors[offset + 2]
                ) / 2.0f
    }

    private fun width(
        anchors: FloatArray,
        offset: Int
    ): Float {
        return anchors[offset + 3] -
                anchors[offset + 1]
    }

    companion object {
        private const val VALUES_PER_ANCHOR = 4
        private const val EPSILON = 0.0001f

        private fun featureMapSize(
            imageSize: Int,
            level: Int
        ): Int {
            val stride = 1 shl level
            return (imageSize + stride - 1) / stride
        }
    }
}