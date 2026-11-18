package com.redox.assistant.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.redox.assistant.RedoxApplication
import com.redox.assistant.service.RedoxForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RedoxBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? RedoxApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val voiceConfig = app.settingsRepository.getVoiceConfig().first()
                if (voiceConfig.wakeWordEnabled && voiceConfig.backgroundListening) {
                    RedoxForegroundService.start(context)
                }
            }
        }
    }
}
