package com.autosense.ai.vehicle.aaos

internal data class AaosPropertyEvent(
    val propertyId: Int,
    val areaId: Int,
    val status: Int,
    val value: Any?
)

internal interface AaosPropertyEventCallback {

    fun onChangeEvent(
        event: AaosPropertyEvent
    )

    fun onErrorEvent(
        propertyId: Int,
        areaId: Int
    )
}

internal interface AaosPropertyClient {

    fun subscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    ): Boolean

    fun unsubscribe(
        propertyId: Int,
        callback: AaosPropertyEventCallback
    )
}