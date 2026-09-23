package com.autosense.ai.api.camera

import java.nio.ByteBuffer

data class FramePlane(
    val buffer: ByteBuffer,
    val rowStride: Int,
    val pixelStride: Int
)
