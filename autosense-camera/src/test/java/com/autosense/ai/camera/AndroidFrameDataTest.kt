package com.autosense.ai.camera

import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import java.nio.ByteBuffer

class AndroidFrameDataTest {

    @Test
    fun format_isPreserved() {
        val frameData = AndroidFrameData(
            format = FrameFormat.YUV_420_888,
            planes = emptyList(),
            closeAction = {}
        )

        assertEquals(
            FrameFormat.YUV_420_888,
            frameData.format
        )
    }

    @Test
    fun planes_arePreserved() {
        val plane = FramePlane(
            buffer = ByteBuffer.allocate(4),
            rowStride = 2,
            pixelStride = 1
        )

        val frameData = AndroidFrameData(
            format = FrameFormat.YUV_420_888,
            planes = listOf(plane),
            closeAction = {}
        )

        assertEquals(
            listOf(plane),
            frameData.planes
        )
    }

    @Test
    fun close_invokesCloseAction() {
        var closeCount = 0

        val frameData = AndroidFrameData(
            format = FrameFormat.YUV_420_888,
            planes = emptyList(),
            closeAction = {
                closeCount++
            }
        )

        frameData.close()

        assertEquals(1, closeCount)
    }

    @Test
    fun close_isIdempotent() {
        var closeCount = 0

        val frameData = AndroidFrameData(
            format = FrameFormat.YUV_420_888,
            planes = emptyList(),
            closeAction = {
                closeCount++
            }
        )

        frameData.close()
        frameData.close()

        assertEquals(1, closeCount)
    }
}