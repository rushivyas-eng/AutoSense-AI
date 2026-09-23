package com.autosense.ai.api.vehicle

fun interface VehicleStateListener {

    fun onVehicleStateChanged(state: VehicleState)
}