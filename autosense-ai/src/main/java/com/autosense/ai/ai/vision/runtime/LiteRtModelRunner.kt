package com.autosense.ai.ai.vision.runtime

internal interface LiteRtModelRunner : AutoCloseable {

    fun run(input: ByteArray): EfficientDetLite0RawOutput
}