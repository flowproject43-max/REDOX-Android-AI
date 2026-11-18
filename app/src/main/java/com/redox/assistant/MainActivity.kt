package com.redox.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.redox.assistant.service.RedoxForegroundService
import com.redox.assistant.ui.navigation.RedoxNavigation
import com.redox.assistant.ui.theme.RedoxTheme
import com.redox.assistant.viewmodel.RedoxViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RedoxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background assistant service if enabled
        val voiceConfig = viewModel.voiceConfig.value
        if (voiceConfig.wakeWordEnabled && voiceConfig.backgroundListening) {
            RedoxForegroundService.start(this)
        }

        setContent {
            RedoxTheme {
                RedoxNavigation(viewModel = viewModel)
            }
        }
    }
}
