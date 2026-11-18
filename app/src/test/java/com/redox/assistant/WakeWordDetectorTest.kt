package com.redox.assistant

import com.redox.assistant.voice.WakeWordDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeWordDetectorTest {

    private val detector = WakeWordDetector()

    @Test
    fun testWakeWordTriggerRecognition() {
        assertTrue(detector.isWakeWordDetected("Hey REDOX what is the weather today?"))
        assertTrue(detector.isWakeWordDetected("ok redox turn on the flashlight"))
        assertTrue(detector.isWakeWordDetected("redox status report"))
        assertFalse(detector.isWakeWordDetected("Hello assistant what time is it?"))
    }

    @Test
    fun testExtractCommandAfterWakeWord() {
        val extracted = detector.extractCommandAfterWakeWord("Hey REDOX open Spotify and play jazz")
        assertEquals("open Spotify and play jazz", extracted)
    }
}
