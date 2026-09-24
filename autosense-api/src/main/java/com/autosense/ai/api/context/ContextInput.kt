package com.autosense.ai.api.context

import com.autosense.ai.api.intent.Intent
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vision.VisionResult

data class ContextInput(
    val vehicleState: VehicleState,
    val visionResult: VisionResult?,
    val intent: Intent?
)
