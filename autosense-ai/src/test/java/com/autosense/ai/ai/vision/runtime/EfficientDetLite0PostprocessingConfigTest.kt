package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EfficientDetLite0PostprocessingConfigTest {

    @Test
    fun defaultConfiguration_matchesEfficientDetLite0() {
        val config = EfficientDetLite0PostprocessingConfig()

        assertEquals(320, config.imageWidth)
        assertEquals(320, config.imageHeight)

        assertEquals(3, config.minLevel)
        assertEquals(7, config.maxLevel)

        assertEquals(3, config.numScales)

        assertEquals(
            listOf(1.0f, 2.0f, 0.5f),
            config.aspectRatios
        )

        assertEquals(3.0f, config.anchorScale, 0f)

        assertEquals(90, config.numClasses)
        assertEquals(19206, config.numAnchors)

        assertEquals(100, config.maxDetections)
    }

    @Test
    fun defaultConfiguration_hasPositiveValues() {
        val config = EfficientDetLite0PostprocessingConfig()

        assertTrue(config.imageWidth > 0)
        assertTrue(config.imageHeight > 0)
        assertTrue(config.numScales > 0)
        assertTrue(config.anchorScale > 0f)
        assertTrue(config.numClasses > 0)
        assertTrue(config.numAnchors > 0)
        assertTrue(config.maxDetections > 0)
    }

    @Test
    fun defaultConfiguration_hasValidAspectRatios() {
        val config = EfficientDetLite0PostprocessingConfig()

        assertTrue(
            config.aspectRatios.all { it > 0f }
        )
    }
}