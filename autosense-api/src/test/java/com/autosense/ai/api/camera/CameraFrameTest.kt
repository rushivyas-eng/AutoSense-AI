package com.autosense.ai.api.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer

class CameraFrameTest {

    @Test
    fun close_closesFrameData() {
        val frameData = FakeFrameData()

        val frame = CameraFrame(
            timestampNanos = 1_000_000L,
            sequenceNumber = 42L,
            width = 1920,
            height = 1080,
            rotationDegrees = 90,
            data = frameData
        )

        frame.close()

        assertTrue(frameData.isClosed)
    }

    @Test
    fun metadata_isPreserved() {
        val frameData = FakeFrameData()

        val frame = CameraFrame(
            timestampNanos = 123_456_789L,
            sequenceNumber = 17L,
            width = 1280,
            height = 720,
            rotationDegrees = 270,
            data = frameData
        )

        assertEquals(123_456_789L, frame.timestampNanos)
        assertEquals(17L, frame.sequenceNumber)
        assertEquals(1280, frame.width)
        assertEquals(720, frame.height)
        assertEquals(270, frame.rotationDegrees)
    }

    @Test
    fun close_canBeCalledMoreThanOnce() {
        val frameData = FakeFrameData()

        val frame = CameraFrame(
            timestampNanos = 1L,
            sequenceNumber = 1L,
            width = 640,
            height = 480,
            rotationDegrees = 0,
            data = frameData
        )

        frame.close()
        frame.close()

        assertEquals(1, frameData.closeCount)
    }

    @Test
    fun framePlane_containsExpectedMetadata() {
        val buffer = ByteBuffer.allocate(16)

        val plane = FramePlane(
            buffer = buffer,
            rowStride = 8,
            pixelStride = 2
        )

        assertEquals(8, plane.rowStride)
        assertEquals(2, plane.pixelStride)
        assertEquals(buffer, plane.buffer)
    }

    private class FakeFrameData : FrameData {

        override val format: FrameFormat = FrameFormat.YUV_420_888

        override val planes: List<FramePlane> = listOf(
            FramePlane(
                buffer = ByteBuffer.allocate(16),
                rowStride = 4,
                pixelStride = 1
            )
        )

        var isClosed = false
            private set

        var closeCount = 0
            private set

        override fun close() {
            isClosed = true
            closeCount++
        }
    }
}