package com.sony.braviaremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.sony.braviaremote.ui.theme.AppIcons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sony.braviaremote.ui.theme.AccentBlue
import com.sony.braviaremote.ui.theme.DarkBorder
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.DarkSurface
import com.sony.braviaremote.ui.theme.TextPrimary

@Composable
fun DPad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DarkCard,
                        DarkSurface
                    )
                )
            )
            .border(2.dp, DarkBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // UP Button
        RemoteButton(
            onClick = onUp,
            icon = AppIcons.Up,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 54.dp,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 8.dp)
        )

        // DOWN Button
        RemoteButton(
            onClick = onDown,
            icon = AppIcons.Down,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 54.dp,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
        )

        // LEFT Button
        RemoteButton(
            onClick = onLeft,
            icon = AppIcons.Left,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 54.dp,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp)
        )

        // RIGHT Button
        RemoteButton(
            onClick = onRight,
            icon = AppIcons.Right,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 54.dp,
            enabled = enabled,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-8).dp)
        )

        // CENTER OK Button
        RemoteButton(
            onClick = onCenter,
            text = "OK",
            contentColor = Color.White,
            backgroundColor = AccentBlue,
            borderColor = AccentBlue.copy(alpha = 0.5f),
            size = 64.dp,
            enabled = enabled,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
