package com.autosense.ai.vehicle.aaos

import android.car.hardware.property.CarPropertyManager
import android.car.hardware.CarPropertyValue

internal class CarPropertyManagerClient(
    private val carPropertyManager: CarPropertyManager
) : AaosPropertyClient {

    private val callbacks =
        mutableMapOf<Int, CarPropertyManager.CarPropertyEventCallback>()

    override fun subscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    ): Boolean {
        val androidCallback =
            object : CarPropertyManager.CarPropertyEventCallback {

                override fun onChangeEvent(
                    value: CarPropertyValue<*>
                ) {
                    callback.onChangeEvent(
                        AaosPropertyEvent(
                            propertyId = value.propertyId,
                            areaId = value.areaId,
                            status = value.status,
                            value = value.value
                        )
                    )
                }

                override fun onErrorEvent(
                    propertyId: Int,
                    areaId: Int
                ) {
                    callback.onErrorEvent(
                        propertyId,
                        areaId
                    )
                }
            }

        val subscribed = carPropertyManager.subscribePropertyEvents(
            propertyId,
            CarPropertyManager.SENSOR_RATE_ONCHANGE,
            androidCallback
        )

        if (subscribed) {
            callbacks[propertyId] = androidCallback
        }

        return subscribed
    }

    override fun unsubscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    ) {
        val androidCallback = callbacks.remove(propertyId)
            ?: return

        carPropertyManager.unsubscribePropertyEvents(
            propertyId,
            androidCallback
        )
    }
}