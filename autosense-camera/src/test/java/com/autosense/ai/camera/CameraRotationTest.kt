package com.autosense.ai.camera

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CameraRotationTest {

    @Test
    fun zeroDegrees_isUnchanged() {
        assertEquals(
            0,
            CameraRotation.normalize(0)
        )
    }

    @Test
    fun ninetyDegrees_isUnchanged() {
        assertEquals(
            90,
            CameraRotation.normalize(90)
        )
    }

    @Test
    fun oneEightyDegrees_isUnchanged() {
        assertEquals(
            180,
            CameraRotation.normalize(180)
        )
    }

    @Test
    fun twoSeventyDegrees_isUnchanged() {
        assertEquals(
            270,
            CameraRotation.normalize(270)
        )
    }

    @Test
    fun threeSixtyDegrees_wrapsToZero() {
        assertEquals(
            0,
            CameraRotation.normalize(360)
        )
    }

    @Test
    fun negativeNinetyDegrees_wrapsToTwoSeventy() {
        assertEquals(
            270,
            CameraRotation.normalize(-90)
        )
    }

    @Test
    fun invalidRotation_throws() {
        assertFailsWith<IllegalArgumentException> {
            CameraRotation.normalize(45)
        }
    }
}