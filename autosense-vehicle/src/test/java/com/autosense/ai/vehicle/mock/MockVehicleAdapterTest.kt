package com.autosense.ai.vehicle.mock

import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vehicle.VehicleStateListener
import org.junit.Assert.assertEquals
import org.junit.Test

class MockVehicleAdapterTest {

    @Test
    fun initialState_isUnknown() {
        val adapter = MockVehicleAdapter()

        val state = adapter.getCurrentState()

        assertEquals(Gear.UNKNOWN, state.gear)
        assertEquals(IgnitionState.UNKNOWN, state.ignitionState)
    }

    @Test
    fun setVehicleState_updatesCurrentState() {
        val adapter = MockVehicleAdapter()

        val expected = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 100L
        )

        adapter.setVehicleState(expected)

        assertEquals(expected, adapter.getCurrentState())
    }

    @Test
    fun setVehicleState_notifiesRegisteredListener() {
        val adapter = MockVehicleAdapter()

        var receivedState: VehicleState? = null

        val listener = VehicleStateListener { state ->
            receivedState = state
        }

        adapter.registerListener(listener)

        val expected = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 200L
        )

        adapter.setVehicleState(expected)

        assertEquals(expected, receivedState)
    }

    @Test
    fun registerListener_doesNotImmediatelyNotify() {
        val adapter = MockVehicleAdapter()

        var callbackCount = 0

        val listener = VehicleStateListener {
            callbackCount++
        }

        adapter.registerListener(listener)

        assertEquals(0, callbackCount)
    }

    @Test
    fun unregisterListener_stopsNotifications() {
        val adapter = MockVehicleAdapter()

        var callbackCount = 0

        val listener = VehicleStateListener {
            callbackCount++
        }

        adapter.registerListener(listener)
        adapter.unregisterListener(listener)

        adapter.setVehicleState(
            VehicleState(
                gear = Gear.REVERSE,
                ignitionState = IgnitionState.ON,
                timestampNanos = 300L
            )
        )

        assertEquals(0, callbackCount)
    }

    @Test
    fun duplicateRegistration_doesNotDuplicateCallbacks() {
        val adapter = MockVehicleAdapter()

        var callbackCount = 0

        val listener = VehicleStateListener {
            callbackCount++
        }

        adapter.registerListener(listener)
        adapter.registerListener(listener)

        adapter.setVehicleState(
            VehicleState(
                gear = Gear.PARK,
                ignitionState = IgnitionState.ON,
                timestampNanos = 400L
            )
        )

        assertEquals(1, callbackCount)
    }
}