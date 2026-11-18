package com.redox.assistant.domain.model

data class VoiceConfig(
    val voicePersona: String = "Jarvis Cinematic Male",
    val pitch: Float = 0.78f,       // Deep cinematic pitch
    val speechRate: Float = 1.0f,   // Confident natural pace
    val volume: Float = 1.0f,
    val wakeWordEnabled: Boolean = true,
    val wakeWordSensitivity: Float = 0.8f,
    val backgroundListening: Boolean = true,
    val languageCode: String = "en-US", // Supports en-US, bn-BD, hi-IN
    val neuralTtsEnabled: Boolean = false
)
