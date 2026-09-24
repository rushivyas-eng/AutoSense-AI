package com.autosense.ai.api.context

interface ContextEngine {

    fun process(input: ContextInput): ContextState
}