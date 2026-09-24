package com.autosense.ai.api.action

import com.autosense.ai.api.intent.Intent
import com.autosense.ai.api.vehicle.VehicleState

data class DecisionInput(
    val intent: Intent,
    val vehicleState: VehicleState,
    val timestampNanos: Long
)
