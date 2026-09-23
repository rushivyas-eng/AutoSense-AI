package com.autosense.ai.api.camera

interface FrameData : AutoCloseable {
    val format: FrameFormat
    val planes: List<FramePlane>
    override fun close()
}