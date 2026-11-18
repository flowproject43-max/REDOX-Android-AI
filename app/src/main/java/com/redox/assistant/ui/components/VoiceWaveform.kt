package com.redox.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.ElectricRed
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    amplitude: Float,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    barCount: Int = 24
) {
    val transition = rememberInfiniteTransition(label = "waveTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val barWidth = (width / barCount) * 0.45f
        val spacing = width / barCount

        val gradient = Brush.verticalGradient(
            colors = listOf(
                ElectricRed,
                CrimsonRed,
                Color(0x33FF1744)
            )
        )

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val wave = if (isSpeaking) {
                (sin(progress * 4 * Math.PI + phase).toFloat() + 1f) / 2f
            } else {
                (sin(progress * 2 * Math.PI + phase).toFloat() + 1f) / 2f
            }

            val dynamicHeight = if (amplitude > 0.05f || isSpeaking) {
                val amp = if (isSpeaking) 0.85f else amplitude
                (canvasHeight * 0.15f) + (canvasHeight * 0.75f * wave * amp)
            } else {
                canvasHeight * 0.08f
            }

            val x = i * spacing + (spacing - barWidth) / 2f
            val y = (canvasHeight - dynamicHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, dynamicHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
