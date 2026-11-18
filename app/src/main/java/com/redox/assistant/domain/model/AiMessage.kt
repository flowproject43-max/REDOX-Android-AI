package com.redox.assistant.domain.model

import java.util.UUID

enum class MessageSender {
    USER,
    REDOX,
    SYSTEM
}

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val providerUsed: String = "REDOX",
    val toolCallInfo: String? = null
)
