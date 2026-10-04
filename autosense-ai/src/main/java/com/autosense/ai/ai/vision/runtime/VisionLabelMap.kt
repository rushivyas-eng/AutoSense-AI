package com.autosense.ai.ai.vision.runtime

internal interface VisionLabelMap {

    fun getLabel(classId: Int): String
}