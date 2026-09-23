package com.autosense.ai.api.vehicle

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleStateTest {

    @Test
    fun vehicleState_preservesValues() {
        val state = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 123_456_789L
        )

        assertEquals(Gear.DRIVE, state.gear)
        assertEquals(IgnitionState.ON, state.ignitionState)
        assertEquals(123_456_789L, state.timestampNanos)
    }

    @Test
    fun vehicleState_supportsUnknownValues() {
        val state = VehicleState(
            gear = Gear.UNKNOWN,
            ignitionState = IgnitionState.UNKNOWN,
            timestampNanos = 100L
        )

        assertEquals(Gear.UNKNOWN, state.gear)
        assertEquals(IgnitionState.UNKNOWN, state.ignitionState)
        assertEquals(100L, state.timestampNanos)
    }

    @Test
    fun vehicleState_supportsValueEquality() {
        val state1 = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.OFF,
            timestampNanos = 500L
        )

        val state2 = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.OFF,
            timestampNanos = 500L
        )

        assertEquals(state1, state2)
    }
}