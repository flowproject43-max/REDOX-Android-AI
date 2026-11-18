package com.redox.assistant.automation

import android.content.Context
import com.redox.assistant.service.RedoxAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenVisionManager(private val context: Context) {

    suspend fun captureScreenContext(): String = withContext(Dispatchers.Default) {
        val service = RedoxAccessibilityService.instance
        if (service == null) {
            return@withContext "Screen inspection unavailable: Accessibility service is inactive."
        }
        val textHierarchy = service.getVisibleScreenHierarchyText()
        if (textHierarchy.isBlank()) {
            return@withContext "Active screen appears blank or system overlay is active."
        }
        textHierarchy
    }
}
