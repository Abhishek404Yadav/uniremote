package com.sony.braviaremote.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sony.braviaremote.ui.theme.DarkBorder
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.TextPrimary

@Composable
fun RemoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null,
    backgroundColor: Color = DarkCard,
    contentColor: Color = TextPrimary,
    borderColor: Color = DarkBorder,
    shape: Shape = CircleShape,
    size: Dp = 56.dp,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1f, label = "buttonScale")

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(shape)
            .background(if (isPressed) backgroundColor.copy(alpha = 0.8f) else backgroundColor)
            .border(1.dp, if (isPressed) contentColor.copy(alpha = 0.5f) else borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null && text != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text ?: "Button",
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
