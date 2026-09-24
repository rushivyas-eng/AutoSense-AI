package com.autosense.ai.api.intent

interface IntentEngine {

    fun process(input: IntentInput): Intent
}