package com.redox.assistant.domain.model

import java.util.UUID

enum class ActivityStatus {
    SUCCESS,
    FAILED,
    CONFIRMATION_REQUIRED,
    CANCELLED
}

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),
    val actionName: String,
    val description: String,
    val status: ActivityStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
