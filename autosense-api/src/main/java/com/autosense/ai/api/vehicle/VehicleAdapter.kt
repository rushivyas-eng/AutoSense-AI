package com.autosense.ai.api.vehicle

interface VehicleAdapter {

    fun getCurrentState(): VehicleState

    fun registerListener(listener: VehicleStateListener)

    fun  unregisterListener(listener: VehicleStateListener)
}