package com.redox.assistant.domain.repository

import com.redox.assistant.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun getAllLogs(): Flow<List<ActivityLog>>
    suspend fun logActivity(log: ActivityLog)
    suspend fun clearLogs()
}
