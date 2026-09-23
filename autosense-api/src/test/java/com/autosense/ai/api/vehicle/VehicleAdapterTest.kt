package com.autosense.ai.api.vehicle

import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleAdapterTest {

    @Test
    fun adapter_returnsCurrentState() {
        val expectedState = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 123_456_789L
        )

        val adapter = FakeVehicleAdapter(expectedState)

        assertEquals(expectedState, adapter.getCurrentState())
    }

    @Test
    fun adapter_notifiesRegisteredListener() {
        val state = VehicleState(
            gear = Gear.REVERSE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 200L
        )

        val adapter = FakeVehicleAdapter(state)

        var receivedState: VehicleState? = null

        val listener = VehicleStateListener {
            receivedState = it
        }

        adapter.registerListener(listener)
        adapter.notifyStateChanged(state)

        assertEquals(state, receivedState)
    }

    @Test
    fun adapter_stopsNotifyingUnregisteredListener() {
        val state = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 300L
        )

        val adapter = FakeVehicleAdapter(state)

        var callbackCount = 0

        val listener = VehicleStateListener {
            callbackCount++
        }

        adapter.registerListener(listener)
        adapter.unregisterListener(listener)
        adapter.notifyStateChanged(state)

        assertEquals(0, callbackCount)
    }

    private class FakeVehicleAdapter(
        private var currentState: VehicleState
    ) : VehicleAdapter {

        private val listeners = mutableSetOf<VehicleStateListener>()

        override fun getCurrentState(): VehicleState {
            return currentState
        }

        override fun registerListener(listener: VehicleStateListener) {
            listeners.add(listener)
        }

        override fun unregisterListener(listener: VehicleStateListener) {
            listeners.remove(listener)
        }

        fun notifyStateChanged(state: VehicleState) {
            currentState = state
            listeners.forEach { it.onVehicleStateChanged(state) }
        }
    }
}