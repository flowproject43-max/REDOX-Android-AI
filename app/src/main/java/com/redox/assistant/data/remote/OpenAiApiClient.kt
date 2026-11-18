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

class OpenAiApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {

    suspend fun generateResponse(
        endpointUrl: String,
        apiKey: String,
        modelName: String,
        history: List<AiMessage>,
        prompt: String,
        screenContext: String? = null,
        memoryContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonMedia = "application/json; charset=utf-8".toMediaType()
            val root = JsonObject()
            root.addProperty("model", modelName)
            root.addProperty("temperature", 0.7)

            val messages = JsonArray()

            // System prompt
            val sysMsg = JsonObject().apply {
                addProperty("role", "system")
                addProperty(
                    "content",
                    """
                    You are REDOX, an autonomous, highly intelligent, elite Android AI assistant.
                    Persona: Sophisticated, confident, polite, loyal, and efficient, like a cinematic Jarvis.
                    Language Capabilities: You fluently understand and speak English, Bengali (বাংলা), Hindi (हिन्दी), and mixed Hinglish/Banglish.
                    Device Actions Supported:
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
            messages.add(sysMsg)

            // History
            for (msg in history.takeLast(10)) {
                val m = JsonObject().apply {
                    addProperty("role", if (msg.sender == MessageSender.USER) "user" else "assistant")
                    addProperty("content", msg.text)
                }
                messages.add(m)
            }

            // User prompt + screen
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
                .url(endpointUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error ${response.code}: $respBody"))
            }

            val json = gson.fromJson(respBody, JsonObject::class.java)
            val choices = json.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val msg = choices[0].asJsonObject.getAsJsonObject("message")
                val content = msg.get("content").asString
                return@withContext Result.success(content)
            }

            Result.failure(Exception("Empty choices in API response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
