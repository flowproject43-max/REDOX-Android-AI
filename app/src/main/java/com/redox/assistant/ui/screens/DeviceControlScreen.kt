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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel

@Composable
fun DeviceControlScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val diagnostics by viewModel.deviceDiagnostics.collectAsState()
    var isFlashlightOn by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(60f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        // Header
        Text(
            text = "DEVICE CONTROL CENTER",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Hardware Abstraction & System Subsystems",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hardware Controls Panel
        GlassSettingsPanel(title = "Hardware Switches") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Torch / Flashlight", color = TextWhitePrimary, fontSize = 15.sp)
                    Text(
                        text = if (isFlashlightOn) "ACTIVE" else "DISABLED",
                        color = if (isFlashlightOn) CrimsonRed else TextGraySecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                GlassButton(
                    text = if (isFlashlightOn) "Turn Off" else "Turn On",
                    icon = if (isFlashlightOn) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
                    onClick = {
                        isFlashlightOn = !isFlashlightOn
                        viewModel.executeDirectAction(DeviceAction.ToggleFlashlight(isFlashlightOn))
                    },
                    isPrimary = isFlashlightOn
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Media Volume", color = TextWhitePrimary, fontSize = 14.sp)
                    Text(text = "${volumeLevel.toInt()}%", color = CrimsonRed, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = volumeLevel,
                    onValueChange = { volumeLevel = it },
                    onValueChangeFinished = {
                        viewModel.executeDirectAction(DeviceAction.AdjustVolume(volumeLevel.toInt()))
                    },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = CrimsonRed,
                        activeTrackColor = CrimsonRed
                    )
                )
            }
        }

        // Media Playback Controls
        GlassSettingsPanel(title = "Media Transport") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GlassButton(
                    text = "Prev",
                    icon = Icons.Default.FastRewind,
                    onClick = { viewModel.executeDirectAction(DeviceAction.MediaControl("previous")) }
                )
                GlassButton(
                    text = "Play/Pause",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.executeDirectAction(DeviceAction.MediaControl("play")) },
                    isPrimary = true
                )
                GlassButton(
                    text = "Next",
                    icon = Icons.Default.FastForward,
                    onClick = { viewModel.executeDirectAction(DeviceAction.MediaControl("next")) }
                )
            }
        }

        // App Launch Shortcuts
        GlassSettingsPanel(title = "App Launch Automation") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassButton(
                    text = "YouTube",
                    icon = Icons.Default.VideoLibrary,
                    onClick = { viewModel.executeDirectAction(DeviceAction.OpenApp("YouTube")) },
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Camera",
                    icon = Icons.Default.CameraAlt,
                    onClick = { viewModel.executeDirectAction(DeviceAction.OpenApp("Camera")) },
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Maps",
                    icon = Icons.Default.Map,
                    onClick = { viewModel.executeDirectAction(DeviceAction.OpenApp("Maps")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // System Diagnostic Report
        GlassSettingsPanel(title = "Telemetry & Hardware Diagnostics") {
            Text(
                text = diagnostics.ifBlank { "Loading diagnostics telemetry..." },
                color = TextWhitePrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            GlassButton(
                text = "Refresh Telemetry",
                icon = Icons.Default.Refresh,
                onClick = { viewModel.refreshDiagnostics() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
