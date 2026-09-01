package com.naze.launcher.weather

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun conditionLabel(c: WeatherCondition): String = when (c) {
    WeatherCondition.CLEAR -> "Cerah"
    WeatherCondition.CLOUDY -> "Berawan"
    WeatherCondition.RAIN -> "Hujan"
    WeatherCondition.STORM -> "Badai"
    WeatherCondition.FOG -> "Berkabut"
    WeatherCondition.SNOW -> "Bersalju"
    WeatherCondition.UNKNOWN -> "-"
}

private fun conditionIcon(c: WeatherCondition): String = when (c) {
    WeatherCondition.CLEAR -> "☀"
    WeatherCondition.CLOUDY -> "☁"
    WeatherCondition.RAIN -> "🌧"
    WeatherCondition.STORM -> "⛈"
    WeatherCondition.FOG -> "🌫"
    WeatherCondition.SNOW -> "❄"
    WeatherCondition.UNKNOWN -> "—"
}

@Composable
fun WeatherSummaryView(
    result: WeatherResult?,
    useFahrenheit: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    when (result) {
        null -> Text("Memuat cuaca…", color = textColor.copy(alpha = 0.6f), fontSize = 15.sp, modifier = modifier)
        is WeatherResult.Unavailable -> Text(
            text = "Weather unavailable",
            color = textColor.copy(alpha = 0.55f),
            fontSize = 15.sp,
            modifier = modifier
        )
        is WeatherResult.Success -> {
            val r = result.reading
            val temp = if (useFahrenheit) (r.temperatureCelsius * 9 / 5 + 32) else r.temperatureCelsius
            val unit = if (useFahrenheit) "°F" else "°C"
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Text(r.locationName, color = textColor.copy(alpha = 0.85f), fontSize = 15.sp)
                Text(
                    " · ${temp.toInt()}$unit · ${conditionIcon(r.condition)} ${conditionLabel(r.condition)}",
                    color = textColor.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
                if (r.isFromCache) {
                    Text(" (offline)", color = textColor.copy(alpha = 0.4f), fontSize = 12.sp)
                }
            }
        }
    }
}
