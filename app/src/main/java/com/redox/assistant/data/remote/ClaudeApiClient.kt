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

class ClaudeApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {

    suspend fun generateResponse(
        apiKey: String,
        modelName: String = "claude-3-5-sonnet-20241022",
        history: List<AiMessage>,
        prompt: String,
        screenContext: String? = null,
        memoryContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonMedia = "application/json; charset=utf-8".toMediaType()
            val root = JsonObject()
            root.addProperty("model", modelName)
            root.addProperty("max_tokens", 1024)
            root.addProperty(
                "system",
                """
                You are REDOX, an autonomous, highly intelligent, elite Android AI assistant.
                Persona: Sophisticated, confident, polite, loyal, and efficient, like a cinematic Jarvis.
                Language Capabilities: You fluently understand and speak English, Bengali (বাংলা), Hindi (हिन्दी), and mixed Hinglish/Banglish.
                Device Actions:
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

            val messages = JsonArray()

            for (msg in history.takeLast(10)) {
                val m = JsonObject().apply {
                    addProperty("role", if (msg.sender == MessageSender.USER) "user" else "assistant")
                    addProperty("content", msg.text)
                }
                messages.add(m)
            }

            var userText = prompt
            if (!screenContext.isNullOrBlank()) {
                userText = "[Current Screen Hierarchy & Visible Text]:\n$screenContext\n\n[User Command]: $prompt"
            }
            val userMsg = JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", userText)
            }
            messages.add(userMsg)

            root.add("messages", messages)

            val body = gson.toJson(root).toRequestBody(jsonMedia)
            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Claude API Error ${response.code}: $respBody"))
            }

            val json = gson.fromJson(respBody, JsonObject::class.java)
            val contentArr = json.getAsJsonArray("content")
            if (contentArr != null && contentArr.size() > 0) {
                val firstContent = contentArr[0].asJsonObject
                val text = firstContent.get("text").asString
                return@withContext Result.success(text)
            }

            Result.failure(Exception("Empty content from Claude API"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
