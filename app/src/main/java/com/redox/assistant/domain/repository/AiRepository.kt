package com.redox.assistant.domain.repository

import com.redox.assistant.domain.model.AiMessage
import com.redox.assistant.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(
        conversationHistory: List<AiMessage>,
        userMessage: String,
        screenContext: String? = null
    ): Result<String>

    fun streamMessage(
        conversationHistory: List<AiMessage>,
        userMessage: String,
        screenContext: String? = null
    ): Flow<String>

    suspend fun testConnection(provider: AiProvider, apiKey: String): Result<Long>
}
