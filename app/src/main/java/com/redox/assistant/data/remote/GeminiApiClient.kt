package com.redox.assistant.data.remote

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.redox.assistant.domain.model.AiMessage
import com.redox.assistant.domain.model.MessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {

    suspend fun generateResponse(
        apiKey: String,
        modelName: String = "gemini-1.5-flash",
        history: List<AiMessage>,
        prompt: String,
        screenContext: String? = null,
        memoryContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val jsonMedia = "application/json; charset=utf-8".toMediaType()

            val root = JsonObject()

            // System Instructions for REDOX Jarvis persona
            val systemInstruction = JsonObject().apply {
                val parts = JsonArray().apply {
                    val part = JsonObject().apply {
                        addProperty(
                            "text",
                            """
                            You are REDOX, an autonomous, highly intelligent, elite Android AI assistant.
                            Persona: Sophisticated, confident, polite, loyal, and efficient, like a cinematic Jarvis.
                            Language Capabilities: You fluently understand and speak English, Bengali (বাংলা), Hindi (हिन्दी), and mixed Hinglish/Banglish queries naturally.
                            Key Directives:
                            1. Answer questions with razor-sharp precision and natural cinematic phrasing.
                            2. You have full device automation capabilities. When the user asks you to perform an action on the phone, specify the action clearly.
                            3. Supported device actions format:
                               - ACTION:OPEN_APP[app_name]
                               - ACTION:CALL[contact_or_number]
                               - ACTION:SMS[recipient, message]
                               - ACTION:WHATSAPP[recipient, message]
                               - ACTION:FLASHLIGHT[on/off]
                               - ACTION:VOLUME[percent]
                               - ACTION:MEDIA[play/pause/next/previous]
                               - ACTION:TIMER[seconds, label]
                               - ACTION:READ_SCREEN
                               - ACTION:STATUS
                            $memoryContext
                            """.trimIndent()
                        )
                    }
                    add(part)
                }
                add("parts", parts)
            }
            root.add("system_instruction", systemInstruction)

            // Contents array
            val contents = JsonArray()

            // History
            val recentHistory = history.takeLast(10)
            for (msg in recentHistory) {
                val contentObj = JsonObject()
                contentObj.addProperty("role", if (msg.sender == MessageSender.USER) "user" else "model")
                val parts = JsonArray()
                val part = JsonObject().apply {
                    addProperty("text", msg.text)
                }
                parts.add(part)
                contentObj.add("parts", parts)
                contents.add(contentObj)
            }

            // Current Prompt + Optional Screen Context
            val currentContent = JsonObject()
            currentContent.addProperty("role", "user")
            val currentParts = JsonArray()

            var fullUserPrompt = prompt
            if (!screenContext.isNullOrBlank()) {
                fullUserPrompt = "[Current Screen Hierarchy & Visible Text]:\n$screenContext\n\n[User Command]: $prompt"
            }

            val userPart = JsonObject().apply {
                addProperty("text", fullUserPrompt)
            }
            currentParts.add(userPart)
            currentContent.add("parts", currentParts)
            contents.add(currentContent)

            root.add("contents", contents)

            // Generation config
            val genConfig = JsonObject().apply {
                addProperty("temperature", 0.7)
                addProperty("maxOutputTokens", 1024)
            }
            root.add("generationConfig", genConfig)

            val body = gson.toJson(root).toRequestBody(jsonMedia)
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API Error ${response.code}: $respBody"))
            }

            val jsonResponse = gson.fromJson(respBody, JsonObject::class.java)
            val candidates = jsonResponse.getAsJsonArray("candidates")
            if (candidates != null && candidates.size() > 0) {
                val firstCandidate = candidates[0].asJsonObject
                val content = firstCandidate.getAsJsonObject("content")
                val parts = content.getAsJsonArray("parts")
                if (parts != null && parts.size() > 0) {
                    val text = parts[0].asJsonObject.get("text").asString
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("Empty candidate response from Gemini API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
