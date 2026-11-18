package com.redox.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.OrbState
import com.redox.assistant.ui.components.AnimatedEnergyOrb
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.components.VoiceWaveform
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.GlassBorder
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel

@Composable
fun LiveVoiceScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val amplitude by viewModel.speechAmplitude.collectAsState()
    val transcript by viewModel.currentTranscript.collectAsState()
    val voiceConfig by viewModel.voiceConfig.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "JARVIS VOICE TERMINAL",
                color = CrimsonRed,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Full Duplex Neural Voice Engine",
                color = TextGrayMuted,
                fontSize = 12.sp
            )
        }

        // Center Energy Orb & Spectral Waveform
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedEnergyOrb(
                state = orbState,
                amplitude = amplitude,
                size = 240.dp,
                onClick = {
                    if (isListening) {
                        viewModel.stopVoiceInteraction()
                    } else {
                        viewModel.startVoiceInteraction()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Waveform Visualizer
            VoiceWaveform(
                amplitude = amplitude,
                isSpeaking = isSpeaking,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Real-Time Transcript Display
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                hasCrimsonGlow = isListening || isSpeaking,
                contentPadding = 16.dp
            ) {
                Text(
                    text = if (transcript.isNotBlank()) {
                        "\"$transcript\""
                    } else if (isListening) {
                        "Listening for your command, sir..."
                    } else if (isSpeaking) {
                        "REDOX transmitting vocal response..."
                    } else {
                        "Press microphone or say 'Hey REDOX' to begin."
                    },
                    color = if (transcript.isNotBlank()) TextWhitePrimary else TextGraySecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp)
        ) {
            // Language selector chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                listOf("English" to "en-US", "বাংলা" to "bn-BD", "हिन्दी" to "hi-IN").forEach { (label, code) ->
                    val isSelected = voiceConfig.languageCode == code
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CrimsonRed else Color(0x1AFFFFFF))
                            .border(1.dp, if (isSelected) Color.White else GlassBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.updateVoiceConfig(voiceConfig.copy(languageCode = code))
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextGraySecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSpeaking) {
                    GlassButton(
                        text = "Interrupt",
                        icon = Icons.Default.Stop,
                        onClick = { viewModel.stopVoiceInteraction() },
                        isPrimary = false
                    )
                }

                GlassButton(
                    text = if (isListening) "Stop Listening" else "Speak to REDOX",
                    icon = Icons.Default.Mic,
                    onClick = {
                        if (isListening) {
                            viewModel.stopVoiceInteraction()
                        } else {
                            viewModel.startVoiceInteraction()
                        }
                    },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
