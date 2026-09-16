package com.naze.launcher.theme

import androidx.compose.ui.unit.dp

/**
 * NAZE design system — single source of truth for spacing, corner radius and motion.
 * No screen invents its own magic numbers; everything routes through these tokens so
 * the whole launcher stays visually consistent.
 *
 * Documented decisions:
 * - Typeface: the system sans (Roboto) with a strict weight/tracking hierarchy.
 *   Bundling a custom font would add APK weight and a first-frame dependency for a
 *   gain that the weight hierarchy already delivers. Revisit only if brand type
 *   becomes a hard requirement.
 * - Color: deep-blue dark surfaces with an electric-blue / blue-purple accent used
 *   sparingly (chips, highlights, clock colon) — never a fully blue screen.
 */
object NazeSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 40.dp
    val screen = 20.dp
}

object NazeRadius {
    val sm = 12.dp
    val md = 18.dp
    val lg = 26.dp
    val xl = 32.dp
}

object NazeMotion {
    /** Micro interactions: press feedback, toggles. */
    const val MICRO = 130

    /** Standard transitions: chips, fades, small slides. */
    const val FAST = 190

    /** Sheet/overlay entrances. */
    const val LARGE = 300

    /** Ambience cross-fade at full quality. */
    const val AMBIENCE = 900

    /** Scales a duration by the active performance mode (0 = instant). */
    fun scaled(base: Int, mode: PerformanceMode): Int = when (mode) {
        PerformanceMode.BATTERY_SAVER -> 0
        PerformanceMode.PERFORMANCE -> (base * 0.6f).toInt()
        PerformanceMode.BALANCED -> base
    }
}

/**
 * How much visual luxury the launcher allows itself. BALANCED is the default on
 * capable devices; low-end users (or anyone) can drop down without losing function.
 */
enum class PerformanceMode {
    /** Everything on: animated ambience, staggered motion, animated clock digits. */
    BALANCED,

    /** Motion kept, heavy visuals off: static background, simplified transitions. */
    PERFORMANCE,

    /** Minimum cost: instant transitions, static background, no decorative draws. */
    BATTERY_SAVER;

    val animatedBackground: Boolean get() = this == BALANCED
    val staggeredMotion: Boolean get() = this == BALANCED
    val animatedClockDigits: Boolean get() = this != BATTERY_SAVER
    val ambienceCrossfadeMs: Int
        get() = when (this) {
            BATTERY_SAVER -> 0
            PERFORMANCE -> 300
            BALANCED -> NazeMotion.AMBIENCE
        }

    companion object {
        fun fromName(raw: String?): PerformanceMode? =
            raw?.let { runCatching { valueOf(it) }.getOrNull() }
    }
}

/** User-facing theme preference. SYSTEM follows the time-of-day ambience. */
enum class ThemeMode {
    SYSTEM, DARK, LIGHT;

    companion object {
        fun fromName(raw: String?): ThemeMode? =
            raw?.let { runCatching { valueOf(it) }.getOrNull() }
    }
}
