package com.autosense.ai.camera

import android.view.Surface

internal interface CameraImageReaderHandle : AutoCloseable {

    val surface: Surface
}