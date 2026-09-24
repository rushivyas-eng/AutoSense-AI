package com.autosense.ai.api.voice

import com.autosense.ai.api.audio.AudioFrame

interface VoiceEngine {

    fun process(frame: AudioFrame): VoiceResult
}