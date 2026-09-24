package com.autosense.ai.api.context

import com.autosense.ai.api.vehicle.VehicleState

data class ContextState(
    val vehicleState: VehicleState,
    val timestampNanos: Long
)
