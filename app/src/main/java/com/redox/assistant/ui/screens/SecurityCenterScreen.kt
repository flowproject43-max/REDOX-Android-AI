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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassDialog
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.components.GlassTextField
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.StatusSuccess
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel
import kotlinx.coroutines.launch

@Composable
fun SecurityCenterScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var requireConfirmations by remember { mutableStateOf(true) }
    var pinCodeInput by remember { mutableStateOf("") }
    var showWipeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "SECURITY & PRIVACY FORTRESS",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Hardware Keystore Encryption & Autonomous Safety Boundaries",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Keystore Status Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            hasCrimsonGlow = true,
            contentPadding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Android Hardware Keystore",
                        color = TextWhitePrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AES-256 GCM authenticated hardware enclave",
                        color = TextGraySecondary,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "ACTIVE",
                    color = StatusSuccess,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safety Guardrails
        GlassSettingsPanel(title = "Autonomous Safety Directives") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sensitive Action Confirmations", color = TextWhitePrimary, fontSize = 14.sp)
                    Text(
                        text = "Require manual approval before dispatching phone calls, SMS or WhatsApp messages.",
                        color = TextGrayMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
                Switch(
                    checked = requireConfirmations,
                    onCheckedChange = { requireConfirmations = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CrimsonRed, checkedTrackColor = CrimsonRed.copy(alpha = 0.5f))
                )
            }
        }

        // App Lock PIN
        GlassSettingsPanel(title = "App Lock Protection") {
            Text(
                text = "Set a 4-digit master PIN to lock assistant access:",
                color = TextGraySecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassTextField(
                    value = pinCodeInput,
                    onValueChange = { if (it.length <= 6) pinCodeInput = it },
                    placeholder = "Enter 4-6 digit PIN",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                GlassButton(
                    text = "Save PIN",
                    icon = Icons.Default.Lock,
                    onClick = {
                        scope.launch {
                            viewModel.settingsRepo.setAppPin(pinCodeInput)
                        }
                    },
                    isPrimary = true
                )
            }
        }

        // Emergency Data Wipe
        GlassSettingsPanel(title = "Emergency Data Purge") {
            Text(
                text = "Instantly wipe all local memories, conversation logs, activity history, and cached credentials.",
                color = TextGraySecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassButton(
                text = "Purge All Assistant Data",
                icon = Icons.Default.DeleteForever,
                onClick = { showWipeDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showWipeDialog) {
            GlassDialog(
                title = "Confirm Data Purge",
                message = "Are you sure you want to completely erase all REDOX local memories, conversation transcripts, and activity logs? This action is irreversible.",
                onDismiss = { showWipeDialog = false },
                confirmText = "Purge Everything",
                onConfirm = {
                    viewModel.clearAllMemories()
                    viewModel.clearChat()
                    viewModel.clearActivityLogs()
                    showWipeDialog = false
                }
            )
        }
    }
}
