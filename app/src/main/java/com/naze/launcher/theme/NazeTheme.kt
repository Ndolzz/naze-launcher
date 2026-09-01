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

/** Smooth, GPU-friendly cross-fade between ambiences — never a hard cut. */
@Composable
fun animateAmbience(target: Ambience, reduceAnimations: Boolean): Ambience {
    val durationMs = if (reduceAnimations) 0 else 900
    val top by animateColorAsState(target.backgroundTop, tween(durationMs), label = "bgTop")
    val bottom by animateColorAsState(target.backgroundBottom, tween(durationMs), label = "bgBottom")
    val onBg by animateColorAsState(target.onBackground, tween(durationMs), label = "onBg")
    val accent by animateColorAsState(target.accent, tween(durationMs), label = "accent")
    return Ambience(top, bottom, onBg, accent, target.isDark)
}

val NazeTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Light, fontSize = 88.sp, letterSpacing = (-1).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.5.sp)
)

@Composable
fun NazeLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(typography = NazeTypography, content = content)
}
