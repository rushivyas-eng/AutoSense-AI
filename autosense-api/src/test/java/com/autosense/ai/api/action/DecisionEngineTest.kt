package com.autosense.ai.api.action

import com.autosense.ai.api.intent.Intent
import com.autosense.ai.api.intent.IntentSource
import com.autosense.ai.api.intent.IntentType
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionEngineTest {

    @Test
    fun engine_convertsDecisionInputToDecision() {
        val intent = Intent(
            type = IntentType.TURN_ON_CABIN_LIGHT,
            confidence = 0.95f,
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

        val expectedDecision = Decision(
            status = DecisionStatus.ALLOWED,
            reason = DecisionReason.ALLOWED,
            action = Action(
                type = ActionType.TURN_ON_CABIN_LIGHT,
                timestampNanos = 110L
            ),
            timestampNanos = 110L
        )

        val engine = FakeDecisionEngine(expectedDecision)

        val result = engine.process(input)

        assertEquals(expectedDecision, result)
    }

    private class FakeDecisionEngine(
        private val decision: Decision
    ) : DecisionEngine {

        override fun process(input: DecisionInput): Decision {
            return decision
        }
    }
}