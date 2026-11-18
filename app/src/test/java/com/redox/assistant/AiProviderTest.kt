package com.redox.assistant

import com.redox.assistant.domain.model.AiProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class AiProviderTest {

    @Test
    fun testProviderResolution() {
        assertEquals(AiProvider.GEMINI, AiProvider.fromString("gemini"))
        assertEquals(AiProvider.OPENAI, AiProvider.fromString("OPENAI"))
        assertEquals(AiProvider.CLAUDE, AiProvider.fromString("claude"))
        assertEquals(AiProvider.GROQ, AiProvider.fromString("Groq"))
        assertEquals(AiProvider.OPENROUTER, AiProvider.fromString("openrouter"))
        assertEquals(AiProvider.GEMINI, AiProvider.fromString("unknown_provider"))
    }
}
