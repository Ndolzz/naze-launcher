package com.naze.launcher.clock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ClockDisplay(val time: String, val date: String)

/**
 * Produces a formatted time/date string that updates every second (or every minute if
 * seconds aren't shown) — isolated in its own small state holder so only this composable
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
            delay(if (showSeconds) 1000L else 1000L * (60 - Calendar.getInstance().get(Calendar.SECOND)))
        }
    }
    return display
}

private fun formatNow(use24Hour: Boolean, showSeconds: Boolean, locale: Locale): ClockDisplay {
    val timePattern = buildString {
        append(if (use24Hour) "HH:mm" else "h:mm")
        if (showSeconds) append(":ss")
        if (!use24Hour) append(" a")
    }
    val time = SimpleDateFormat(timePattern, locale).format(System.currentTimeMillis())
    val date = SimpleDateFormat("EEEE, d MMMM", locale).format(System.currentTimeMillis())
    return ClockDisplay(time, date)
}

@Composable
fun ClockView(
    use24Hour: Boolean,
    showSeconds: Boolean,
    sizeScale: Float,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val display = rememberClockDisplay(use24Hour, showSeconds)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = display.date,
            color = textColor.copy(alpha = 0.75f),
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = display.time,
            color = textColor,
            fontSize = (72 * sizeScale).sp,
            fontWeight = FontWeight.Light
        )
    }
}
