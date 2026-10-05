package com.naze.launcher.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.theme.LocalNazeDisplayFont
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ClockDisplay(val time: String, val date: String)

/**
 * Produces a formatted time/date string that updates every second (or every minute if
 * seconds aren't shown) — isolated in its own small state holder so only
 * this composable recomposes on tick, never the full home screen tree.
 */
@Composable
fun rememberClockDisplay(
    use24Hour: Boolean,
    showSeconds: Boolean,
    locale: Locale = Locale.getDefault()
): ClockDisplay {
    var display by remember { mutableStateOf(formatNow(use24Hour, showSeconds, locale)) }

    LaunchedEffect(use24Hour, showSeconds, locale) {
        while (true) {
            display = formatNow(use24Hour, showSeconds, locale)
            delay(if (showSeconds) 1_000L else 1_000L * (60 - Calendar.getInstance().get(Calendar.SECOND)))
        }
    }
    return display
}

private fun formatNow(use24Hour: Boolean, showSeconds: Boolean, locale: Locale): ClockDisplay {
    val timePattern = buildString {
        append(if (use24Hour) "HH:mm" else "h:mm")
        if (showSeconds) append(":ss")
    }
    val time = SimpleDateFormat(timePattern, locale).format(System.currentTimeMillis())
    val date = SimpleDateFormat("EEEE, d MMMM", locale).format(System.currentTimeMillis())
    return ClockDisplay(time, date)
}

/**
 * The home-screen clock, restyled to match the design mockup: the hours and the
 * minutes sit stacked as two huge bold lines, with the minutes rendered in the
 * signature blue -> violet -> pink gradient.
 *
 *  - Space Grotesk Bold tabular numerals, auto-shrunk to fit any width;
 *  - the whole line rolls vertically with a short slide+fade when it changes
 *    (disabled in BATTERY_SAVER, where digits swap instantly);
 *  - seconds, when enabled, appear as a small quiet line under the stack.
 */
@Composable
fun ClockView(
    use24Hour: Boolean,
    showSeconds: Boolean,
    sizeScale: Float,
    textColor: Color,
    performanceMode: PerformanceMode,
    modifier: Modifier = Modifier,
    minuteGradient: List<Color> = listOf(
        Color(0xFF4F7CFF),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
) {
    val display = rememberClockDisplay(use24Hour, showSeconds)
    val animateDigits = performanceMode.animatesClockDigits
    val displayFont = LocalNazeDisplayFont.current

    val parts = display.time.split(":")
    val hour = parts.getOrNull(0)?.padStart(2, '0') ?: ""
    val minute = parts.getOrNull(1)?.padStart(2, '0') ?: ""
    val seconds = parts.getOrNull(2)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = display.date.uppercase(),
            color = textColor.copy(alpha = 0.60f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = displayFont,
            letterSpacing = 2.5.sp,
            modifier = Modifier.width(340.dp),
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        BoxWithConstraints(modifier = Modifier.width(340.dp), contentAlignment = Alignment.Center) {
            // Auto-fit: two stacked digits must never overflow the available width.
            val maxByWidth = with(LocalDensity.current) { (maxWidth.toSp() / (2 * 0.62f)).value }
            val fontSizeSp = (92f * sizeScale).coerceAtMost(maxByWidth).coerceAtLeast(40f)
            val gradientBrush = Brush.linearGradient(colors = minuteGradient)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RollingLine(
                    value = hour,
                    fontSizeSp = fontSizeSp,
                    textColor = textColor,
                    animate = animateDigits,
                    brush = null,
                    label = "hourLine"
                )
                RollingLine(
                    value = minute,
                    fontSizeSp = fontSizeSp,
                    textColor = textColor,
                    animate = animateDigits,
                    brush = gradientBrush,
                    label = "minuteLine"
                )
                if (seconds != null) {
                    Text(
                        text = seconds,
                        color = textColor.copy(alpha = 0.45f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = displayFont,
                        letterSpacing = 2.sp,
                        modifier = Modifier.width(340.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun RollingLine(
    value: String,
    fontSizeSp: Float,
    textColor: Color,
    animate: Boolean,
    brush: Brush?,
    label: String
) {
    if (!animate) {
        StackText(value, fontSizeSp, textColor, brush)
        return
    }
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically(tween(180, easing = LinearEasing)) { it / 3 } +
                fadeIn(tween(180))).togetherWith(
                slideOutVertically(tween(180, easing = LinearEasing)) { -it / 3 } +
                    fadeOut(tween(140))
            )
        },
        label = label
    ) { line -> StackText(line, fontSizeSp, textColor, brush) }
}

@Composable
private fun StackText(value: String, fontSizeSp: Float, textColor: Color, brush: Brush?) {
    val style = if (brush != null) {
        TextStyle(brush = brush, fontFeatureSettings = "tnum")
    } else {
        TextStyle(color = textColor, fontFeatureSettings = "tnum")
    }
    Text(
        text = value,
        color = Color.Unspecified,
        fontSize = fontSizeSp.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = LocalNazeDisplayFont.current,
        letterSpacing = (-2).sp,
        lineHeight = (fontSizeSp * 0.86f).sp,
        textAlign = TextAlign.Center,
        style = style,
        maxLines = 1
    )
}
