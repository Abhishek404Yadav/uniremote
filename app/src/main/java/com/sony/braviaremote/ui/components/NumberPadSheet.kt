package com.sony.braviaremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import com.sony.braviaremote.ui.theme.AppIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sony.braviaremote.model.IrccCommand
import com.sony.braviaremote.ui.theme.AccentBlue
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.DarkSurface
import com.sony.braviaremote.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberPadSheet(
    onDismiss: () -> Unit,
    onSendCommand: (IrccCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Number Pad",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Rows: 1-3, 4-6, 7-9, Dot-0-Enter
            val rows = listOf(
                listOf(1, 2, 3),
                listOf(4, 5, 6),
                listOf(7, 8, 9)
            )

            for (row in rows) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    for (digit in row) {
                        RemoteButton(
                            onClick = {
                                IrccCommand.fromNumber(digit)?.let(onSendCommand)
                            },
                            text = digit.toString(),
                            backgroundColor = DarkCard,
                            contentColor = TextPrimary,
                            size = 60.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }

            // Bottom Row: Dot, 0, Enter
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                RemoteButton(
                    onClick = { onSendCommand(IrccCommand.DOT) },
                    text = ".",
                    backgroundColor = DarkCard,
                    contentColor = TextPrimary,
                    size = 60.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                RemoteButton(
                    onClick = { onSendCommand(IrccCommand.NUM_0) },
                    text = "0",
                    backgroundColor = DarkCard,
                    contentColor = TextPrimary,
                    size = 60.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                RemoteButton(
                    onClick = {
                        onSendCommand(IrccCommand.ENTER)
                        onDismiss()
                    },
                    icon = AppIcons.Check,
                    backgroundColor = AccentBlue,
                    contentColor = Color.White,
                    size = 60.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
