package com.naze.launcher.core

/**
 * Naze performance policy. Every visual effect in the launcher consults the current
 * mode, so the UI degrades gracefully on low-end devices and in battery saver —
 * never trading responsiveness for visuals.
 *
 * PERFORMANCE    — fastest: static background, no staggered entrances, shortened motion.
 * BALANCED       — default full experience (ambient light movement, staggered motion).
 * BATTERY_SAVER  — most conservative: no glow layer at all, minimal motion.
 */
enum class PerformanceMode(val motionScale: Float) {
    PERFORMANCE(0.65f),
    BALANCED(1.0f),
    BATTERY_SAVER(0.5f);

    /** Whether the ambient background glow is allowed to drift slowly. */
    val animatesAmbient: Boolean get() = this == BALANCED

    /** Whether the soft accent glow layer is drawn at all. */
    val showsAmbientGlow: Boolean get() = this != BATTERY_SAVER

    /** Whether list/drawer items enter with a staggered fade+scale. */
    val staggersEntrances: Boolean get() = this == BALANCED

    /** Whether the clock animates digit transitions. */
    val animatesClockDigits: Boolean get() = this != BATTERY_SAVER

    companion object {
        fun fromName(raw: String?): PerformanceMode =
            raw?.let { runCatching { valueOf(it) }.getOrNull() } ?: BALANCED
    }
}

/**
 * Naze motion system — one place that defines every duration used anywhere in the app.
 *
 *   micro interaction  100–160 ms
 *   normal transition  180–250 ms
 *   large transition   250–350 ms
 *
 * Durations are additionally scaled by the active [PerformanceMode] so low-end
 * devices get snappier (shorter) transitions instead of dropped frames.
 */
object NazeMotion {

    fun micro(mode: PerformanceMode): Int = (130 * mode.motionScale).toInt().coerceAtLeast(60)

    fun normal(mode: PerformanceMode): Int = (210 * mode.motionScale).toInt().coerceAtLeast(90)

    fun large(mode: PerformanceMode): Int = (300 * mode.motionScale).toInt().coerceAtLeast(120)

    /** Color cross-fade for ambience/theme changes — slower than UI motion by design. */
    fun ambience(mode: PerformanceMode): Int = (500 * mode.motionScale).toInt().coerceAtLeast(150)
}
