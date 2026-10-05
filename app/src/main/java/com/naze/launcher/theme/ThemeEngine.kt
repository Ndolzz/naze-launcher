package com.naze.launcher.theme

import androidx.compose.ui.graphics.Color
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition

/**
 * A resolved ambience: a small, deliberately restrained palette (not a full Material
 * scheme swap) so the launcher always reads as "premier OS surface", never "reskinned
 * per weather like a game". Every input degrades gracefully:
 *   - dynamicWeather off / temperature off / time off -> all resolve to the same
 *     constant NAZE ambience (the palette below no longer reacts to inputs at all).
 *
 * Brand rule: the entire palette family lives on the deep ink / night blues with a
 * blue -> violet -> pink accent gradient. Dark surfaces are deep desaturated navy
 * (never pure black, so depth survives), and the accent is used sparingly —
 * it never floods the screen.
 */
data class Ambience(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val onBackground: Color,
    val accent: Color,
    val isDark: Boolean,
    /** Deep glow used by the ambient background's lower-left haze. */
    val glowA: Color = Color(0xFF141A44),
    /** Deep glow used by the ambient background's lower-right haze. */
    val glowB: Color = Color(0xFF2A1A5E),
    /** The signature blue -> violet -> pink accent gradient. */
    val accentGradient: List<Color> = listOf(
        Color(0xFF4F7CFF),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
) {
    /** Translucent surface derived from the text color — used by cards and sheets. */
    val surface: Color get() = onBackground.copy(alpha = 0.06f)

    /** Slightly stronger surface for elements sitting on top of [surface]. */
    val surfaceHigh: Color get() = onBackground.copy(alpha = 0.10f)

    /** Hairline outline color for subtle borders. */
    val outline: Color get() = onBackground.copy(alpha = 0.14f)
}

object ThemeEngine {

    /**
     * The single Naze night palette, matching the design mockup: an ink-to-night
     * vertical backdrop, off-white text, and the blue/violet/pink accent family.
     *
     * The launcher used to re-theme itself from weather, temperature and time of
     * day; that "wallpaper follows the weather" plan was dropped, so resolve() now
     * returns this constant ambience and simply ignores its inputs. The parameters
     * are kept so existing call sites compile unchanged.
     */
    private val NAZE = Ambience(
        backgroundTop = Color(0xFF0B1020),
        backgroundBottom = Color(0xFF050510),
        onBackground = Color(0xFFF4F6FF),
        accent = Color(0xFF8B5CF6),
        isDark = true
    )

    fun resolve(
        timeOfDay: TimeOfDay,
        condition: WeatherCondition,
        temperatureBand: TemperatureBand,
        dynamicWeatherEnabled: Boolean,
        dynamicTemperatureEnabled: Boolean,
        dynamicTimeEnabled: Boolean
    ): Ambience = NAZE
}
