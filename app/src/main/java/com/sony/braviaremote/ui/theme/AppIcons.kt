package com.sony.braviaremote.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.sony.braviaremote.R

object AppIcons {
    // Standard Material icons from core
    val Settings = Icons.Default.Settings
    val ArrowBack = Icons.Default.ArrowBack
    val Home = Icons.Default.Home
    val Menu = Icons.Default.Menu
    val PlayArrow = Icons.Default.PlayArrow
    val Add = Icons.Default.Add
    val Check = Icons.Default.Check
    val Close = Icons.Default.Close
    val Refresh = Icons.Default.Refresh
    val CheckCircle = Icons.Default.CheckCircle
    val HelpOutline = Icons.Default.Info

    // Native lightweight vector drawables
    val Up: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_arrow_up)
    val Down: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_arrow_down)
    val Left: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_arrow_left)
    val Right: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_arrow_right)
    val Power: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_power)
    val Input: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_input)
    val VolumeOff: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_volume_off)
    val FastRewind: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_fast_rewind)
    val FastForward: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_fast_forward)
    val Pause: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_pause)
    val Remove: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_remove)
    val Tv: ImageVector @Composable get() = ImageVector.vectorResource(R.drawable.ic_tv)
}
