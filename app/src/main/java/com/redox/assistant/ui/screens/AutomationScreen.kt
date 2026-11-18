package com.redox.assistant.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.service.RedoxAccessibilityService
import com.redox.assistant.ui.components.GlassButton
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

@Composable
fun AutomationScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsState()

    var clickTargetText by remember { mutableStateOf("") }
    var typeTextContent by remember { mutableStateOf("") }
    var lastExecutionResult by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "AUTOMATION CENTER",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Autonomous UI Interaction & Scripting Engine",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Accessibility Service Status Banner
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            hasCrimsonGlow = !isAccessibilityActive,
            contentPadding = 16.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Accessibility Engine", color = TextWhitePrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (isAccessibilityActive) "ONLINE · FULL AGENT CONTROL" else "OFFLINE · SERVICE DISABLED",
                            color = if (isAccessibilityActive) StatusSuccess else CrimsonRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    GlassButton(
                        text = if (isAccessibilityActive) "Configured" else "Enable",
                        icon = Icons.Default.Settings,
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        isPrimary = !isAccessibilityActive
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Navigation Direct Controls
        GlassSettingsPanel(title = "System Gesture Automation") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassButton(
                    text = "Back",
                    icon = Icons.Default.ArrowBack,
                    onClick = { RedoxAccessibilityService.instance?.triggerGlobalBack() },
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Home",
                    icon = Icons.Default.Home,
                    onClick = { RedoxAccessibilityService.instance?.triggerGlobalHome() },
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Notifs",
                    icon = Icons.Default.Notifications,
                    onClick = { RedoxAccessibilityService.instance?.triggerGlobalNotifications() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Action Tester
        GlassSettingsPanel(title = "Automated UI Interaction Tester") {
            Text(
                text = "Simulate autonomous click on any visible text or button:",
                color = TextGraySecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassTextField(
                    value = clickTargetText,
                    onValueChange = { clickTargetText = it },
                    placeholder = "Target button text (e.g., 'Install')",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                GlassButton(
                    text = "Click",
                    icon = Icons.Default.TouchApp,
                    onClick = {
                        if (clickTargetText.isNotBlank()) {
                            viewModel.executeDirectAction(DeviceAction.ClickElement(clickTargetText)) { res ->
                                lastExecutionResult = res
                            }
                        }
                    },
                    isPrimary = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Simulate autonomous typing into active text field:",
                color = TextGraySecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassTextField(
                    value = typeTextContent,
                    onValueChange = { typeTextContent = it },
                    placeholder = "Text to enter",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                GlassButton(
                    text = "Type",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        if (typeTextContent.isNotBlank()) {
                            viewModel.executeDirectAction(DeviceAction.TypeText(typeTextContent)) { res ->
                                lastExecutionResult = res
                            }
                        }
                    },
                    isPrimary = true
                )
            }
        }

        // Execution feedback
        if (lastExecutionResult.isNotBlank()) {
            GlassSettingsPanel(title = "Execution Output") {
                Text(
                    text = lastExecutionResult,
                    color = TextWhitePrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
