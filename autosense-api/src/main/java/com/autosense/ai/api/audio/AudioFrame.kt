package com.autosense.ai.api.audio

class AudioFrame(
    val timestampNanos: Long,
    val sequenceNumber: Long,
    val sampleRateHz: Int,
    val channelCount: Int,
    val format: AudioFormat,
    val data: ByteArray
) {
}