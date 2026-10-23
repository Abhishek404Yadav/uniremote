package com.sony.braviaremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.sony.braviaremote.ui.theme.AppIcons
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sony.braviaremote.ui.theme.DarkBorder
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.TextMuted
import com.sony.braviaremote.ui.theme.TextPrimary

@Composable
fun RockerButton(
    title: String,
    onUp: () -> Unit,
    onDown: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .width(56.dp)
            .height(140.dp)
            .clip(shape)
            .background(DarkCard)
            .border(1.dp, DarkBorder, shape)
            .padding(vertical = 4.dp)
    ) {
        // UP / PLUS
        RemoteButton(
            onClick = onUp,
            icon = AppIcons.Add,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 46.dp,
            enabled = enabled
        )

        // LABEL (e.g. VOL or CH)
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )

        // DOWN / MINUS
        RemoteButton(
            onClick = onDown,
            icon = AppIcons.Remove,
            contentColor = TextPrimary,
            backgroundColor = Color.Transparent,
            borderColor = Color.Transparent,
            size = 46.dp,
            enabled = enabled
        )
    }
}
