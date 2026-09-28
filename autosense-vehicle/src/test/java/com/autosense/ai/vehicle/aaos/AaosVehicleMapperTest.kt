package com.autosense.ai.vehicle.aaos

import android.car.VehicleGear
import android.car.VehicleIgnitionState
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import org.junit.Assert.assertEquals
import org.junit.Test

class AaosVehicleMapperTest {

    @Test
    fun mapGear_park_returnsPark() {
        assertEquals(
            Gear.PARK,
            AaosVehicleMapper.mapGear(VehicleGear.GEAR_PARK)
        )
    }

    @Test
    fun mapGear_reverse_returnsReverse() {
        assertEquals(
            Gear.REVERSE,
            AaosVehicleMapper.mapGear(VehicleGear.GEAR_REVERSE)
        )
    }

    @Test
    fun mapGear_neutral_returnsNeutral() {
        assertEquals(
            Gear.NEUTRAL,
            AaosVehicleMapper.mapGear(VehicleGear.GEAR_NEUTRAL)
        )
    }

    @Test
    fun mapGear_drive_returnsDrive() {
        assertEquals(
            Gear.DRIVE,
            AaosVehicleMapper.mapGear(VehicleGear.GEAR_DRIVE)
        )
    }

    @Test
    fun mapGear_unsupportedValue_returnsUnknown() {
        assertEquals(
            Gear.UNKNOWN,
            AaosVehicleMapper.mapGear(Int.MAX_VALUE)
        )
    }

    @Test
    fun mapIgnitionState_off_returnsOff() {
        assertEquals(
            IgnitionState.OFF,
            AaosVehicleMapper.mapIgnitionState(VehicleIgnitionState.OFF)
        )
    }

    @Test
    fun mapIgnitionState_acc_returnsAcc() {
        assertEquals(
            IgnitionState.ACC,
            AaosVehicleMapper.mapIgnitionState(VehicleIgnitionState.ACC)
        )
    }

    @Test
    fun mapIgnitionState_on_returnsOn() {
        assertEquals(
            IgnitionState.ON,
            AaosVehicleMapper.mapIgnitionState(VehicleIgnitionState.ON)
        )
    }

    @Test
    fun mapIgnitionState_unsupportedValue_returnsUnknown() {
        assertEquals(
            IgnitionState.UNKNOWN,
            AaosVehicleMapper.mapIgnitionState(Int.MAX_VALUE)
        )
    }
}