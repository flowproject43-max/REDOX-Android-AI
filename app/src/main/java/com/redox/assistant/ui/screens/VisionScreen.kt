package com.redox.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.ui.components.GlassButton
import com.redox.assistant.ui.components.GlassSettingsPanel
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextGraySecondary
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel
import kotlinx.coroutines.launch

@Composable
fun VisionScreen(
    viewModel: RedoxViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var screenAnalysisText by remember { mutableStateOf("No screen capture analyzed yet. Tap below to inspect active UI hierarchy.") }
    var isAnalyzing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .padding(bottom = 75.dp)
    ) {
        Text(
            text = "VISION & SCREEN INTELLIGENCE",
            color = CrimsonRed,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Multimodal OCR, Layout Perception & AI Vision",
            color = TextGrayMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Column {
                GlassButton(
                    text = if (isAnalyzing) "Scanning Active Screen..." else "Analyze Active Screen",
                    icon = Icons.Default.DocumentScanner,
                    onClick = {
                        isAnalyzing = true
                        scope.launch {
                            val context = viewModel.visionManager.captureScreenContext()
                            screenAnalysisText = context
                            isAnalyzing = false
                            // Feed to AI
                            viewModel.processUserPrompt("Analyze this screen and tell me what is currently displayed.", context)
                        }
                    },
                    isPrimary = true,
                    enabled = !isAnalyzing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlassButton(
                    text = "Launch Camera Vision",
                    icon = Icons.Default.CameraAlt,
                    onClick = {
                        viewModel.executeDirectAction(com.redox.assistant.domain.model.DeviceAction.OpenApp("Camera"))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen Hierarchy Output
        GlassSettingsPanel(title = "Visible UI Hierarchy & OCR Stream") {
            Text(
                text = screenAnalysisText,
                color = TextWhitePrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
