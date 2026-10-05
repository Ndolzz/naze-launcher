package com.naze.launcher.clock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.text.TextStyle

data class ClockDisplay(val time: String, val date: String)

/**
 * Produces a formatted time/date string that updates every second (or every minute if
 * seconds aren't shown) — isolated in its own small state holder so only
 this composable
 * recomposes on tick, never the full home screen tree.
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
 * The home-screen clock — the centerpiece of Naze.
 *
 *  - Space Grotesk Light numerals with tabular figures, auto-shrunk to fit any width;
 *  - each digit rolls vertically with a short slide+fade when it changes
 *    (disabled in BATTERY_SAVER, where digits swap instantly);
 *  - the colon breathes very slowly when seconds are hidden — the only "decoration"
 *    the clock gets. Typography *is* the design.
 */
@Composable
fun ClockView(
    use24Hour: Boolean,
    showSeconds: Boolean,
    sizeScale: Float,
    textColor: Color,
    performanceMode: PerformanceMode,
    modifier: Modifier = Modifier
) {
    val display = rememberClockDisplay(use24Hour, showSeconds)
    val animateDigits = performanceMode.animatesClockDigits
    val displayFont = LocalNazeDisplayFont.current

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
            val charCount = display.time.length
            // Auto-fit: never overflow the available width, whatever the scale setting.
            val maxByWidth = with(LocalDensity.current) { (maxWidth.toSp() / (charCount * 0.60f)).value }
            val fontSizeSp = (96f * sizeScale).coerceAtMost(maxByWidth).coerceAtLeast(40f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                display.time.forEachIndexed { index, char ->
                    key(index) {
                        if (char == ':') {
                            Colon(
                                fontSizeSp = fontSizeSp,
                                pulse = animateDigits && !showSeconds,
                                textColor = textColor
                            )
                        } else if (animateDigits) {
                            AnimatedContent(
                                targetState = char,
                                transitionSpec = {
                                    (slideInVertically(tween(180, easing = LinearEasing)) { it / 3 } +
                                        fadeIn(tween(180)))
                                        .togetherWith(
                                        slideOutVertically(tween(180, easing = LinearEasing)) { -it / 3 } +
                                            fadeOut(tween(140))
                                    )
                                },
                                label = "digit$index"
                            ) { digitChar -
>
                                Digit(digitChar, fontSizeSp, textColor)
                            }
                        } else {
                            Digit(char, fontSizeSp, textColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Digit(char: Char, fontSizeSp: Float, textColor: Color) {
    Text(
        text = char.toString(),
        color = textColor,
        fontSize = fontSizeSp.sp,
        fontWeight = FontWeight.Light,
        fontFamily = LocalNazeDisplayFont.current,
        style = TextStyle(fontFeatureSettings = "tnum"),
        textAlign = TextAlign.Center,
        modifier = Modifier.width((fontSizeSp * 0.62f).dp)
    )
}

@Composable
private fun Colon(fontSizeSp: Float, pulse: Boolean, textColor: Color) {
    val alpha = if (pulse) {
        val transition = rememberInfiniteTransition(label = "colon")
        val phase by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(2_200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "colonPhase"
        )
        phase
    } else {
        1f
    }
    Text(
        text = ":",
        color = textColor.copy(alpha = alpha),
        fontSize = fontSizeSp.sp,
        fontWeight = FontWeight.Light,
        fontFamily = LocalNazeDisplayFont.current,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width((fontSizeSp * 0.34f).dp)
            .graphicsLayer { this.alpha = alpha }
    )
}
