package com.autosense.ai.vehicle.aaos

import android.car.hardware.CarPropertyValue

internal class FakeAaosPropertyClient : AaosPropertyClient {

    data class Subscription(
        val propertyId: Int,
        val callback: AaosPropertyEventCallback
    )

    private val subscriptionList =
        mutableListOf<Subscription>()

    var subscribeResult: Boolean = true

    val subscribeResults = mutableListOf<Boolean>()

    fun subscriptions(): List<Subscription> {
        return subscriptionList.toList()
    }

    override fun subscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    ): Boolean {
        val result = if (subscribeResults.isNotEmpty()) {
            subscribeResults.removeAt(0)
        } else {
            subscribeResult
        }

        if (!result) {
            return false
        }

        subscriptionList.add(
            Subscription(
                propertyId = propertyId,
                callback = callback
            )
        )

        return true
    }

    override fun unsubscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    ) {
        subscriptionList.removeAll {
            it.propertyId == propertyId &&
                    it.callback === callback
        }
    }

    fun emitChange(
        propertyId: Int,
        value: Any?,
        status: Int = CarPropertyValue.STATUS_AVAILABLE
    ) {
        val event = AaosPropertyEvent(
            propertyId = propertyId,
            areaId = 0,
            status = status,
            value = value
        )

        subscriptionList
            .filter { it.propertyId == propertyId }
            .forEach { subscription ->
                subscription.callback.onChangeEvent(event)
            }
    }

    fun emitError(
        propertyId: Int
    ) {
        subscriptionList
            .filter { it.propertyId == propertyId }
            .forEach { subscription ->
                subscription.callback.onErrorEvent(
                    propertyId,
                    0
                )
            }
    }
}