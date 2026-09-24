package com.autosense.ai.api.action

interface DecisionEngine {

    fun process(input: DecisionInput): Decision
}