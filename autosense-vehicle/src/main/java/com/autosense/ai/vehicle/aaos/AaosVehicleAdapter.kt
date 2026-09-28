package com.autosense.ai.vehicle.aaos

import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import com.autosense.ai.api.vehicle.Gear
import com.autosense.ai.api.vehicle.IgnitionState
import com.autosense.ai.api.vehicle.VehicleAdapter
import com.autosense.ai.api.vehicle.VehicleState
import com.autosense.ai.api.vehicle.VehicleStateListener

class AaosVehicleAdapter private constructor(
    private val propertyClient: AaosPropertyClient
) : VehicleAdapter {

    constructor(
        carPropertyManager: CarPropertyManager
    ) : this(
        CarPropertyManagerClient(carPropertyManager)
    )

    internal constructor(
        propertyClient: AaosPropertyClient,
        testOnly: Boolean
    ) : this(propertyClient)

    private val lock = Any()

    private var currentGear = Gear.UNKNOWN
    private var currentIgnitionState = IgnitionState.UNKNOWN

    private val listeners = mutableSetOf<VehicleStateListener>()

    private val propertyEventCallback =
        object : AaosPropertyEventCallback {

            override fun onChangeEvent(
                event: AaosPropertyEvent
            ) {
                handlePropertyChange(event)
            }

            override fun onErrorEvent(
                propertyId: Int,
                areaId: Int
            ) {
                handlePropertyError(propertyId)
            }
        }

    override fun getCurrentState(): VehicleState {
        synchronized(lock) {
            return createVehicleState()
        }
    }

    override fun registerListener(
        listener: VehicleStateListener
    ) {
        synchronized(lock) {
            listeners.add(listener)
        }
    }

    override fun unregisterListener(
        listener: VehicleStateListener
    ) {
        synchronized(lock) {
            listeners.remove(listener)
        }
    }

    fun start() {
        val gearSubscribed = propertyClient.subscribe(
            VehiclePropertyIds.GEAR_SELECTION,
            propertyEventCallback
        )

        val ignitionSubscribed = propertyClient.subscribe(
            VehiclePropertyIds.IGNITION_STATE,
            propertyEventCallback
        )

        if (!gearSubscribed || !ignitionSubscribed) {

            if (gearSubscribed) {
                propertyClient.unsubscribe(
                    VehiclePropertyIds.GEAR_SELECTION,
                    propertyEventCallback
                )
            }

            if (ignitionSubscribed) {
                propertyClient.unsubscribe(
                    VehiclePropertyIds.IGNITION_STATE,
                    propertyEventCallback
                )
            }

            throw IllegalStateException(
                "Failed to subscribe to required vehicle properties"
            )
        }
    }

    fun release() {
        propertyClient.unsubscribe(
            VehiclePropertyIds.GEAR_SELECTION,
            propertyEventCallback
        )

        propertyClient.unsubscribe(
            VehiclePropertyIds.IGNITION_STATE,
            propertyEventCallback
        )
    }

    private fun handlePropertyChange(
        event: AaosPropertyEvent
    ) {
        if (event.status != CarPropertyValue.STATUS_AVAILABLE) {
            handlePropertyError(event.propertyId)
            return
        }

        val rawValue = event.value

        if (rawValue !is Int) {
            handlePropertyError(event.propertyId)
            return
        }

        val listenersSnapshot: List<VehicleStateListener>
        val state: VehicleState

        synchronized(lock) {
            when (event.propertyId) {

                VehiclePropertyIds.GEAR_SELECTION -> {
                    currentGear =
                        AaosVehicleMapper.mapGear(rawValue)
                }

                VehiclePropertyIds.IGNITION_STATE -> {
                    currentIgnitionState =
                        AaosVehicleMapper.mapIgnitionState(rawValue)
                }

                else -> {
                    return
                }
            }

            state = createVehicleState()
            listenersSnapshot = listeners.toList()
        }

        listenersSnapshot.forEach { listener ->
            listener.onVehicleStateChanged(state)
        }
    }

    private fun handlePropertyError(
        propertyId: Int
    ) {
        val listenersSnapshot: List<VehicleStateListener>
        val state: VehicleState

        synchronized(lock) {
            when (propertyId) {

                VehiclePropertyIds.GEAR_SELECTION -> {
                    currentGear = Gear.UNKNOWN
                }

                VehiclePropertyIds.IGNITION_STATE -> {
                    currentIgnitionState = IgnitionState.UNKNOWN
                }

                else -> {
                    return
                }
            }

            state = createVehicleState()
            listenersSnapshot = listeners.toList()
        }

        listenersSnapshot.forEach { listener ->
            listener.onVehicleStateChanged(state)
        }
    }

    private fun createVehicleState(): VehicleState {
        return VehicleState(
            gear = currentGear,
            ignitionState = currentIgnitionState,
            timestampNanos = System.nanoTime()
        )
    }
}