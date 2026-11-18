package com.redox.assistant.data.repository

import com.redox.assistant.RedoxApplication
import com.redox.assistant.data.remote.ClaudeApiClient
import com.redox.assistant.data.remote.GeminiApiClient
import com.redox.assistant.data.remote.OpenAiApiClient
import com.redox.assistant.domain.model.AiMessage
import com.redox.assistant.domain.model.AiProvider
import com.redox.assistant.domain.repository.AiRepository
import com.redox.assistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class AiRepositoryImpl(
    private val settingsRepository: SettingsRepository,
    private val geminiClient: GeminiApiClient = GeminiApiClient(),
    private val openAiClient: OpenAiApiClient = OpenAiApiClient(),
    private val claudeClient: ClaudeApiClient = ClaudeApiClient()
) : AiRepository {

    override suspend fun sendMessage(
        conversationHistory: List<AiMessage>,
        userMessage: String,
        screenContext: String?
    ): Result<String> {
        val activeProvider = settingsRepository.getActiveProvider().first()
        val isFallbackEnabled = settingsRepository.isAutoFallbackEnabled().first()
        val memoryContext = RedoxApplication.instance.memoryRepository.buildMemoryContext()

        // Primary call attempt
        val primaryResult = executeProviderCall(activeProvider, conversationHistory, userMessage, screenContext, memoryContext)
        if (primaryResult.isSuccess) {
            return primaryResult
        }

        // Automatic fallback if enabled
        if (isFallbackEnabled) {
            val fallbackCandidates = AiProvider.entries.filter { it != activeProvider }
            for (fallback in fallbackCandidates) {
                val apiKey = settingsRepository.getApiKey(fallback)
                if (!apiKey.isNullOrBlank()) {
                    val fallbackResult = executeProviderCall(fallback, conversationHistory, userMessage, screenContext, memoryContext)
                    if (fallbackResult.isSuccess) {
                        return fallbackResult
                    }
                }
            }
        }

        // If all API calls fail or no keys are configured, provide an intelligent local Jarvis response
        if (primaryResult.isFailure) {
            val errorMsg = primaryResult.exceptionOrNull()?.message ?: "Unknown API failure"
            return if (errorMsg.contains("API Key missing", ignoreCase = true)) {
                Result.success(
                    "Greetings. I am operating in offline triage mode. To unlock full cognitive reasoning, please configure your API key under AI Provider Settings. How may I assist you with device automation in the meantime?"
                )
            } else {
                primaryResult
            }
        }

        return primaryResult
    }

    private suspend fun executeProviderCall(
        provider: AiProvider,
        history: List<AiMessage>,
        prompt: String,
        screenContext: String?,
        memoryContext: String
    ): Result<String> {
        val apiKey = settingsRepository.getApiKey(provider)
        if (apiKey.isNullOrBlank()) {
            return Result.failure(Exception("API Key missing for ${provider.displayName}. Please configure it in AI Provider Settings."))
        }

        return when (provider) {
            AiProvider.GEMINI -> {
                geminiClient.generateResponse(
                    apiKey = apiKey,
                    modelName = provider.defaultModel,
                    history = history,
                    prompt = prompt,
                    screenContext = screenContext,
                    memoryContext = memoryContext
                )
            }
            AiProvider.OPENAI -> {
                openAiClient.generateResponse(
                    endpointUrl = "https://api.openai.com/v1/chat/completions",
                    apiKey = apiKey,
                    modelName = provider.defaultModel,
                    history = history,
                    prompt = prompt,
                    screenContext = screenContext,
                    memoryContext = memoryContext
                )
            }
            AiProvider.GROQ -> {
                openAiClient.generateResponse(
                    endpointUrl = "https://api.groq.com/openai/v1/chat/completions",
                    apiKey = apiKey,
                    modelName = provider.defaultModel,
                    history = history,
                    prompt = prompt,
                    screenContext = screenContext,
                    memoryContext = memoryContext
                )
            }
            AiProvider.OPENROUTER -> {
                openAiClient.generateResponse(
                    endpointUrl = "https://openrouter.ai/api/v1/chat/completions",
                    apiKey = apiKey,
                    modelName = provider.defaultModel,
                    history = history,
                    prompt = prompt,
                    screenContext = screenContext,
                    memoryContext = memoryContext
                )
            }
            AiProvider.CLAUDE -> {
                claudeClient.generateResponse(
                    apiKey = apiKey,
                    modelName = provider.defaultModel,
                    history = history,
                    prompt = prompt,
                    screenContext = screenContext,
                    memoryContext = memoryContext
                )
            }
        }
    }

    override fun streamMessage(
        conversationHistory: List<AiMessage>,
        userMessage: String,
        screenContext: String?
    ): Flow<String> = flow {
        // Emit word chunks or full response
        val result = sendMessage(conversationHistory, userMessage, screenContext)
        if (result.isSuccess) {
            val fullText = result.getOrNull() ?: ""
            val words = fullText.split(" ")
            val sb = StringBuilder()
            for (word in words) {
                sb.append(word).append(" ")
                emit(sb.toString())
                kotlinx.coroutines.delay(25)
            }
        } else {
            throw result.exceptionOrNull() ?: Exception("Unknown generation failure")
        }
    }

    override suspend fun testConnection(provider: AiProvider, apiKey: String): Result<Long> {
        val startTime = System.currentTimeMillis()
        val testPrompt = "Ping. Respond with one word: 'ONLINE'."
        val result = when (provider) {
            AiProvider.GEMINI -> geminiClient.generateResponse(apiKey, provider.defaultModel, emptyList(), testPrompt)
            AiProvider.OPENAI -> openAiClient.generateResponse("https://api.openai.com/v1/chat/completions", apiKey, provider.defaultModel, emptyList(), testPrompt)
            AiProvider.GROQ -> openAiClient.generateResponse("https://api.groq.com/openai/v1/chat/completions", apiKey, provider.defaultModel, emptyList(), testPrompt)
            AiProvider.OPENROUTER -> openAiClient.generateResponse("https://openrouter.ai/api/v1/chat/completions", apiKey, provider.defaultModel, emptyList(), testPrompt)
            AiProvider.CLAUDE -> claudeClient.generateResponse(apiKey, provider.defaultModel, emptyList(), testPrompt)
        }
        val latency = System.currentTimeMillis() - startTime
        return if (result.isSuccess) {
            Result.success(latency)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Connection test failed"))
        }
    }
}
