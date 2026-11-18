package com.redox.assistant.domain.model

import java.util.UUID

enum class MemoryCategory(val label: String) {
    FACT("Fact"),
    PREFERENCE("Preference"),
    CONTACT("Contact"),
    AUTOMATION("Routine")
}

data class MemoryItem(
    val id: String = UUID.randomUUID().toString(),
    val category: MemoryCategory,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
