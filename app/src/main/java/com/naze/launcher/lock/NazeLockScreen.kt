package com.naze.launcher.lock

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.core.BatteryStatus
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.theme.Ambience
import com.naze.launcher.theme.LocalNazeDisplayFont
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.theme.ThemeEngine
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/** Brand gradient (blue → violet → pink). Used only on the minute digits, the mark and active states. */
private val BrandBrush = Brush.linearGradient(
    listOf(Color(0xFF4F7CFF), Color(0xFF8B5CF6), Color(0xFFEC4899))
)
private val BrandPink = Color(0xFFEC4899)

/**
 * Naze Lock. Stateless: all data (battery, torch, settings) and all effects
 * (unlock, camera, torch) are passed in, so the screen is trivially previewable.
 *
 * Seconds are only ticked for Analog/Terminal and never in BATTERY_SAVER — in that
 * mode the screen wakes once a minute instead of once a second.
 */
@Composable
fun NazeLockScreen(
    clockStyle: LockClockStyle,
    clock24Hour: Boolean,
    performanceMode: PerformanceMode,
    battery: BatteryStatus?,
    torchOn: Boolean,
    onToggleTorch: () -> Unit,
    onOpenCamera: () -> Unit,
    onCycleClock: () -> Unit,
    onUnlock: () -> Unit
) {
    val ambience = remember {
        ThemeEngine.resolve(
            TimeOfDay.current(), WeatherCondition.UNKNOWN, TemperatureBand.NORMAL,
            dynamicWeatherEnabled = false, dynamicTemperatureEnabled = false, dynamicTimeEnabled = true
        )
    }
    val display = LocalNazeDisplayFont.current
    val ui = LocalNazeUiFont.current

    val showSeconds = clockStyle != LockClockStyle.STACK && performanceMode != PerformanceMode.BATTERY_SAVER
    val nowMs by produceState(System.currentTimeMillis(), showSeconds) {
        val step = if (showSeconds) 1_000L else 60_000L
        while (true) {
            value = System.currentTimeMillis()
            delay(step - value % step)
        }
    }
    val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
    val h24 = cal.get(Calendar.HOUR_OF_DAY)
    val min = cal.get(Calendar.MINUTE)
    val sec = cal.get(Calendar.SECOND)
    val shownHour = if (clock24Hour) h24 else (h24 % 12).let { if (it == 0) 12 else it }
    val dateText = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(cal.time) +
        if (clock24Hour) "" else if (h24 < 12) "  ·  AM" else "  ·  PM"

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(ambience.backgroundTop, ambience.backgroundBottom)))
            .pointerInput(Unit) {
                var total = 0f
                val threshold = 120.dp.toPx()
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onVerticalDrag = { _, dy -> total += dy },
                    onDragEnd = { if (total < -threshold) onUnlock() }
                )
            }
    ) {
        if (performanceMode.showsAmbientGlow) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(ambience.accent.copy(alpha = 0.10f), size.width * 0.75f, Offset(size.width, 0f))
                drawCircle(ambience.accent.copy(alpha = 0.07f), size.width * 0.60f, Offset(0f, size.height))
            }
        }

        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Brand lockup + battery chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(BrandBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N", color = Color.White, fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "NAZE", color = ambience.onBackground, fontFamily = display,
                    fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 6.sp
                )
                Spacer(Modifier.weight(1f))
                if (battery != null) {
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(ambience.surfaceHigh)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(NazeIcons.Battery, null, tint = ambience.onBackground.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${battery.levelPercent}%" + if (battery.charging) "  ·  charging" else "",
                            color = ambience.onBackground.copy(alpha = 0.85f), fontFamily = ui,
                            fontWeight = FontWeight.Medium, fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Tap the clock to cycle styles; the choice is persisted (Settings → Clock).
            Box(Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onCycleClock() 
}) }) {
                when (clockStyle) {
                    LockClockStyle.STACK -> StackClock("%02d".format(shownHour), "%02d".format(min), ambience.onBackground, display)
                    LockClockStyle.ANALOG -> AnalogClock(h24, min, sec, showSeconds, ambience)
                    LockClockStyle.TERMINAL -> TerminalClock(shownHour, min, sec, showSeconds, ambience, performanceMode.animatesAmbient)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                dateText, color = ambience.onBackground.copy(alpha = 0.8f), fontFamily = ui,
                fontWeight = FontWeight.Medium, fontSize = 17.sp
            )

            Spacer(Modifier.weight(1f))

            // Real shortcuts + unlock affordance (also tappable, for accessibility).
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                RoundAction(NazeIcons.Flash, "Flashlight", torchOn, ambience, onToggleTorch)
                Column(
                    Modifier.weight(1f)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onUnlock)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.width(56.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(BrandBrush))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Swipe up to unlock", color = ambience.onBackground.copy(alpha = 0.6f),
                        fontFamily = ui, fontSize = 12.sp, letterSpacing = 0.4.sp
                    )
                }
                RoundAction(NazeIcons.Camera, "Camera", false, ambience, onOpenCamera)
            }
        }
    }
}

@Composable
private fun StackClock(hh: String, mm: String, color: Color, family: FontFamily) {
    val base = TextStyle(
        fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 148.sp,
        lineHeight = 128.sp, letterSpacing = (-4).sp, fontFeatureSettings = "tnum"
    )
    Column {
        Text(hh, style = base.copy(color = color))
        Text(mm, style = base.copy(brush = BrandBrush))
    }
}

@Composable
private fun AnalogClock(h24: Int, min: Int, sec: Int, showSeconds: Boolean, a: Ambience) {
    Canvas(Modifier.size(236.dp)) {
        val c = center
        val r = size.minDimension / 2f
        fun pt(deg: Double, len: Float) = Offset(
            c.x + (cos(Math.toRadians(deg - 90)) * len).toFloat(),
            c.y + (sin(Math.toRadians(deg - 90)) * len).toFloat()
        )
        drawCircle(a.outline, r - 1.dp.toPx(), c, style = Stroke(1.5f.dp.toPx()))
        for (i in 0 until 12) {
            val major = i % 3 == 0
            val inner = r - (if (major) 22.dp.toPx() else 16.dp.toPx())
            drawLine(
                a.onBackground.copy(alpha = if (major) 1f else 0.5f),
                pt(i * 30.0, inner), pt(i * 30.0, r - 8.dp.toPx()),
                (if (major) 3.dp else 2.dp).toPx(), StrokeCap.Round
            )
        }
        val hourDeg = ((h24 % 12) + min / 60.0) * 30
        val minDeg = (min + sec / 60.0) * 6
        drawLine(a.onBackground, c, pt(hourDeg, r * 0.45f), 7.dp.toPx(), StrokeCap.Round)
        drawLine(BrandBrush, c, pt(minDeg, r * 0.68f), 5.dp.toPx(), StrokeCap.Round)
        if (showSeconds) {
            drawLine(BrandPink, pt(sec * 6.0 + 180, r * 0.12f), pt(sec * 6.0, r * 0.82f), 2.dp.toPx(), StrokeCap.Round)
        }
        drawCircle(BrandPink, 5.dp.toPx(), c)
    }
}

@Composable
private fun TerminalClock(h: Int, min: Int, sec: Int, showSeconds: Boolean, a: Ambience, blink: Boolean) {
    val mono = FontFamily.Monospace
    val time = if (showSeconds) "%02d:%02d:%02d".format(h, min, sec) else "%02d:%02d".format(h, min)
    val cursorAlpha = if (blink) {
        val transition = rememberInfiniteTransition(label = "cursor")
        val 
v by transition.animateFloat(
            1f, 0f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "cursorAlpha"
        )
        v
    } else 1f
    Column {
        Text("naze@lock ~ \$ date", color = a.onBackground.copy(alpha = 0.6f), fontFamily = mono, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                time, color = a.onBackground, fontFamily = mono,
                fontWeight = FontWeight.Medium, fontSize = 52.sp, letterSpacing = (-2).sp
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(width = 14.dp, height = 40.dp).alpha(cursorAlpha).background(BrandBrush))
        }
    }
}

@Composable
private fun RoundAction(icon: ImageVector, label: String, active: Boolean, a: Ambience, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp).clip(CircleShape)
            .background(if (active) BrandBrush else SolidColor(a.surfaceHigh))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, label, modifier = Modifier.size(22.dp),
            tint = if (active) Color.White else a.onBackground
        )
    }
}