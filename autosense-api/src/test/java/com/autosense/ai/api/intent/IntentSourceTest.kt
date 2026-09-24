package com.autosense.ai.api.intent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentSourceTest {

    @Test
    fun intentSource_containsExpectedValues() {
        assertEquals(IntentSource.VOICE, IntentSource.valueOf("VOICE"))
        assertEquals(IntentSource.VISION, IntentSource.valueOf("VISION"))
        assertEquals(IntentSource.CONTEXT, IntentSource.valueOf("CONTEXT"))
        assertEquals(IntentSource.UNKNOWN, IntentSource.valueOf("UNKNOWN"))
    }
}