package com.autosense.ai.api.context

import com.autosense.ai.api.intent.Intent
import com.autosense.ai.api.intent.IntentSource
import com.autosense.ai.api.intent.IntentType
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vision.BoundingBox
import com.autosense.ai.api.vision.Detection
import com.autosense.ai.api.vision.VisionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContextInputTest {

    @Test
    fun contextInput_preservesAllSignals() {
        val vehicleState = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 100L
        )

        val visionResult = VisionResult(
            timestampNanos = 110L,
            inferenceTimeMs = 40L,
            detections = listOf(
                Detection(
                    label = "person",
                    confidence = 0.95f,
                    boundingBox = BoundingBox(
                        left = 0.1f,
                        top = 0.1f,
                        right = 0.5f,
                        bottom = 0.9f
                    )
                )
            )
        )

        val intent = Intent(
            type = IntentType.TURN_ON_CABIN_LIGHT,
            confidence = 0.92f,
            timestampNanos = 120L,
            source = IntentSource.VOICE
        )

        val input = ContextInput(
            vehicleState = vehicleState,
            visionResult = visionResult,
            intent = intent
        )

        assertEquals(vehicleState, input.vehicleState)
        assertEquals(visionResult, input.visionResult)
        assertEquals(intent, input.intent)
    }

    @Test
    fun contextInput_supportsMissingOptionalSignals() {
        val vehicleState = VehicleState(
            gear = Gear.DRIVE,
            ignitionState = IgnitionState.ON,
            timestampNanos = 200L
        )

        val input = ContextInput(
            vehicleState = vehicleState,
            visionResult = null,
            intent = null
        )

        assertEquals(vehicleState, input.vehicleState)
        assertNull(input.visionResult)
        assertNull(input.intent)
    }
}