package com.naze.launcher.shade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.core.BatteryStatus
import com.naze.launcher.notifications.NazeNotification
import com.naze.launcher.theme.LocalNazeDisplayFont
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.pressScale
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// SPEC 2 — the constant Naze palette; no screen-local colors outside these tokens.
private val Ink = Color(0xFF0B1020)
private val Mist = Color(0xFFB8C4FF)
private val White = Color(0xFFF4F6FF)
private val Line = Color(0xFF252D5C)
private val Gradient = listOf(Color(0xFF4F7CFF), Color(0xFF8B5CF6), Color(0xFFEC4899))

/**
 * SPEC 4.3 — the Naze quick shade:
 *
 *   header   big time + date + battery
 *   tiles    torch (real), Wi-Fi panel, Bluetooth panel, wallpaper picker, settings
 *   slider   real brightness control
 *   list     real notifications via NotificationListenerService, with
 *            per-item dismiss, clear-all and an honest "enable access" state.
 */
@Composable
fun QuickShadeScreen(
    battery: BatteryStatus?,
    torchOn: Boolean,
    notifications: List<NazeNotification>,
    notificationAccessGranted: Boolean,
    brightnessPercent: Int,
    onBrightnessChange: (Int) -> Unit,
    onToggleTorch: () -> Unit,
    onOpenWifiPanel: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onDismissNotification: (String) -> Unit,
    onDismissAll: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        ShadeHeader(battery = battery)

        Spacer(Modifier.height(22.dp))

        // ── Quick-setting tiles ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShadeTile(NazeIcons.Flash, "Senter", active = torchOn, onClick = onToggleTorch)
            ShadeTile(NazeIcons.Wifi, "Wi-Fi", active = false, onClick = onOpenWifiPanel)
            ShadeTile(NazeIcons.Bluetooth, "Bluetooth", active = false, onClick = onOpenBluetooth)
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShadeTile(NazeIcons.Wallpaper, "Wallpaper", active = false, onClick = onOpenWallpaperPicker)
            ShadeTile(NazeIcons.Settings, "Setelan", active = false, onClick = onOpenSettings)
        }

        Spacer(Modifier.height(22.dp))

        // ── Brightness ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(White.copy(alpha = 0.06f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Icon(
                NazeIcons.Battery,
                contentDescription = null,
                tint = Mist,
                modifier = Modifier.size(18.dp)
            )
            Slider(
                value = brightnessPercent.toFloat(),
                onValueChange = { onBrightnessChange(it.toInt().coerceIn(20, 100)) },
                valueRange = 20f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF8B5CF6),
                    activeTrackColor = Color(0xFF8B5CF6),
                    inactiveTrackColor = Line
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            Text(
                text = "$brightnessPercent%",
                color = Mist,
                fontSize = 12.sp,
                fontFamily = LocalNazeUiFont.current,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Notifications ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(White.copy(alpha = 0.4f))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "NOTIFIKASI",
                color = White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeDisplayFont.current,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.weight(1f))
            if (notifications.isNotEmpty()) {
                Text(
                    text = "Hapus semua",
                    color = Mist,
                    fontSize = 13.sp,
                    fontFamily = LocalNazeUiFont.current,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDismissAll
                        )
                        .padding(6.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (!notificationAccessGranted) {
            PermissionCard(onOpenNotificationAccess)
        } else if (notifications.isEmpty()) {
            Text(
                text = "Tidak ada notifikasi baru. Notifikasi yang masuk akan muncul di sini.",
                color = White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontFamily = LocalNazeUiFont.current,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                textAlign = TextAlign.Center
            )
        } else {
            notifications.forEach { n ->
                NotificationCard(n, onDismissNotification)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Close handle ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            val interaction = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .size(width = 64.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.linearGradient(Gradient))
                    .pressScale(interaction)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClose)
            )
        }
    }
}

@Composable
private fun ShadeHeader(battery: BatteryStatus?) {
    var time by remember { mutableStateOf(formatShadeTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            time = formatShadeTime()
            delay(15_000L)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = time,
                color = White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LocalNazeDisplayFont.current,
                letterSpacing = (-1).sp
            )
            Text(
                text = shadeDate(),
                color = Mist,
                fontSize = 14.sp,
                fontFamily = LocalNazeUiFont.current
            )
        }
        Spacer(Modifier.weight(1f))
        battery?.let { b ->
            if (b.levelPercent >= 0) {
                Text(
                    text = if (b.charging) "${b.levelPercent}% ⚡" else "${b.levelPercent}%",
                    color = Mist,
                    fontSize = 12.sp,
                    fontFamily = LocalNazeUiFont.current
                )
            }
        }
    }
}

@Composable
private fun ShadeTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .pressScale(interaction, pressedScale = 0.92f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (active) Brush.linearGradient(Gradient) else Brush.linearGradient(
                        listOf(White.copy(alpha = 0.08f), White.copy(alpha = 0.08f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) Color.White else Mist,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = if (active) White else Mist,
            fontSize = 11.sp,
            fontFamily = LocalNazeUiFont.current,
            maxLines = 1
        )
    }
}

@Composable
private fun PermissionCard(onOpenNotificationAccess: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White.copy(alpha = 0.06f))
            .pressScale(interaction, pressedScale = 0.98f)
            .clickable(interactionSource = interaction, indication = null, onClick = onOpenNotificationAccess)
            .padding(18.dp)
    ) {
        Text(
            text = "Notifikasi belum aktif",
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Beri Naze akses notifikasi untuk melihat notifikasi di panel ini. Ketuk untuk membuka setelan.",
            color = Mist,
            fontSize = 13.sp,
            fontFamily = LocalNazeUiFont.current
        )
    }
}

@Composable
private fun NotificationCard(
    n: NazeNotification,
    onDismiss: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(White.copy(alpha = 0.06f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(Gradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = n.appLabel.take(1).uppercase(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LocalNazeDisplayFont.current
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = n.title.ifBlank { n.appLabel },
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
            if (n.text.isNotBlank()) {
                Text(
                    text = n.text,
                    color = Mist,
                    fontSize = 13.sp,
                    fontFamily = LocalNazeUiFont.current,
                    maxLines = 2
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = notificationTime(n.postTimeMillis),
            color = Mist.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontFamily = LocalNazeUiFont.current
        )
        Spacer(Modifier.width(6.dp))
        val interaction = remember { MutableInteractionSource() }
        Icon(
            NazeIcons.Minus,
            contentDescription = "Hapus notifikasi",
            tint = Mist.copy(alpha = 0.6f),
            modifier = Modifier
                .size(18.dp)
                .pressScale(interaction, pressedScale = 0.8f)
                .clickable(interactionSource = interaction, indication = null, onClick = { onDismiss(n.key) })
        )
    }
}

private fun formatShadeTime(): String =
    SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date())

private fun shadeDate(): String =
    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

private fun notificationTime(t: Long): String =
    SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date(t))
