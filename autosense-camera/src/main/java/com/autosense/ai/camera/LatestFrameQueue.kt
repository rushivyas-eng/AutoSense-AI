package com.autosense.ai.camera

import com.autosense.ai.api.camera.CameraFrame

internal class LatestFrameQueue : AutoCloseable {

    private val lock = Any()

    private var latestFrame: CameraFrame? = null

    fun offer(frame: CameraFrame) {
        val frameToClose: CameraFrame?

        synchronized(lock) {
            frameToClose = latestFrame
            latestFrame = frame
        }

        frameToClose?.close()
    }

    fun poll(): CameraFrame? {
        synchronized(lock) {
            val frame = latestFrame
            latestFrame = null
            return frame
        }
    }

    override fun close() {
        val frameToClose: CameraFrame?

        synchronized(lock) {
            frameToClose = latestFrame
            latestFrame = null
        }

        frameToClose?.close()
    }
}