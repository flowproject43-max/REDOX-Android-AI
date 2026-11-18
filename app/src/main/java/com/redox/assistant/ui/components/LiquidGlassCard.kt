package com.redox.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.redox.assistant.ui.theme.GlassBorder
import com.redox.assistant.ui.theme.GlassBorderSubtle
import com.redox.assistant.ui.theme.GlassHighlight
import com.redox.assistant.ui.theme.GlassSurface

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    hasCrimsonGlow: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (hasCrimsonGlow) GlassBorder else GlassBorderSubtle

    Box(
        modifier = modifier
            .shadow(
                elevation = if (hasCrimsonGlow) 12.dp else 6.dp,
                shape = shape,
                ambientColor = if (hasCrimsonGlow) Color(0x33FF1744) else Color(0x22000000),
                spotColor = if (hasCrimsonGlow) Color(0x66FF1744) else Color(0x33000000)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassHighlight,
                        GlassSurface,
                        Color(0x1F0A0A0F)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.8f),
                        borderColor.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}
