package com.naze.launcher.theme

import androidx.compose.ui.graphics.Color
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition

/**
 * A resolved ambience: a deliberately restrained palette (not a full Material scheme
 * swap) so the launcher always reads as a premium OS surface, never a reskinned game.
 *
 * The Naze identity lives here: deep-blue dark surfaces with an electric-blue /
 * blue-purple accent. Every input degrades gracefully:
 *   - dynamicWeather off      -> neutral condition contribution
 *   - dynamicTemperature off  -> NORMAL band
 *   - dynamicTime off         -> AFTERNOON-equivalent neutrality
 *   - ThemeMode.DARK / LIGHT  -> overrides the time-of-day base entirely
 */
data class Ambience(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val surface: Color,
    val onBackground: Color,
    val accent: Color,
    val accentSoft: Color,
    val isDark: Boolean
)

object ThemeEngine {

    fun resolve(
        timeOfDay: TimeOfDay,
        condition: WeatherCondition,
        temperatureBand: TemperatureBand,
        dynamicWeatherEnabled: Boolean,
        dynamicTemperatureEnabled: Boolean,
        dynamicTimeEnabled: Boolean,
        themeMode: ThemeMode = ThemeMode.SYSTEM
    ): Ambience {
        val effectiveTime = when (themeMode) {
            ThemeMode.DARK -> TimeOfDay.NIGHT
            ThemeMode.LIGHT -> TimeOfDay.AFTERNOON
            ThemeMode.SYSTEM -> if (dynamicTimeEnabled) timeOfDay else TimeOfDay.AFTERNOON
        }
        val effectiveCondition = if (dynamicWeatherEnabled) condition else WeatherCondition.UNKNOWN
        val effectiveBand = if (dynamicTemperatureEnabled) temperatureBand else TemperatureBand.NORMAL

        val base = baseForTime(effectiveTime)
        val weatherAdjusted = applyWeather(base, effectiveCondition)
        return applyTemperature(weatherAdjusted, effectiveBand)
    }

    private fun darkAmbience(top: Long, bottom: Long, onBg: Long, accent: Long): Ambience =
        Ambience(
            backgroundTop = Color(top),
            backgroundBottom = Color(bottom),
            surface = Color(0xFF141D33).copy(alpha = 0.72f),
            onBackground = Color(onBg),
            accent = Color(accent),
            accentSoft = Color(accent).copy(alpha = 0.16f),
            isDark = true
        )

    private fun lightAmbience(top: Long, bottom: Long, onBg: Long, accent: Long): Ambience =
        Ambience(
            backgroundTop = Color(top),
            backgroundBottom = Color(bottom),
            surface = Color(0xFFFFFFFF).copy(alpha = 0.72f),
            onBackground = Color(onBg),
            accent = Color(accent),
            accentSoft = Color(accent).copy(alpha = 0.12f),
            isDark = false
        )

    private fun baseForTime(time: TimeOfDay): Ambience = when (time) {
        TimeOfDay.MORNING -> lightAmbience(
            top = 0xFFE9F1FC, bottom = 0xFFFAFCFF, onBg = 0xFF13203A, accent = 0xFF2F7BFF
        )
        TimeOfDay.AFTERNOON -> lightAmbience(
            top = 0xFFE7EFFB, bottom = 0xFFF7FAFF, onBg = 0xFF101B30, accent = 0xFF2E6BFF
        )
        // Evening now resolves to the dark family — dusk is when the deep-blue
        // Naze identity reads best, and it avoids a jarring mid-evening flip.
        TimeOfDay.EVENING -> darkAmbience(
            top = 0xFF101430, bottom = 0xFF070510, onBg = 0xFFE9EAF2, accent = 0xFF8B7BFF
        )
        TimeOfDay.NIGHT -> darkAmbience(
            top = 0xFF0B1226, bottom = 0xFF05080F, onBg = 0xFFE8EEF9, accent = 0xFF5B8CFF
        )
    }

    private fun applyWeather(base: Ambience, condition: WeatherCondition): Ambience = when (condition) {
        WeatherCondition.CLEAR -> base.copy(
            backgroundTop = lighten(base.backgroundTop, 0.05f),
            accent = warm(base.accent),
            accentSoft = warm(base.accent).copy(alpha = base.accentSoft.alpha)
        )
        WeatherCondition.CLOUDY -> base.copy(
            backgroundTop = desaturate(base.backgroundTop, 0.85f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.9f)
        )
        WeatherCondition.RAIN -> base.copy(
            backgroundTop = cool(base.backgroundTop),
            backgroundBottom = cool(base.backgroundBottom),
            accent = Color(0xFF6FA8DC),
            accentSoft = Color(0xFF6FA8DC).copy(alpha = base.accentSoft.alpha)
        )
        WeatherCondition.STORM -> base.copy(
            backgroundTop = darken(base.backgroundTop, 0.25f),
            backgroundBottom = darken(base.backgroundBottom, 0.35f),
            surface = Color(0xFF141D33).copy(alpha = 0.72f),
            onBackground = if (base.isDark) base.onBackground else Color(0xFFE8EEF9),
            isDark = true
        )
        WeatherCondition.FOG -> base.copy(
            backgroundTop = desaturate(lighten(base.backgroundTop, 0.04f), 0.7f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.75f)
        )
        WeatherCondition.SNOW -> base.copy(
            backgroundTop = lighten(desaturate(base.backgroundTop, 0.6f), 0.08f),
            accent = Color(0xFF8FB8FF),
            accentSoft = Color(0xFF8FB8FF).copy(alpha = base.accentSoft.alpha)
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
            accent = warm(base.accent),
            accentSoft = warm(base.accent).copy(alpha = base.accentSoft.alpha)
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

    private fun warm(c: Color): Color =
        Color(
            red = (c.red + 0.04f).coerceIn(0f, 1f),
            green = c.green,
            blue = (c.blue - 0.03f).coerceIn(0f, 1f),
            alpha = c.alpha
        )

    private fun cool(c: Color): Color =
        Color(
            red = (c.red - 0.03f).coerceIn(0f, 1f),
            green = c.green,
            blue = (c.blue + 0.04f).coerceIn(0f, 1f),
            alpha = c.alpha
        )
}
