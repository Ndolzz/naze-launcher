package com.naze.launcher.theme

import androidx.compose.ui.graphics.Color
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition

/**
 * A resolved ambience: a small, deliberately restrained palette (not a full Material
 * scheme swap) so the launcher always reads as "premium OS surface", never "reskinned
 * per weather like a game". Every input degrades gracefully:
 *   - dynamicWeather off  -> always use a NEUTRAL condition contribution
 *   - dynamicTemperature off -> always NORMAL band
 *   - dynamicTime off -> always AFTERNOON-equivalent neutrality
 */
data class Ambience(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val onBackground: Color,
    val accent: Color,
    val isDark: Boolean
)

object ThemeEngine {

    fun resolve(
        timeOfDay: TimeOfDay,
        condition: WeatherCondition,
        temperatureBand: TemperatureBand,
        dynamicWeatherEnabled: Boolean,
        dynamicTemperatureEnabled: Boolean,
        dynamicTimeEnabled: Boolean
    ): Ambience {
        val effectiveTime = if (dynamicTimeEnabled) timeOfDay else TimeOfDay.AFTERNOON
        val effectiveCondition = if (dynamicWeatherEnabled) condition else WeatherCondition.UNKNOWN
        val effectiveBand = if (dynamicTemperatureEnabled) temperatureBand else TemperatureBand.NORMAL

        val base = baseForTime(effectiveTime)
        val weatherAdjusted = applyWeather(base, effectiveCondition)
        val finalAmbience = applyTemperature(weatherAdjusted, effectiveBand)

        return finalAmbience
    }

    private fun baseForTime(time: TimeOfDay): Ambience = when (time) {
        TimeOfDay.MORNING -> Ambience(
            backgroundTop = Color(0xFFFFE9D6),
            backgroundBottom = Color(0xFFFFF6EC),
            onBackground = Color(0xFF2B2118),
            accent = Color(0xFFFF9D4D),
            isDark = false
        )
        TimeOfDay.AFTERNOON -> Ambience(
            backgroundTop = Color(0xFFEAF3FF),
            backgroundBottom = Color(0xFFF7FAFF),
            onBackground = Color(0xFF16202B),
            accent = Color(0xFF3E8BFF),
            isDark = false
        )
        TimeOfDay.EVENING -> Ambience(
            backgroundTop = Color(0xFFFFDCC2),
            backgroundBottom = Color(0xFF3B3050),
            onBackground = Color(0xFF1C1626),
            accent = Color(0xFFFF7A59),
            isDark = false
        )
        TimeOfDay.NIGHT -> Ambience(
            backgroundTop = Color(0xFF0E1420),
            backgroundBottom = Color(0xFF060A12),
            onBackground = Color(0xFFEDEFF3),
            accent = Color(0xFF6E8CFF),
            isDark = true
        )
    }

    private fun applyWeather(base: Ambience, condition: WeatherCondition): Ambience = when (condition) {
        WeatherCondition.CLEAR -> base.copy(
            backgroundTop = lighten(base.backgroundTop, 0.06f),
            accent = warm(base.accent)
        )
        WeatherCondition.CLOUDY -> base.copy(
            backgroundTop = desaturate(base.backgroundTop, 0.85f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.9f)
        )
        WeatherCondition.RAIN -> base.copy(
            backgroundTop = cool(base.backgroundTop),
            backgroundBottom = cool(base.backgroundBottom),
            accent = Color(0xFF6FA8DC)
        )
        WeatherCondition.STORM -> base.copy(
            backgroundTop = darken(base.backgroundTop, 0.25f),
            backgroundBottom = darken(base.backgroundBottom, 0.35f),
            onBackground = if (base.isDark) base.onBackground else Color(0xFF20242C),
            isDark = true
        )
        WeatherCondition.FOG -> base.copy(
            backgroundTop = desaturate(lighten(base.backgroundTop, 0.04f), 0.7f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.75f)
        )
        WeatherCondition.SNOW -> base.copy(
            backgroundTop = lighten(desaturate(base.backgroundTop, 0.6f), 0.08f),
            accent = Color(0xFF8FB8FF)
        )
        WeatherCondition.UNKNOWN -> base
    }

    private fun applyTemperature(base: Ambience, band: TemperatureBand): Ambience = when (band) {
        TemperatureBand.LOW -> base.copy(
            backgroundTop = cool(base.backgroundTop),
            backgroundBottom = cool(base.backgroundBottom)
        )
        TemperatureBand.HIGH -> base.copy(
            backgroundTop = warm(base.backgroundTop),
            accent = warm(base.accent)
        )
        TemperatureBand.NORMAL -> base
    }

    // --- Small, intentionally subtle color-math helpers (never extreme shifts) ---

    private fun lighten(c: Color, amount: Float): Color =
        Color(
            red = (c.red + amount).coerceIn(0f, 1f),
            green = (c.green + amount).coerceIn(0f, 1f),
            blue = (c.blue + amount).coerceIn(0f, 1f),
            alpha = c.alpha
        )

    private fun darken(c: Color, amount: Float): Color = lighten(c, -amount)

    private fun desaturate(c: Color, factor: Float): Color {
        val gray = (c.red + c.green + c.blue) / 3f
        return Color(
            red = c.red + (gray - c.red) * (1 - factor),
            green = c.green + (gray - c.green) * (1 - factor),
            blue = c.blue + (gray - c.blue) * (1 - factor),
            alpha = c.alpha
        )
    }

    private fun warm(c: Color): Color = Color(
        red = (c.red + 0.04f).coerceIn(0f, 1f),
        green = c.green,
        blue = (c.blue - 0.03f).coerceIn(0f, 1f),
        alpha = c.alpha
    )

    private fun cool(c: Color): Color = Color(
        red = (c.red - 0.03f).coerceIn(0f, 1f),
        green = c.green,
        blue = (c.blue + 0.04f).coerceIn(0f, 1f),
        alpha = c.alpha
    )
}
