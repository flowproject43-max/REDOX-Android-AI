package com.redox.assistant.data.repository

import com.redox.assistant.data.local.SecurePreferences
import com.redox.assistant.domain.model.AiProvider
import com.redox.assistant.domain.model.VoiceConfig
import com.redox.assistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val prefs: SecurePreferences
) : SettingsRepository {

    private val activeProviderFlow = MutableStateFlow(
        AiProvider.fromString(prefs.getString(KEY_ACTIVE_PROVIDER, AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name)
    )

    private val voiceConfigFlow = MutableStateFlow(loadVoiceConfig())

    private val autoFallbackFlow = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_FALLBACK, true)
    )

    private val appLockFlow = MutableStateFlow(
        prefs.getBoolean(KEY_APP_LOCK, false)
    )

    private val sensitiveConfirmFlow = MutableStateFlow(
        prefs.getBoolean(KEY_SENSITIVE_CONFIRM, true)
    )

    private fun loadVoiceConfig(): VoiceConfig {
        return VoiceConfig(
            voicePersona = prefs.getString(KEY_VOICE_PERSONA, "Jarvis Cinematic Male") ?: "Jarvis Cinematic Male",
            pitch = prefs.getFloat(KEY_VOICE_PITCH, 0.78f),
            speechRate = prefs.getFloat(KEY_VOICE_RATE, 1.0f),
            volume = prefs.getFloat(KEY_VOICE_VOLUME, 1.0f),
            wakeWordEnabled = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, true),
            wakeWordSensitivity = prefs.getFloat(KEY_WAKE_SENSITIVITY, 0.8f),
            backgroundListening = prefs.getBoolean(KEY_BG_LISTENING, true),
            languageCode = prefs.getString(KEY_LANGUAGE, "en-US") ?: "en-US",
            neuralTtsEnabled = prefs.getBoolean(KEY_NEURAL_TTS, false)
        )
    }

    override fun getActiveProvider(): Flow<AiProvider> = activeProviderFlow.asStateFlow()

    override suspend fun setActiveProvider(provider: AiProvider) {
        prefs.putString(KEY_ACTIVE_PROVIDER, provider.name)
        activeProviderFlow.value = provider
    }

    override suspend fun getApiKey(provider: AiProvider): String? {
        return prefs.getSecureString("api_key_${provider.name}")
    }

    override suspend fun setApiKey(provider: AiProvider, key: String) {
        prefs.putSecureString("api_key_${provider.name}", key)
    }

    override suspend fun getModelName(provider: AiProvider): String {
        return prefs.getString("model_name_${provider.name}", provider.defaultModel) ?: provider.defaultModel
    }

    override suspend fun setModelName(provider: AiProvider, model: String) {
        prefs.putString("model_name_${provider.name}", model)
    }

    override fun getVoiceConfig(): Flow<VoiceConfig> = voiceConfigFlow.asStateFlow()

    override suspend fun setVoiceConfig(config: VoiceConfig) {
        prefs.putString(KEY_VOICE_PERSONA, config.voicePersona)
        prefs.putFloat(KEY_VOICE_PITCH, config.pitch)
        prefs.putFloat(KEY_VOICE_RATE, config.speechRate)
        prefs.putFloat(KEY_VOICE_VOLUME, config.volume)
        prefs.putBoolean(KEY_WAKE_WORD_ENABLED, config.wakeWordEnabled)
        prefs.putFloat(KEY_WAKE_SENSITIVITY, config.wakeWordSensitivity)
        prefs.putBoolean(KEY_BG_LISTENING, config.backgroundListening)
        prefs.putString(KEY_LANGUAGE, config.languageCode)
        prefs.putBoolean(KEY_NEURAL_TTS, config.neuralTtsEnabled)
        voiceConfigFlow.value = config
    }

    override fun isAutoFallbackEnabled(): Flow<Boolean> = autoFallbackFlow.asStateFlow()

    override suspend fun setAutoFallbackEnabled(enabled: Boolean) {
        prefs.putBoolean(KEY_AUTO_FALLBACK, enabled)
        autoFallbackFlow.value = enabled
    }

    override fun isAppLockEnabled(): Flow<Boolean> = appLockFlow.asStateFlow()

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        prefs.putBoolean(KEY_APP_LOCK, enabled)
        appLockFlow.value = enabled
    }

    override suspend fun getAppPin(): String? {
        return prefs.getSecureString(KEY_APP_PIN)
    }

    override suspend fun setAppPin(pin: String?) {
        if (pin == null) {
            prefs.putString(KEY_APP_PIN, "")
        } else {
            prefs.putSecureString(KEY_APP_PIN, pin)
        }
    }

    override fun isSensitiveConfirmationRequired(): Flow<Boolean> = sensitiveConfirmFlow.asStateFlow()

    override suspend fun setSensitiveConfirmationRequired(required: Boolean) {
        prefs.putBoolean(KEY_SENSITIVE_CONFIRM, required)
        sensitiveConfirmFlow.value = required
    }

    companion object {
        private const val KEY_ACTIVE_PROVIDER = "active_ai_provider"
        private const val KEY_VOICE_PERSONA = "voice_persona"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_RATE = "voice_rate"
        private const val KEY_VOICE_VOLUME = "voice_volume"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_WAKE_SENSITIVITY = "wake_sensitivity"
        private const val KEY_BG_LISTENING = "bg_listening"
        private const val KEY_LANGUAGE = "voice_language"
        private const val KEY_NEURAL_TTS = "neural_tts"
        private const val KEY_AUTO_FALLBACK = "auto_fallback"
        private const val KEY_APP_LOCK = "app_lock_enabled"
        private const val KEY_APP_PIN = "app_pin_code"
        private const val KEY_SENSITIVE_CONFIRM = "sensitive_confirmation"
    }
}
