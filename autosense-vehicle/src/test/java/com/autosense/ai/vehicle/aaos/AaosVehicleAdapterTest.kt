package com.autosense.ai.vehicle.aaos

import android.car.VehicleGear
import android.car.VehicleIgnitionState
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vehicle.VehicleStateListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AaosVehicleAdapterTest {

    @Test
    fun initialState_isUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        val state = adapter.getCurrentState()

        assertEquals(Gear.UNKNOWN, state.gear)
        assertEquals(IgnitionState.UNKNOWN, state.ignitionState)
    }

    @Test
    fun start_subscribesToRequiredProperties() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        val subscriptions = propertyClient.subscriptions()

        assertEquals(2, subscriptions.size)

        assertTrue(
            subscriptions.any {
                it.propertyId == VehiclePropertyIds.GEAR_SELECTION
            }
        )

        assertTrue(
            subscriptions.any {
                it.propertyId == VehiclePropertyIds.IGNITION_STATE
            }
        )
    }

    @Test
    fun gearChange_updatesVehicleState() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        val state = adapter.getCurrentState()

        assertEquals(Gear.DRIVE, state.gear)
        assertEquals(
            IgnitionState.UNKNOWN,
            state.ignitionState
        )
    }

    @Test
    fun ignitionChange_updatesVehicleState() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.IGNITION_STATE,
            VehicleIgnitionState.ON
        )

        val state = adapter.getCurrentState()

        assertEquals(
            Gear.UNKNOWN,
            state.gear
        )

        assertEquals(
            IgnitionState.ON,
            state.ignitionState
        )
    }

    @Test
    fun independentPropertyChanges_areMergedIntoSingleState() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        propertyClient.emitChange(
            VehiclePropertyIds.IGNITION_STATE,
            VehicleIgnitionState.ON
        )

        val state = adapter.getCurrentState()

        assertEquals(Gear.DRIVE, state.gear)
        assertEquals(IgnitionState.ON, state.ignitionState)
    }

    @Test
    fun listener_receivesVehicleStateChanges() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        val receivedStates = mutableListOf<VehicleState>()

        val listener = VehicleStateListener { state ->
            receivedStates.add(state)
        }

        adapter.registerListener(listener)
        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_REVERSE
        )

        assertEquals(1, receivedStates.size)
        assertEquals(
            Gear.REVERSE,
            receivedStates[0].gear
        )
    }

    @Test
    fun duplicateListenerRegistration_doesNotDuplicateCallbacks() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        val receivedStates = mutableListOf<VehicleState>()

        val listener = VehicleStateListener { state ->
            receivedStates.add(state)
        }

        adapter.registerListener(listener)
        adapter.registerListener(listener)

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        assertEquals(1, receivedStates.size)
    }

    @Test
    fun unregisterListener_stopsCallbacks() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        val receivedStates = mutableListOf<VehicleState>()

        val listener = VehicleStateListener { state ->
            receivedStates.add(state)
        }

        adapter.registerListener(listener)
        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        adapter.unregisterListener(listener)

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_REVERSE
        )

        assertEquals(1, receivedStates.size)
        assertEquals(
            Gear.DRIVE,
            receivedStates[0].gear
        )
    }

    @Test
    fun unavailableGear_setsUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            0,
            CarPropertyValue.STATUS_UNAVAILABLE
        )

        assertEquals(
            Gear.UNKNOWN,
            adapter.getCurrentState().gear
        )
    }

    @Test
    fun unavailableIgnition_setsUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.IGNITION_STATE,
            VehicleIgnitionState.ON
        )

        propertyClient.emitChange(
            VehiclePropertyIds.IGNITION_STATE,
            0,
            CarPropertyValue.STATUS_UNAVAILABLE
        )

        assertEquals(
            IgnitionState.UNKNOWN,
            adapter.getCurrentState().ignitionState
        )
    }

    @Test
    fun errorEvent_setsGearUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        propertyClient.emitError(
            VehiclePropertyIds.GEAR_SELECTION
        )

        assertEquals(
            Gear.UNKNOWN,
            adapter.getCurrentState().gear
        )
    }

    @Test
    fun errorEvent_setsIgnitionUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.IGNITION_STATE,
            VehicleIgnitionState.ON
        )

        propertyClient.emitError(
            VehiclePropertyIds.IGNITION_STATE
        )

        assertEquals(
            IgnitionState.UNKNOWN,
            adapter.getCurrentState().ignitionState
        )
    }

    @Test
    fun nonIntegerPropertyValue_setsGearUnknown() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            "INVALID"
        )

        assertEquals(
            Gear.UNKNOWN,
            adapter.getCurrentState().gear
        )
    }

    @Test
    fun failedSubscription_throwsException() {
        val propertyClient = FakeAaosPropertyClient()
        propertyClient.subscribeResult = false

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        assertThrows(IllegalStateException::class.java) {
            adapter.start()
        }

        assertTrue(
            propertyClient.subscriptions().isEmpty()
        )
    }

    @Test
    fun secondSubscriptionFailure_rollsBackFirstSubscription() {
        val propertyClient = FakeAaosPropertyClient()

        propertyClient.subscribeResults.add(true)
        propertyClient.subscribeResults.add(false)

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        assertThrows(IllegalStateException::class.java) {
            adapter.start()
        }

        assertTrue(
            propertyClient.subscriptions().isEmpty()
        )
    }

    @Test
    fun release_unsubscribesFromRequiredProperties() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        assertEquals(
            2,
            propertyClient.subscriptions().size
        )

        adapter.release()

        assertTrue(
            propertyClient.subscriptions().isEmpty()
        )
    }

    @Test
    fun release_stopsFutureVehicleUpdates() {
        val propertyClient = FakeAaosPropertyClient()

        val adapter = AaosVehicleAdapter(
            propertyClient,
            true
        )

        adapter.start()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_DRIVE
        )

        adapter.release()

        propertyClient.emitChange(
            VehiclePropertyIds.GEAR_SELECTION,
            VehicleGear.GEAR_REVERSE
        )

        assertEquals(
            Gear.DRIVE,
            adapter.getCurrentState().gear
        )
    }
}