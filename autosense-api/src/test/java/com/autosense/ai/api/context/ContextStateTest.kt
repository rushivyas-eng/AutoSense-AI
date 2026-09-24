package com.autosense.ai.api.context

import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextStateTest {

    @Test
    fun contextState_preservesValues() {
        val vehicleState = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 100L
        )

        val contextState = ContextState(
            vehicleState = vehicleState,
            timestampNanos = 150L
        )

        assertEquals(vehicleState, contextState.vehicleState)
        assertEquals(150L, contextState.timestampNanos)
    }

    @Test
    fun contextState_supportsValueEquality() {
        val vehicleState = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 200L
        )

        val state1 = ContextState(
            vehicleState = vehicleState,
            timestampNanos = 250L
        )

        val state2 = ContextState(
            vehicleState = vehicleState,
            timestampNanos = 250L
        )

        assertEquals(state1, state2)
    }
}