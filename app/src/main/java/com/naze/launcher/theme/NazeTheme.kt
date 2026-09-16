package com.naze.launcher.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Smooth, GPU-friendly cross-fade between ambiences — never a hard cut.
 * Duration follows the active performance mode (0 ms = instant swap).
 */
@Composable
fun animateAmbience(target: Ambience, performanceMode: PerformanceMode): Ambience {
    val durationMs = performanceMode.ambienceCrossfadeMs
    val top by animateColorAsState(target.backgroundTop, tween(durationMs), label = "bgTop")
    val bottom by animateColorAsState(target.backgroundBottom, tween(durationMs), label = "bgBottom")
    val surface by animateColorAsState(target.surface, tween(durationMs), label = "surface")
    val onBg by animateColorAsState(target.onBackground, tween(durationMs), label = "onBg")
    val accent by animateColorAsState(target.accent, tween(durationMs), label = "accent")
    val accentSoft by animateColorAsState(target.accentSoft, tween(durationMs), label = "accentSoft")
    return Ambience(top, bottom, surface, onBg, accent, accentSoft, target.isDark)
}

/**
 * Typography hierarchy is one of Naze's biggest levers: a single family, five
 * weights, deliberate tracking. Large display for the clock, quiet labels for
 * system information.
 */
val NazeTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 86.sp, letterSpacing = (-2.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Light, fontSize = 52.sp, letterSpacing = (-1.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, letterSpacing = 0.1.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.1.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.9.sp)
)

@Composable
fun NazeLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(typography = NazeTypography, content = content)
}
