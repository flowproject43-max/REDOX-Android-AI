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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.domain.model.AiProvider
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.components.GlassTextField
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.GlassBorder
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.StatusSuccess
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel
import kotlinx.coroutines.launch

@Composable
fun AiProviderSettingsScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val activeProvider by viewModel.activeProvider.collectAsState()
    val scope = rememberCoroutineScope()

    var apiKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    // Load stored key for active provider
    LaunchedEffect(activeProvider) {
        apiKeyInput = viewModel.settingsRepo.getApiKey(activeProvider) ?: ""
        testResultText = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "AI PROVIDER CONFIGURATION",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Multi-Brain Intelligence Routing & API Keystore",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Active Provider Selection
        GlassSettingsPanel(title = "Primary Cognitive Engine") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiProvider.entries.forEach { provider ->
                    val isSelected = provider == activeProvider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x33FF1744) else Color(0x1AFFFFFF))
                            .border(1.dp, if (isSelected) CrimsonRed else GlassBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.setProvider(provider) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = provider.displayName,
                                    color = TextWhitePrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Model: ${provider.defaultModel}",
                                    color = TextGrayMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = CrimsonRed)
                            }
                        }
                    }
                }
            }
        }

        // API Key Storage
        GlassSettingsPanel(title = "${activeProvider.displayName} API Key") {
            Text(
                text = "Encrypted in hardware Android Keystore (AES-256 GCM). Never logged or transmitted outside direct model requests.",
                color = TextGraySecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            GlassTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                placeholder = "Enter your ${activeProvider.displayName} API Key",
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility",
                            tint = TextGraySecondary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassButton(
                    text = "Save Key",
                    icon = Icons.Default.Save,
                    onClick = {
                        viewModel.saveApiKey(activeProvider, apiKeyInput)
                        testResultText = "API key saved securely."
                    },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )

                GlassButton(
                    text = if (isTesting) "Testing..." else "Test Connection",
                    icon = Icons.Default.Bolt,
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            isTesting = true
                            scope.launch {
                                val result = viewModel.aiRepo.testConnection(activeProvider, apiKeyInput)
                                isTesting = false
                                testResultText = if (result.isSuccess) {
                                    "✓ ONLINE — Latency: ${result.getOrNull()} ms"
                                } else {
                                    "✗ FAILED: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        }
                    },
                    enabled = !isTesting && apiKeyInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (testResultText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = testResultText ?: "",
                    color = if (testResultText?.startsWith("✓") == true) StatusSuccess else CrimsonRed,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
