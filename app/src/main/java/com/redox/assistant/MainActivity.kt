package com.redox.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.redox.assistant.service.RedoxForegroundService
import com.redox.assistant.ui.navigation.RedoxNavigation
import com.redox.assistant.ui.theme.RedoxTheme
import com.redox.assistant.viewmodel.RedoxViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RedoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely check microphone permission before starting foreground service
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val voiceConfig = viewModel.voiceConfig.value
        if (hasMicPermission && voiceConfig.wakeWordEnabled && voiceConfig.backgroundListening) {
            try {
                RedoxForegroundService.start(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            RedoxTheme {
                RedoxNavigation(viewModel = viewModel)
            }
        }
    }
}
