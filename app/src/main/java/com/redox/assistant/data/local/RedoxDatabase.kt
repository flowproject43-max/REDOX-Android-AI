package com.redox.assistant.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.redox.assistant.domain.model.ActivityLog
import com.redox.assistant.domain.model.ActivityStatus
import com.redox.assistant.domain.model.AiMessage
import com.redox.assistant.domain.model.MemoryCategory
import com.redox.assistant.domain.model.MemoryItem
import com.redox.assistant.domain.model.MessageSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class RedoxDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val _memoryUpdates = MutableSharedFlow<Unit>(replay = 1)
    val memoryUpdates: Flow<Unit> = _memoryUpdates.asSharedFlow()

    private val _logUpdates = MutableSharedFlow<Unit>(replay = 1)
    val logUpdates: Flow<Unit> = _logUpdates.asSharedFlow()

    private val _chatUpdates = MutableSharedFlow<Unit>(replay = 1)
    val chatUpdates: Flow<Unit> = _chatUpdates.asSharedFlow()

    init {
        _memoryUpdates.tryEmit(Unit)
        _logUpdates.tryEmit(Unit)
        _chatUpdates.tryEmit(Unit)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MEMORIES (
                id TEXT PRIMARY KEY,
                category TEXT NOT NULL,
                item_key TEXT NOT NULL,
                item_value TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_LOGS (
                id TEXT PRIMARY KEY,
                action_name TEXT NOT NULL,
                description TEXT NOT NULL,
                status TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                details TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CHAT (
                id TEXT PRIMARY KEY,
                sender TEXT NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                provider TEXT NOT NULL,
                tool_info TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEMORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        onCreate(db)
    }

    // --- Memories Operations ---

    fun insertOrUpdateMemory(item: MemoryItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", item.id)
            put("category", item.category.name)
            put("item_key", item.key)
            put("item_value", item.value)
            put("timestamp", item.timestamp)
        }
        db.insertWithOnConflict(TABLE_MEMORIES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        _memoryUpdates.tryEmit(Unit)
    }

    fun getAllMemories(): List<MemoryItem> {
        val list = mutableListOf<MemoryItem>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, category, item_key, item_value, timestamp FROM $TABLE_MEMORIES ORDER BY timestamp DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val catStr = it.getString(1)
                val cat = try {
                    MemoryCategory.valueOf(catStr)
                } catch (e: Exception) {
                    MemoryCategory.FACT
                }
                list.add(
                    MemoryItem(
                        id = it.getString(0),
                        category = cat,
                        key = it.getString(2),
                        value = it.getString(3),
                        timestamp = it.getLong(4)
                    )
                )
            }
        }
        return list
    }

    fun deleteMemory(id: String) {
        val db = writableDatabase
        db.delete(TABLE_MEMORIES, "id = ?", arrayOf(id))
        _memoryUpdates.tryEmit(Unit)
    }

    fun clearAllMemories() {
        val db = writableDatabase
        db.delete(TABLE_MEMORIES, null, null)
        _memoryUpdates.tryEmit(Unit)
    }

    // --- Activity Logs Operations ---

    fun insertLog(log: ActivityLog) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", log.id)
            put("action_name", log.actionName)
            put("description", log.description)
            put("status", log.status.name)
            put("timestamp", log.timestamp)
            put("details", log.details)
        }
        db.insertWithOnConflict(TABLE_LOGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        _logUpdates.tryEmit(Unit)
    }

    fun getAllLogs(): List<ActivityLog> {
        val list = mutableListOf<ActivityLog>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, action_name, description, status, timestamp, details FROM $TABLE_LOGS ORDER BY timestamp DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val statusStr = it.getString(3)
                val status = try {
                    ActivityStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    ActivityStatus.SUCCESS
                }
                list.add(
                    ActivityLog(
                        id = it.getString(0),
                        actionName = it.getString(1),
                        description = it.getString(2),
                        status = status,
                        timestamp = it.getLong(4),
                        details = it.getString(5)
                    )
                )
            }
        }
        return list
    }

    fun clearLogs() {
        val db = writableDatabase
        db.delete(TABLE_LOGS, null, null)
        _logUpdates.tryEmit(Unit)
    }

    // --- Chat Messages Operations ---

    fun insertChatMessage(message: AiMessage) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", message.id)
            put("sender", message.sender.name)
            put("content", message.text)
            put("timestamp", message.timestamp)
            put("provider", message.providerUsed)
            put("tool_info", message.toolCallInfo)
        }
        db.insertWithOnConflict(TABLE_CHAT, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        _chatUpdates.tryEmit(Unit)
    }

    fun getAllChatMessages(): List<AiMessage> {
        val list = mutableListOf<AiMessage>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, sender, content, timestamp, provider, tool_info FROM $TABLE_CHAT ORDER BY timestamp ASC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val sender = try {
                    MessageSender.valueOf(it.getString(1))
                } catch (e: Exception) {
                    MessageSender.REDOX
                }
                list.add(
                    AiMessage(
                        id = it.getString(0),
                        sender = sender,
                        text = it.getString(2),
                        timestamp = it.getLong(3),
                        providerUsed = it.getString(4),
                        toolCallInfo = it.getString(5)
                    )
                )
            }
        }
        return list
    }

    fun clearChat() {
        val db = writableDatabase
        db.delete(TABLE_CHAT, null, null)
        _chatUpdates.tryEmit(Unit)
    }

    companion object {
        private const val DATABASE_NAME = "redox_assistant.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_MEMORIES = "memories"
        private const val TABLE_LOGS = "activity_logs"
        private const val TABLE_CHAT = "chat_messages"
    }
}
