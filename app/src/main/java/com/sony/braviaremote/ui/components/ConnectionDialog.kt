package com.sony.braviaremote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.sony.braviaremote.ui.theme.AppIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sony.braviaremote.model.TvDevice
import com.sony.braviaremote.ui.theme.AccentBlue
import com.sony.braviaremote.ui.theme.DarkBorder
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.DarkSurface
import com.sony.braviaremote.ui.theme.StatusGreen
import com.sony.braviaremote.ui.theme.TextMuted
import com.sony.braviaremote.ui.theme.TextPrimary
import com.sony.braviaremote.ui.theme.TextSecondary

@Composable
fun ConnectionDialog(
    currentIp: String,
    currentPsk: String,
    isScanning: Boolean,
    discoveredDevices: List<TvDevice>,
    onScan: () -> Unit,
    onSaveAndConnect: (ip: String, psk: String) -> Unit,
    onDismiss: () -> Unit
) {
    var ipInput by remember { mutableStateOf(currentIp) }
    var pskInput by remember { mutableStateOf(if (currentPsk.isBlank()) "1234" else currentPsk) }
    var showInstructions by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "scanRotate")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TV Connection Setup",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sony BRAVIA (KD-32W830K)",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = AppIcons.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Wi-Fi Auto-Discovery Button
                item {
                    Button(
                        onClick = onScan,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCard,
                            contentColor = AccentBlue
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Icon(
                            imageVector = AppIcons.Refresh,
                            contentDescription = "Scan",
                            modifier = Modifier
                                .size(20.dp)
                                .then(if (isScanning) Modifier.rotate(rotation) else Modifier)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScanning) "Scanning Wi-Fi Network…" else "Scan for Sony TVs Automatically",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Discovered TVs List
                if (discoveredDevices.isNotEmpty()) {
                    item {
                        Text(
                            text = "Found on your Wi-Fi:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                    items(discoveredDevices) { device ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCard)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    ipInput = device.ipAddress
                                    if (device.psk.isNotBlank()) pskInput = device.psk
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AppIcons.Tv,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.friendlyName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = device.ipAddress,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (ipInput == device.ipAddress) {
                                Icon(
                                    imageVector = AppIcons.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = StatusGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Manual Configuration Section
                item {
                    Text(
                        text = "Or Enter IP Manually",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                item {
                    OutlinedTextField(
                        value = ipInput,
                        onValueChange = { ipInput = it.trim() },
                        label = { Text("TV IP Address (e.g. 192.168.1.50)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = pskInput,
                        onValueChange = { pskInput = it.trim() },
                        label = { Text("Pre-Shared Key (Optional / Legacy TVs only)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // TV Setup Guide Toggle
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInstructions = !showInstructions }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = AppIcons.HelpOutline,
                            contentDescription = "Help",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showInstructions) "Hide Setup Guide" else "How pairing works on your TV",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue
                        )
                    }

                    AnimatedVisibility(visible = showInstructions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCard)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "✨ Google TV Models (e.g. KD-32W830K):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                            Text(
                                text = "1. Connect Phone & TV to the same Wi-Fi.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "2. Enter TV IP and tap 'Pair & Connect TV'.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "3. A 6-character code will appear on your TV screen. Enter it here to pair—just like Google TV remote!",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📺 Legacy Sony Bravia Models:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Settings -> Network -> IP Control -> PSK: 1234, Simple IP Control: ON.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Connect & Save Button
                item {
                    Button(
                        onClick = {
                            if (ipInput.isNotBlank()) {
                                onSaveAndConnect(ipInput, pskInput)
                                onDismiss()
                            }
                        },
                        enabled = ipInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Pair & Connect TV",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
