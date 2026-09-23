package com.autosense.ai.api.camera

import java.lang.AutoCloseable

class CameraFrame(
    val timestampNanos: Long,
    val sequenceNumber: Long,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val data: FrameData
) : AutoCloseable {

    private var closed = false

    override fun close() {
        if (closed) {
            return
        }

        closed = true
        data.close()
    }
}