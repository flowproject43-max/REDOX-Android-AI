package com.redox.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.redox.assistant.domain.model.OrbState
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.DeepCrimson
import com.redox.assistant.ui.theme.ElectricRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedEnergyOrb(
    state: OrbState,
    amplitude: Float = 0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val transition = rememberInfiniteTransition(label = "OrbTransitions")

    // Slow ambient rotation
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2000
                    OrbState.SPEAKING -> 4000
                    OrbState.LISTENING -> 6000
                    OrbState.ERROR -> 1500
                    OrbState.IDLE -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val pulseScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.LISTENING -> 800
                    OrbState.SPEAKING -> 600
                    OrbState.THINKING -> 1000
                    OrbState.ERROR -> 400
                    OrbState.IDLE -> 2400
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Reverse ring rotation
    val reverseRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2500
                    OrbState.SPEAKING -> 5000
                    else -> 14000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "reverseRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.45f

            // Dynamic scale with amplitude for speech/listening
            val dynamicRadius = baseRadius * pulseScale * (1f + amplitude * 0.35f)

            val coreColor = when (state) {
                OrbState.ERROR -> Color(0xFFFF1111)
                OrbState.THINKING -> ElectricRed
                OrbState.SPEAKING -> CrimsonRed
                OrbState.LISTENING -> CrimsonRed
                OrbState.IDLE -> DeepCrimson
            }

            // 1. Outer Glow Diffusion
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = if (state == OrbState.IDLE) 0.3f else 0.55f),
                        coreColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 2.2f
                ),
                radius = dynamicRadius * 2.2f,
                center = center
            )

            // 2. Rotating Outer HUD Ring (Clockwise)
            rotate(rotationAngle, center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            CrimsonRed,
                            Color.Transparent,
                            Color.White.copy(alpha = 0.6f),
                            CrimsonRed.copy(alpha = 0.2f),
                            CrimsonRed
                        ),
                        center = center
                    ),
                    radius = dynamicRadius * 1.55f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f)
                    )
                )
            }

            // 3. Counter-Rotating Inner Tech Ring
            rotate(reverseRotation, center) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = dynamicRadius * 1.3f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                    )
                )

                // 4 Orbiting Holographic Nodes
                for (i in 0 until 4) {
                    val angle = Math.toRadians((i * 90.0) + reverseRotation)
                    val nodeX = center.x + (dynamicRadius * 1.3f * cos(angle)).toFloat()
                    val nodeY = center.y + (dynamicRadius * 1.3f * sin(angle)).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 4. Central Liquid Energy Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        CrimsonRed,
                        DeepCrimson,
                        Color(0xFF2B0008)
                    ),
                    center = center.copy(
                        x = center.x - dynamicRadius * 0.15f,
                        y = center.y - dynamicRadius * 0.15f
                    ),
                    radius = dynamicRadius
                ),
                radius = dynamicRadius,
                center = center
            )

            // 5. Glass Reflection Specular Arc
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    startY = center.y - dynamicRadius,
                    endY = center.y
                ),
                radius = dynamicRadius * 0.85f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
