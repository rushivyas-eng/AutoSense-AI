package com.autosense.ai.api.action

import com.autosense.ai.api.intent.Intent
import com.autosense.ai.api.intent.IntentSource
import com.autosense.ai.api.intent.IntentType
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionInputTest {

    @Test
    fun decisionInput_preservesValues() {
        val intent = Intent(
            type = IntentType.TURN_ON_CABIN_LIGHT,
            confidence = 0.94f,
            timestampNanos = 100L,
            source = IntentSource.VOICE
        )

        val vehicleState = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 90L
        )

        val input = DecisionInput(
            intent = intent,
            vehicleState = vehicleState,
            timestampNanos = 110L
        )

        assertEquals(intent, input.intent)
        assertEquals(vehicleState, input.vehicleState)
        assertEquals(110L, input.timestampNanos)
    }
}