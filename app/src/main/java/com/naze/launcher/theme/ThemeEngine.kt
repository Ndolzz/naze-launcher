package com.naze.launcher.theme

import androidx.compose.ui.graphics.Color
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition

/**
 * A resolved ambience: a small, deliberately restrained palette (not a full Material
 * scheme swap) so the launcher always reads as "premium OS surface", never "reskinned
 * per weather like a game". Every input degrades gracefully:
 *   - dynamicWeather off      -> always use a NEUTRAL condition contribution
 *   - dynamicTemperature off  -> always NORMAL band
 *   - dynamicTime off         -> always AFTERNOON-equivalent neutrality
 *
 * Brand rule: the entire palette family lives on the blue / electric-blue /
 * deep-blue / blue-purple spectrum. Dark surfaces are deep desaturated navy
 * (never pure black, so depth survives), and the accent is used sparingly —
 * it never floods the screen.
 */
data class Ambience(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val onBackground: Color,
    val accent: Color,
    val isDark: Boolean
) {
    /** Translucent surface derived from the text color — used by cards and sheets. */
    val surface: Color get() = onBackground.copy(alpha = 0.06f)

    /** Slightly stronger surface for elements sitting on top of [surface]. */
    val surfaceHigh: Color get() = onBackground.copy(alpha = 0.10f)

    /** Hairline outline color for subtle borders. */
    val outline: Color get() = onBackground.copy(alpha = 0.14f)
}

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
        // Early light: cool paper-blue, soft indigo accent.
        TimeOfDay.MORNING -> Ambience(
            backgroundTop = Color(0xFFE9EFFB),
            backgroundBottom = Color(0xFFF7FAFE),
            onBackground = Color(0xFF131B2B),
            accent = Color(0xFF3E7BFF),
            isDark = false
        )
        // Day: the designed light mode — airy cool white with electric blue accent.
        TimeOfDay.AFTERNOON -> Ambience(
            backgroundTop = Color(0xFFE4EDFB),
            backgroundBottom = Color(0xFFF6F9FE),
            onBackground = Color(0xFF101827),
            accent = Color(0xFF2F6BFF),
            isDark = false
        )
        // Dusk: blue-purple gradient, the identity moment of the palette.
        TimeOfDay.EVENING -> Ambience(
            backgroundTop = Color(0xFF323B68),
            backgroundBottom = Color(0xFF171C33),
            onBackground = Color(0xFFEAEDF8),
            accent = Color(0xFF8E7BFF),
            isDark = true
        )
        // Night: deep navy (not pure black) so surfaces keep their depth.
        TimeOfDay.NIGHT -> Ambience(
            backgroundTop = Color(0xFF0C1224),
            backgroundBottom = Color(0xFF05080F),
            onBackground = Color(0xFFECEFF7),
            accent = Color(0xFF5B8CFF),
            isDark = true
        )
    }

    private fun applyWeather(base: Ambience, condition: WeatherCondition): Ambience = when (condition) {
        WeatherCondition.CLEAR -> base.copy(
            backgroundTop = lighten(base.backgroundTop, 0.03f)
        )
        WeatherCondition.CLOUDY -> base.copy(
            backgroundTop = desaturate(base.backgroundTop, 0.88f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.92f)
        )
        WeatherCondition.RAIN -> base.copy(
            backgroundTop = cool(base.backgroundTop),
            backgroundBottom = cool(base.backgroundBottom),
            accent = Color(0xFF6FA8DC)
        )
        WeatherCondition.STORM -> base.copy(
            backgroundTop = darken(base.backgroundTop, 0.22f),
            backgroundBottom = darken(base.backgroundBottom, 0.32f),
            onBackground = if (base.isDark) base.onBackground else Color(0xFF20242C),
            isDark = true
        )
        WeatherCondition.FOG -> base.copy(
            backgroundTop = desaturate(lighten(base.backgroundTop, 0.04f), 0.72f),
            backgroundBottom = desaturate(base.backgroundBottom, 0.78f)
        )
        WeatherCondition.SNOW -> base.copy(
            backgroundTop = lighten(desaturate(base.backgroundTop, 0.62f), 0.06f),
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
            backgroundTop = warm(base.backgroundTop)
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
        red = (c.red + 0.03f).coerceIn(0f, 1f),
        green = c.green,
        blue = (c.blue - 0.02f).coerceIn(0f, 1f),
        alpha = c.alpha
    )

    private fun cool(c: Color): Color = Color(
        red = (c.red - 0.02f).coerceIn(0f, 1f),
        green = c.green,
        blue = (c.blue + 0.03f).coerceIn(0f, 1f),
        alpha = c.alpha
    )
}
