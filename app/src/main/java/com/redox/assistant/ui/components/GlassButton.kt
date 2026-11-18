package com.redox.assistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.DeepCrimson
import com.redox.assistant.ui.theme.GlassBorder
import com.redox.assistant.ui.theme.TextWhitePrimary

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    val shape = RoundedCornerShape(16.dp)

    val backgroundBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                CrimsonRed,
                DeepCrimson
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x33FFFFFF),
                Color(0x1A14141E),
                Color(0x26000000)
            )
        )
    }

    val borderBrush = if (isPrimary) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFFF8099),
                CrimsonRed
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x66FFFFFF),
                GlassBorder
            )
        )
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderBrush, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) Color.White else CrimsonRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = if (enabled) TextWhitePrimary else Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}
