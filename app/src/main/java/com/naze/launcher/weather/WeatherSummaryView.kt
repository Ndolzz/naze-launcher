package com.naze.launcher.weather

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.NazeIconButton
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.NazeLoader

private fun conditionLabel(c: WeatherCondition): String = when (c) {
    WeatherCondition.CLEAR -> "Clear"
    WeatherCondition.CLOUDY -> "Cloudy"
    WeatherCondition.RAIN -> "Rain"
    WeatherCondition.STORM -> "Storm"
    WeatherCondition.FOG -> "Fog"
    WeatherCondition.SNOW -> "Snow"
    WeatherCondition.UNKNOWN -> "—"
}

fun WeatherCondition.icon(): ImageVector = when (this) {
    WeatherCondition.CLEAR -> NazeIcons.WeatherClear
    WeatherCondition.CLOUDY -> NazeIcons.WeatherCloudy
    WeatherCondition.RAIN -> NazeIcons.WeatherRain
    WeatherCondition.STORM -> NazeIcons.WeatherStorm
    WeatherCondition.FOG -> NazeIcons.WeatherFog
    WeatherCondition.SNOW -> NazeIcons.WeatherSnow
    WeatherCondition.UNKNOWN -> NazeIcons.WeatherUnknown
}

/**
 * Compact, informative weather line under the clock. Every state has a designed
 * presentation: loading (Naze loader), success (vector glyph + temp + condition),
 * and failure — which is honest and actionable: it names the reason and offers a
 * retry, or points to Settings when an API key is missing.
 */
@Composable
fun WeatherSummaryView(
    result: WeatherResult?,
    useFahrenheit: Boolean,
    textColor: Color,
    accent: Color,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (result) {
        null -> Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            NazeLoader(color = textColor.copy(alpha = 0.7f), dotSize = 4.dp)
            Spacer(Modifier.width(8.dp))
            Text("Fetching weather",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = LocalNazeUiFont.current
            )
        }

        is WeatherResult.Unavailable -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                NazeIcons.Info,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.height(16.dp).width(16.dp)
            )
            Spacer(Modifier.width(7.dp))
            val message = when (result.reason) {
                WeatherResult.Reason.NO_API_KEY -> "Add a weather API key in settings"
                WeatherResult.Reason.NO_LOCATION -> "Set a location in settings"
                else -> "Weather unavailable"
            }
            Text(
                message,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontFamily = LocalNazeUiFont.current
            )
            Spacer(Modifier.width(4.dp))
            if (result.reason == WeatherResult.Reason.NO_API_KEY ||
                result.reason == WeatherResult.Reason.NO_LOCATION
            ) {
                NazeIconButton(
                    icon = NazeIcons.ChevronRight,
                    contentDescription = "Open settings",
                    onClick = onOpenSettings,
                    tint = accent,
                    size = 28.dp,
             

       iconSize = 14.dp
                )
            }
 else {
                NazeIconButton(
                    icon = NazeIcons.Refresh,
                    contentDescription = "Retry weather fetch",
                    onClick = onRetry,
                    tint = accent,
                    size = 28.dp,
                    iconSize = 14.dp
                )
            }
        }

        is WeatherResult.Success -> {
            val r = result.reading
            val temp = if (useFahrenheit) (r.temperatureCelsius * 9 / 5 + 32) else r.temperatureCelsius
            val unit = if (useFahrenheit) "°F" else "°C"
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    r.condition.icon(),
                    contentDescription = conditionLabel(r.condition),
                    tint = textColor.copy(alpha = 0.8f),
                    modifier = Modifier.height(17.dp).width(17.dp)
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "${temp.toInt()}$unit",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = LocalNazeUiFont.current
                )
                Text(
                    "  ·  ${conditionLabel(r.condition)}  ·  ${r.locationName}",
                    color = textColor.copy(alpha = 0.62f),
                    fontSize = 14.sp,
                    fontFamily = LocalNazeUiFont.current
                )
                if (r.isFromCache) {
                    Spacer(Modifier.width(6.dp))
                    Text("cached",
                        color = textColor.copy(alpha = 0.38f),
                        fontSize = 11.sp,
                        fontFamily = LocalNazeUiFont.current,
                        modifier = Modifier.padding(top = 1.dp)
                    )
  
 
             }
            }
        }
    }
}
