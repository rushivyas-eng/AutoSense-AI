package com.autosense.ai.camera

import com.autosense.ai.api.camera.CameraFrame
import com.autosense.ai.api.camera.FrameData
import com.autosense.ai.api.camera.FrameFormat
import com.autosense.ai.api.camera.FramePlane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.ByteBuffer

class LatestFrameQueueTest {

    @Test
    fun offer_thenPoll_returnsFrame() {
        val queue = LatestFrameQueue()
        val frame = createFrame(1)

        queue.offer(frame)

        assertEquals(
            frame,
            queue.poll()
        )
    }

    @Test
    fun poll_emptyQueue_returnsNull() {
        val queue = LatestFrameQueue()

        assertNull(queue.poll())
    }

    @Test
    fun offer_newFrame_closesPreviousFrame() {
        val queue = LatestFrameQueue()

        val firstFrameData = TestFrameData()
        val firstFrame = createFrame(
            sequenceNumber = 1,
            frameData = firstFrameData
        )

        val secondFrame = createFrame(2)

        queue.offer(firstFrame)
        queue.offer(secondFrame)

        assertEquals(
            1,
            firstFrameData.closeCount
        )

        assertEquals(
            secondFrame,
            queue.poll()
        )
    }

    @Test
    fun poll_removesFrameFromQueue() {
        val queue = LatestFrameQueue()
        val frame = createFrame(1)

        queue.offer(frame)

        assertEquals(
            frame,
            queue.poll()
        )

        assertNull(
            queue.poll()
        )
    }

    @Test
    fun close_closesQueuedFrame() {
        val queue = LatestFrameQueue()

        val frameData = TestFrameData()
        val frame = createFrame(
            sequenceNumber = 1,
            frameData = frameData
        )

        queue.offer(frame)

        queue.close()

        assertEquals(
            1,
            frameData.closeCount
        )

        assertNull(
            queue.poll()
        )
    }

    @Test
    fun close_emptyQueue_doesNothing() {
        val queue = LatestFrameQueue()

        queue.close()

        assertNull(
            queue.poll()
        )
    }

    private fun createFrame(
        sequenceNumber: Long,
        frameData: FrameData = TestFrameData()
    ): CameraFrame {
        return CameraFrame(
            timestampNanos = sequenceNumber,
            sequenceNumber = sequenceNumber,
            width = 1280,
            height = 720,
            rotationDegrees = 0,
            data = frameData
        )
    }

    private class TestFrameData : FrameData {

        var closeCount = 0

        override val format: FrameFormat =
            FrameFormat.YUV_420_888

        override val planes: List<FramePlane> =
            emptyList()

        override fun close() {
            closeCount++
        }
    }
}