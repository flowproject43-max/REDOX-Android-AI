package com.redox.assistant.domain.model

enum class OrbState(val statusLabel: String) {
    IDLE("STANDBY"),
    LISTENING("LISTENING"),
    THINKING("PROCESSING"),
    SPEAKING("TRANSMITTING"),
    ERROR("SYSTEM ALERT")
}
