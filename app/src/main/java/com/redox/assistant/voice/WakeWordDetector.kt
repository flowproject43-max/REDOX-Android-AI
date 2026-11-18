package com.redox.assistant.voice

class WakeWordDetector {

    fun isWakeWordDetected(text: String, sensitivity: Float = 0.8f): Boolean {
        val clean = text.lowercase().trim()
        val keywords = listOf("hey redox", "ok redox", "redox", "hey readox", "hey ridox")
        for (kw in keywords) {
            if (clean.contains(kw)) {
                return true
            }
        }
        return false
    }

    fun extractCommandAfterWakeWord(text: String): String {
        val clean = text.lowercase().trim()
        val keywords = listOf("hey redox", "ok redox", "redox")
        for (kw in keywords) {
            val idx = clean.indexOf(kw)
            if (idx >= 0) {
                return text.substring(idx + kw.length).trim()
            }
        }
        return text
    }
}
