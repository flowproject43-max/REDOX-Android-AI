package com.redox.assistant.domain.model

enum class AiProvider(
    val displayName: String,
    val defaultModel: String,
    val requiresApiKey: Boolean = true
) {
    GEMINI("Google Gemini", "gemini-2.0-flash", true),
    OPENAI("OpenAI", "gpt-4o", true),
    CLAUDE("Anthropic Claude", "claude-3-5-sonnet-20241022", true),
    GROQ("Groq (Ultra-Fast)", "llama-3.3-70b-versatile", true),
    OPENROUTER("OpenRouter", "google/gemini-2.0-flash-exp:free", true);

    companion object {
        fun fromString(name: String): AiProvider {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: GEMINI
        }
    }
}
