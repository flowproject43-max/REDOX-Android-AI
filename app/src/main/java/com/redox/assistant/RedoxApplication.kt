package com.redox.assistant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.redox.assistant.data.local.RedoxDatabase
import com.redox.assistant.data.local.SecurePreferences
import com.redox.assistant.data.repository.ActivityLogRepositoryImpl
import com.redox.assistant.data.repository.AiRepositoryImpl
import com.redox.assistant.data.repository.MemoryRepositoryImpl
import com.redox.assistant.data.repository.SettingsRepositoryImpl

class RedoxApplication : Application() {

    lateinit var database: RedoxDatabase
        private set

    lateinit var securePreferences: SecurePreferences
        private set

    lateinit var settingsRepository: SettingsRepositoryImpl
        private set

    lateinit var memoryRepository: MemoryRepositoryImpl
        private set

    lateinit var activityLogRepository: ActivityLogRepositoryImpl
        private set

    lateinit var aiRepository: AiRepositoryImpl
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize local persistence & encrypted storage
        database = RedoxDatabase(this)
        securePreferences = SecurePreferences(this)
        settingsRepository = SettingsRepositoryImpl(securePreferences)
        memoryRepository = MemoryRepositoryImpl(database)
        activityLogRepository = ActivityLogRepositoryImpl(database)
        aiRepository = AiRepositoryImpl(settingsRepository)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_FOREGROUND,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_FOREGROUND = "redox_foreground_channel"
        lateinit var instance: RedoxApplication
            private set
    }
}
