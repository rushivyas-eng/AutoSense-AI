package com.autosense.ai.api.vehicle

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleStateListenerTest {

    @Test
    fun listener_receivesVehicleState() {
        var receivedState: VehicleState? = null

        val listener = VehicleStateListener { state ->
            receivedState = state
        }

        val state = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 123_456_789L
        )

        listener.onVehicleStateChanged(state)

        assertEquals(state, receivedState)
    }
}