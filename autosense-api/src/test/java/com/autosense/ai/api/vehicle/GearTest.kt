package com.autosense.ai.api.vehicle

import org.junit.Assert.assertEquals
import org.junit.Test

class GearTest {

    @Test
    fun gear_containsExpectedStates() {
        assertEquals(Gear.PARK, Gear.valueOf("PARK"))
        assertEquals(Gear.REVERSE, Gear.valueOf("REVERSE"))
        assertEquals(Gear.NEUTRAL, Gear.valueOf("NEUTRAL"))
        assertEquals(Gear.DRIVE, Gear.valueOf("DRIVE"))
        assertEquals(Gear.UNKNOWN, Gear.valueOf("UNKNOWN"))
    }
}