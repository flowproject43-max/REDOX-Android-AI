package com.redox.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel

@Composable
fun VoiceSettingsScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val voiceConfig by viewModel.voiceConfig.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "VOICE & SPEECH ENGINE",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Deep Cinematic Jarvis Synthesis & Wake Word Engine",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Vocal Tuning
        GlassSettingsPanel(title = "Jarvis Cinematic Tuning") {
            // Pitch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Voice Pitch (Deep Male)", color = TextWhitePrimary, fontSize = 14.sp)
                Text(text = String.format("%.2f", voiceConfig.pitch), color = CrimsonRed, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = voiceConfig.pitch,
                onValueChange = { viewModel.updateVoiceConfig(voiceConfig.copy(pitch = it)) },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = CrimsonRed, activeTrackColor = CrimsonRed)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Speech Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Speech Rate (Cadence)", color = TextWhitePrimary, fontSize = 14.sp)
                Text(text = String.format("%.2f", voiceConfig.speechRate), color = CrimsonRed, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = voiceConfig.speechRate,
                onValueChange = { viewModel.updateVoiceConfig(voiceConfig.copy(speechRate = it)) },
                valueRange = 0.5f..1.5f,
                colors = SliderDefaults.colors(thumbColor = CrimsonRed, activeTrackColor = CrimsonRed)
            )

            Spacer(modifier = Modifier.height(14.dp))

            GlassButton(
                text = "Preview Voice Profile",
                icon = Icons.Default.VolumeUp,
                onClick = {
                    viewModel.voiceEngine.speak("Greetings, sir. All core subsystems are functioning at peak efficiency. REDOX is at your disposal.")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Wake Word & Background Service
        GlassSettingsPanel(title = "Wake Word Subsystem") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Wake Phrase: 'Hey REDOX'", color = TextWhitePrimary, fontSize = 14.sp)
                    Text(text = "Continuous audio activation", color = TextGrayMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = voiceConfig.wakeWordEnabled,
                    onCheckedChange = { viewModel.updateVoiceConfig(voiceConfig.copy(wakeWordEnabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = CrimsonRed, checkedTrackColor = CrimsonRed.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Background Foreground Service", color = TextWhitePrimary, fontSize = 14.sp)
                    Text(text = "Persistent mic listening in background", color = TextGrayMuted, fontSize = 11.sp)
                }
                Switch(
                    checked = voiceConfig.backgroundListening,
                    onCheckedChange = { viewModel.updateVoiceConfig(voiceConfig.copy(backgroundListening = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = CrimsonRed, checkedTrackColor = CrimsonRed.copy(alpha = 0.5f))
                )
            }
        }
    }
}
