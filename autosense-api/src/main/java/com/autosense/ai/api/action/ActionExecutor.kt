package com.autosense.ai.api.action

interface ActionExecutor {

    fun execute(action: Action): ActionExecutionResult
}