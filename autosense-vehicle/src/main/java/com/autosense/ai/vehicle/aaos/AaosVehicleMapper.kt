package com.autosense.ai.vehicle.aaos

import android.car.VehicleGear
import android.car.VehicleIgnitionState
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState

internal object AaosVehicleMapper {

    fun mapGear(aaosGear: Int): Gear {
        return when (aaosGear) {
            VehicleGear.GEAR_PARK -> Gear.PARK
            VehicleGear.GEAR_REVERSE -> Gear.REVERSE
            VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
            VehicleGear.GEAR_DRIVE -> Gear.DRIVE
            else -> Gear.UNKNOWN
        }
    }

    fun mapIgnitionState(aaosIgnitionState: Int): IgnitionState {
        return when (aaosIgnitionState) {
            VehicleIgnitionState.OFF -> IgnitionState.OFF
            VehicleIgnitionState.ACC -> IgnitionState.ACC
            VehicleIgnitionState.ON -> IgnitionState.ON
            else -> IgnitionState.UNKNOWN
        }
    }
}