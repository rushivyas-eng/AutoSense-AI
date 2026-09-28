package com.autosense.ai.vehicle.mock

import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleAdapter
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vehicle.VehicleStateListener

class MockVehicleAdapter : VehicleAdapter {

    private val lock = Any()

    private var currentState = VehicleState(
        gear = Gear.UNKNOWN,
        ignitionState = IgnitionState.UNKNOWN,
        timestampNanos = System.nanoTime()
    )

    private val listeners = mutableSetOf<VehicleStateListener>()

    override fun getCurrentState(): VehicleState {
        synchronized(lock) {
            return currentState
        }
    }

    override fun registerListener(listener: VehicleStateListener) {
        synchronized(lock) {
            listeners.add(listener)
        }
    }

    override fun unregisterListener(listener: VehicleStateListener) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }

    fun setVehicleState(state: VehicleState) {
        val listenersSnapshot: List<VehicleStateListener>

        synchronized(lock) {
            currentState = state
            listenersSnapshot = listeners.toList()
        }

        listenersSnapshot.forEach { listener -> listener.onVehicleStateChanged(state) }
    }
}