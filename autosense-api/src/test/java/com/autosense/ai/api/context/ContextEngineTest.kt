package com.autosense.ai.api.context

import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import org.junit.Assert.assertEquals
import org.junit.Test

class ContextEngineTest {

    @Test
    fun engine_convertsInputToContextState() {
        val vehicleState = VehicleState(
            gear = Gear.PARK,
            ignitionState = IgnitionState.ON,
            timestampNanos = 100L
        )

        val input = ContextInput(
            vehicleState = vehicleState,
            visionResult = null,
            intent = null
        )

        val expectedState = ContextState(
            vehicleState = vehicleState,
            timestampNanos = 150L
        )

        val engine = FakeContextEngine(expectedState)

        val result = engine.process(input)

        assertEquals(expectedState, result)
    }

    private class FakeContextEngine(
        private val contextState: ContextState
    ) : ContextEngine {

        override fun process(input: ContextInput): ContextState {
            return contextState
        }
    }
}