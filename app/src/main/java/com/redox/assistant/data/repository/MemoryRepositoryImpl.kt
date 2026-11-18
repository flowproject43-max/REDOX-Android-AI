package com.redox.assistant.data.repository

import com.redox.assistant.data.local.RedoxDatabase
import com.redox.assistant.domain.model.MemoryCategory
import com.redox.assistant.domain.model.MemoryItem
import com.redox.assistant.domain.repository.MemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class MemoryRepositoryImpl(
    private val db: RedoxDatabase
) : MemoryRepository {

    override fun getAllMemories(): Flow<List<MemoryItem>> = flow {
        emit(db.getAllMemories())
        db.memoryUpdates.collect {
            emit(db.getAllMemories())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getMemoriesByCategory(category: MemoryCategory): List<MemoryItem> =
        withContext(Dispatchers.IO) {
            db.getAllMemories().filter { it.category == category }
        }

    override suspend fun saveMemory(item: MemoryItem) = withContext(Dispatchers.IO) {
        db.insertOrUpdateMemory(item)
    }

    override suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        db.deleteMemory(id)
    }

    override suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        db.clearAllMemories()
    }

    override suspend fun buildMemoryContext(): String = withContext(Dispatchers.IO) {
        val memories = db.getAllMemories()
        if (memories.isEmpty()) return@withContext ""

        val sb = StringBuilder("\n[REDOX Long-Term Memory & User Profile]:\n")
        memories.forEach {
            sb.append("- [${it.category.label}] ${it.key}: ${it.value}\n")
        }
        sb.toString()
    }
}
