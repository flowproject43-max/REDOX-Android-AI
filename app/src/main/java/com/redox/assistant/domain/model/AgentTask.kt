package com.redox.assistant.domain.model

import java.util.UUID

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    WAITING_CONFIRMATION
}

data class TaskStep(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val action: DeviceAction? = null,
    val status: StepStatus = StepStatus.PENDING,
    val result: String? = null
)

data class AgentTask(
    val id: String = UUID.randomUUID().toString(),
    val goal: String,
    val steps: List<TaskStep>,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
