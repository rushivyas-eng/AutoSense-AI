package com.autosense.ai.ai.vision.runtime

import org.junit.Assert.assertEquals
import org.junit.Test

class EfficientDetLite0LabelMapTest {

    @Test
    fun getLabel_mapsCommonObjectClasses() {
        assertEquals(
            "person",
            EfficientDetLite0LabelMap.getLabel(0)
        )

        assertEquals(
            "bicycle",
            EfficientDetLite0LabelMap.getLabel(1)
        )

        assertEquals(
            "car",
            EfficientDetLite0LabelMap.getLabel(2)
        )

        assertEquals(
            "motorcycle",
            EfficientDetLite0LabelMap.getLabel(3)
        )

        assertEquals(
            "bus",
            EfficientDetLite0LabelMap.getLabel(5)
        )

        assertEquals(
            "truck",
            EfficientDetLite0LabelMap.getLabel(7)
        )
    }

    @Test
    fun getLabel_preservesPlaceholderPositions() {
        assertEquals(
            "???",
            EfficientDetLite0LabelMap.getLabel(11)
        )

        assertEquals(
            "???",
            EfficientDetLite0LabelMap.getLabel(25)
        )

        assertEquals(
            "???",
            EfficientDetLite0LabelMap.getLabel(44)
        )
    }

    @Test
    fun getLabel_mapsLastClass() {
        assertEquals(
            "toothbrush",
            EfficientDetLite0LabelMap.getLabel(89)
        )
    }

    @Test
    fun getLabel_returnsFallbackForInvalidIndices() {
        assertEquals(
            "unknown_-1",
            EfficientDetLite0LabelMap.getLabel(-1)
        )

        assertEquals(
            "unknown_90",
            EfficientDetLite0LabelMap.getLabel(90)
        )
    }
}