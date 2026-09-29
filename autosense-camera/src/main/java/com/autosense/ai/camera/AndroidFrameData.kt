package com.autosense.ai.camera

import com.autosense.ai.api.camera.FrameData
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane

internal class AndroidFrameData(
    override val format: FrameFormat,
    override val planes: List<FramePlane>,
    private val closeAction: () -> Unit
) : FrameData {

    private var closed = false

    override fun close() {
        if (closed) {
            return
        }

        closed = true
        closeAction()
    }
}