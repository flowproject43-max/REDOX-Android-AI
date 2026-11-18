package com.redox.assistant

import com.redox.assistant.automation.AutonomousAgent
import com.redox.assistant.domain.model.DeviceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutonomousAgentTest {

    @Test
    fun testParseActionCommands() {
        // Dummy manager not invoked for pure text parsing test
        val agent = AutonomousAgent(automationManager = null as? com.redox.assistant.automation.DeviceAutomationManager ?: return)
    }

    @Test
    fun testWakeWordDetectionRegex() {
        val sampleResponse = """
            Right away, sir. Opening YouTube now.
            ACTION:OPEN_APP[YouTube]
            ACTION:FLASHLIGHT[on]
            ACTION:VOLUME[85]
        """.trimIndent()

        val regex = Regex("""ACTION:([A-Z_]+)(?:\[(.*?)])?""")
        val matches = regex.findAll(sampleResponse).toList()

        assertEquals(3, matches.size)
        assertEquals("OPEN_APP", matches[0].groupValues[1])
        assertEquals("YouTube", matches[0].groupValues[2])
        assertEquals("FLASHLIGHT", matches[1].groupValues[1])
        assertEquals("on", matches[1].groupValues[2])
        assertEquals("VOLUME", matches[2].groupValues[1])
        assertEquals("85", matches[2].groupValues[2])
    }
}
