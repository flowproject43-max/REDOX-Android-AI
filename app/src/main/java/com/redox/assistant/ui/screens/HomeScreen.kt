package com.redox.assistant.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.domain.model.OrbState
import com.redox.assistant.ui.components.AnimatedEnergyOrb
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.navigation.Screen
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.StatusSuccess
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: RedoxViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val amplitude by viewModel.speechAmplitude.collectAsState()
    val activeProvider by viewModel.activeProvider.collectAsState()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()

    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top HUD Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "REDOX",
                    color = CrimsonRed,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "JARVIS PROTOCOL v1.0",
                    color = TextGrayMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // System Status Pill
            LiquidGlassCard(
                hasCrimsonGlow = orbState != OrbState.IDLE,
                contentPadding = 8.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (orbState == OrbState.ERROR) CrimsonRed else StatusSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = orbState.statusLabel,
                        color = TextWhitePrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Clock & Date HUD
        Text(
            text = currentTime,
            color = TextWhitePrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
        Text(
            text = currentDate.uppercase(),
            color = TextGraySecondary,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Center Energy Orb
        AnimatedEnergyOrb(
            state = orbState,
            amplitude = amplitude,
            size = 230.dp,
            onClick = {
                if (orbState == OrbState.LISTENING) {
                    viewModel.stopVoiceInteraction()
                } else {
                    viewModel.startVoiceInteraction()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tap Instruction / Status Pill
        Text(
            text = when (orbState) {
                OrbState.IDLE -> "TAP ORB OR SAY 'HEY REDOX'"
                OrbState.LISTENING -> "LISTENING... SPEAK NOW"
                OrbState.THINKING -> "PROCESSING COMMAND..."
                OrbState.SPEAKING -> "TRANSMITTING RESPONSE..."
                OrbState.ERROR -> "SYSTEM ALERT DETECTED"
            },
            color = if (orbState == OrbState.IDLE) TextGraySecondary else CrimsonRed,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Quick Navigation Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.weight(1f),
                hasCrimsonGlow = false,
                contentPadding = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(
                        text = "Live Voice",
                        icon = Icons.Default.Mic,
                        onClick = { onNavigate(Screen.LiveVoice) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            LiquidGlassCard(
                modifier = Modifier.weight(1f),
                hasCrimsonGlow = false,
                contentPadding = 12.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GlassButton(
                        text = "AI Chat",
                        icon = Icons.Default.Chat,
                        onClick = { onNavigate(Screen.Chat) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LiquidGlassCard(
                modifier = Modifier.weight(1f),
                contentPadding = 12.dp
            ) {
                GlassButton(
                    text = "Vision HUD",
                    icon = Icons.Default.Visibility,
                    onClick = { onNavigate(Screen.Vision) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LiquidGlassCard(
                modifier = Modifier.weight(1f),
                contentPadding = 12.dp
            ) {
                GlassButton(
                    text = "Automation",
                    icon = Icons.Default.SmartToy,
                    onClick = { onNavigate(Screen.Automation) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // System Diagnostic Overview Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORE DIAGNOSTICS",
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "PROVIDER: ${activeProvider.displayName.uppercase()}",
                        color = TextGrayMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Accessibility Service:",
                        color = TextGraySecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isAccessibilityActive) "ACTIVE" else "INACTIVE",
                        color = if (isAccessibilityActive) StatusSuccess else CrimsonRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
