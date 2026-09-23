package com.autosense.ai.api.vehicle

data class VehicleState(
    val gear: Gear,
    val ignitionState: IgnitionState,
    val timestampNanos: Long
)
