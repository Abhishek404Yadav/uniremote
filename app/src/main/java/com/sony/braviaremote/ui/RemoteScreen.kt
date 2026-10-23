package com.sony.braviaremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.sony.braviaremote.ui.theme.AppIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sony.braviaremote.model.IrccCommand
import com.sony.braviaremote.ui.components.ConnectionDialog
import com.sony.braviaremote.ui.components.DPad
import com.sony.braviaremote.ui.components.NumberPadSheet
import com.sony.braviaremote.ui.components.RemoteButton
import com.sony.braviaremote.ui.components.RockerButton
import com.sony.braviaremote.ui.theme.DarkBackground
import com.sony.braviaremote.ui.theme.DarkBorder
import com.sony.braviaremote.ui.theme.DarkCard
import com.sony.braviaremote.ui.theme.DarkSurface
import com.sony.braviaremote.ui.theme.NetflixRed
import com.sony.braviaremote.ui.theme.PowerRed
import com.sony.braviaremote.ui.theme.PrimeBlue
import com.sony.braviaremote.ui.theme.StatusAmber
import com.sony.braviaremote.ui.theme.StatusGreen
import com.sony.braviaremote.ui.theme.TextMuted
import com.sony.braviaremote.ui.theme.TextPrimary
import com.sony.braviaremote.ui.theme.TextSecondary
import com.sony.braviaremote.ui.theme.YouTubeRed
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RemoteScreen(viewModel: MainViewModel) {
    val activeIp by viewModel.activeIp.collectAsState()
    val activePsk by viewModel.activePsk.collectAsState()
    val tvName by viewModel.tvName.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    var showConnectionDialog by remember { mutableStateOf(false) }
    var showNumberPad by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.messageEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================= TOP HEADER BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TV Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .clickable { showConnectionDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isConnecting -> StatusAmber
                                    isConnected -> StatusGreen
                                    else -> PowerRed
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (activeIp.isNotBlank()) "KD-32W830K" else "No TV Connected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (activeIp.isNotBlank()) activeIp else "Tap to setup Wi-Fi",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Header Action Buttons (Settings & Power)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RemoteButton(
                        onClick = { showConnectionDialog = true },
                        icon = AppIcons.Settings,
                        contentColor = TextSecondary,
                        backgroundColor = DarkSurface,
                        borderColor = DarkBorder,
                        size = 46.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    RemoteButton(
                        onClick = { viewModel.sendCommand(IrccCommand.POWER) },
                        icon = AppIcons.Power,
                        contentColor = Color.White,
                        backgroundColor = PowerRed,
                        borderColor = PowerRed.copy(alpha = 0.6f),
                        size = 48.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= APP SHORTCUTS (YouTube, Netflix, Prime, Input) =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // YouTube
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.YOUTUBE) },
                    text = "YouTube",
                    backgroundColor = DarkSurface,
                    contentColor = YouTubeRed,
                    borderColor = DarkBorder,
                    shape = RoundedCornerShape(12.dp),
                    size = 42.dp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Netflix
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.NETFLIX) },
                    text = "Netflix",
                    backgroundColor = DarkSurface,
                    contentColor = NetflixRed,
                    borderColor = DarkBorder,
                    shape = RoundedCornerShape(12.dp),
                    size = 42.dp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Prime Video
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.PRIME_VIDEO) },
                    text = "Prime",
                    backgroundColor = DarkSurface,
                    contentColor = PrimeBlue,
                    borderColor = DarkBorder,
                    shape = RoundedCornerShape(12.dp),
                    size = 42.dp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // TV Input
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.INPUT) },
                    icon = AppIcons.Input,
                    backgroundColor = DarkSurface,
                    contentColor = TextSecondary,
                    borderColor = DarkBorder,
                    shape = RoundedCornerShape(12.dp),
                    size = 42.dp,
                    modifier = Modifier.weight(0.7f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= SYSTEM NAVIGATION ROW (Back, Home, Menu, 123) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.BACK) },
                    icon = AppIcons.ArrowBack,
                    contentColor = TextPrimary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )

                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.HOME) },
                    icon = AppIcons.Home,
                    contentColor = TextPrimary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )

                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.OPTIONS) },
                    icon = AppIcons.Menu,
                    contentColor = TextPrimary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )

                RemoteButton(
                    onClick = { showNumberPad = true },
                    text = "1 2 3",
                    contentColor = TextPrimary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= D-PAD NAVIGATION =================
            DPad(
                onUp = { viewModel.sendCommand(IrccCommand.UP) },
                onDown = { viewModel.sendCommand(IrccCommand.DOWN) },
                onLeft = { viewModel.sendCommand(IrccCommand.LEFT) },
                onRight = { viewModel.sendCommand(IrccCommand.RIGHT) },
                onCenter = { viewModel.sendCommand(IrccCommand.ENTER) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ================= VOLUME, MUTE & CHANNELS =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // VOLUME ROCKER
                RockerButton(
                    title = "VOL",
                    onUp = { viewModel.sendCommand(IrccCommand.VOLUME_UP) },
                    onDown = { viewModel.sendCommand(IrccCommand.VOLUME_DOWN) }
                )

                // CENTER MUTE & PLAY/PAUSE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RemoteButton(
                        onClick = { viewModel.sendCommand(IrccCommand.MUTE) },
                        icon = AppIcons.VolumeOff,
                        contentColor = TextSecondary,
                        backgroundColor = DarkCard,
                        borderColor = DarkBorder,
                        size = 50.dp
                    )

                    RemoteButton(
                        onClick = { viewModel.sendCommand(IrccCommand.PLAY) },
                        icon = AppIcons.PlayArrow,
                        contentColor = TextPrimary,
                        backgroundColor = DarkCard,
                        borderColor = DarkBorder,
                        size = 50.dp
                    )
                }

                // CHANNEL ROCKER
                RockerButton(
                    title = "CH",
                    onUp = { viewModel.sendCommand(IrccCommand.CHANNEL_UP) },
                    onDown = { viewModel.sendCommand(IrccCommand.CHANNEL_DOWN) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ================= MEDIA TRANSPORT (Rewind, Play, Pause, FastForward) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.REWIND) },
                    icon = AppIcons.FastRewind,
                    contentColor = TextSecondary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )

                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.PAUSE) },
                    icon = AppIcons.Pause,
                    contentColor = TextSecondary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )

                RemoteButton(
                    onClick = { viewModel.sendCommand(IrccCommand.FAST_FORWARD) },
                    icon = AppIcons.FastForward,
                    contentColor = TextSecondary,
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                    size = 44.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Modals
        if (showConnectionDialog) {
            ConnectionDialog(
                currentIp = activeIp,
                currentPsk = activePsk,
                isScanning = isScanning,
                discoveredDevices = discoveredDevices,
                onScan = { viewModel.scanNetwork() },
                onSaveAndConnect = { ip, psk ->
                    viewModel.updateConnection(ip, psk)
                },
                onDismiss = { showConnectionDialog = false }
            )
        }

        if (showNumberPad) {
            NumberPadSheet(
                onDismiss = { showNumberPad = false },
                onSendCommand = { command -> viewModel.sendCommand(command) }
            )
        }
    }
}
