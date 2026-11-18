package com.redox.assistant.domain.repository

import com.redox.assistant.domain.model.AiProvider
import com.redox.assistant.domain.model.VoiceConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getActiveProvider(): Flow<AiProvider>
    suspend fun setActiveProvider(provider: AiProvider)

    suspend fun getApiKey(provider: AiProvider): String?
    suspend fun setApiKey(provider: AiProvider, key: String)

    suspend fun getModelName(provider: AiProvider): String
    suspend fun setModelName(provider: AiProvider, model: String)

    fun getVoiceConfig(): Flow<VoiceConfig>
    suspend fun setVoiceConfig(config: VoiceConfig)

    fun isAutoFallbackEnabled(): Flow<Boolean>
    suspend fun setAutoFallbackEnabled(enabled: Boolean)

    fun isAppLockEnabled(): Flow<Boolean>
    suspend fun setAppLockEnabled(enabled: Boolean)

    suspend fun getAppPin(): String?
    suspend fun setAppPin(pin: String?)

    fun isSensitiveConfirmationRequired(): Flow<Boolean>
    suspend fun setSensitiveConfirmationRequired(required: Boolean)
}
