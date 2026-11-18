package com.redox.assistant.data.repository

import com.redox.assistant.data.local.RedoxDatabase
import com.redox.assistant.domain.model.ActivityLog
import com.redox.assistant.domain.repository.ActivityLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ActivityLogRepositoryImpl(
    private val db: RedoxDatabase
) : ActivityLogRepository {

    override fun getAllLogs(): Flow<List<ActivityLog>> = flow {
        emit(db.getAllLogs())
        db.logUpdates.collect {
            emit(db.getAllLogs())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun logActivity(log: ActivityLog) = withContext(Dispatchers.IO) {
        db.insertLog(log)
    }

    override suspend fun clearLogs() = withContext(Dispatchers.IO) {
        db.clearLogs()
    }
}
