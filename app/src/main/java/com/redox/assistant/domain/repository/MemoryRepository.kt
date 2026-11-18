package com.redox.assistant.domain.repository

import com.redox.assistant.domain.model.MemoryCategory
import com.redox.assistant.domain.model.MemoryItem
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getAllMemories(): Flow<List<MemoryItem>>
    suspend fun getMemoriesByCategory(category: MemoryCategory): List<MemoryItem>
    suspend fun saveMemory(item: MemoryItem)
    suspend fun deleteMemory(id: String)
    suspend fun clearAllMemories()
    suspend fun buildMemoryContext(): String
}
