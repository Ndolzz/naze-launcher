package com.naze.launcher.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.naze.launcher.R
import com.naze.launcher.core.NazeMotion
import com.naze.launcher.core.PerformanceMode

/**
 * Naze typography strategy:
 *
 *  - Space Grotesk (Light/Medium) — the clock and display numerals: technical,
 *    geometric, unmistakably "engineered". This is the strongest identity signal
 *    on the home screen.
 *  - Inter (Regular/Medium/SemiBold) — every other piece of UI text: clean,
 *    high-readability workhorse.
 *
 * Both come from Google Fonts via Downloadable Fonts (no TTFs bundled — the APK stays
 * small). On devices without Play Services the Composable font stack falls back to
 * the platform sans-serif, which keeps everything readable; weights and letter
 * spacing still carry the hierarchy either way.
 */

private val SpaceGroteskFont = GoogleFont("Space Grotesk")
private val InterFont = GoogleFont("Inter")

val LocalNazeDisplayFont = staticCompositionLocalOf<FontFamily> { FontFamily.SansSerif }
val LocalNazeUiFont = staticCompositionLocalOf<FontFamily> { FontFamily.SansSerif }

private fun displayFamily(provider: GoogleFont.Provider): FontFamily = FontFamily(
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = SpaceGroteskFont, fontProvider = provider, weight = FontWeight.Medium)
)

private fun uiFamily(provider: GoogleFont.Provider): FontFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold)
)

/**
 * Smooth, GPU-friendly cross-fade between ambiences — never a hard cut. Duration
 * follows the motion system and the active performance mode.
 */
@Composable
fun animateAmbience(target: Ambience, performanceMode: PerformanceMode): Ambience {
    val durationMs = NazeMotion.ambience(performanceMode)
    val top by animateColorAsState(target.backgroundTop, tween(durationMs), label = "bgTop")
    val bottom by animateColorAsState(target.backgroundBottom, tween(durationMs), label = "bgBottom")
    val onBg by animateColorAsState(target.onBackground, tween(durationMs), label = "onBg")
    val accent by animateColorAsState(target.accent, tween(durationMs), label = "accent")
    return Ambience(top, bottom, onBg, accent, target.isDark)
}

@Composable
fun NazeLauncherTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val provider = remember {
        runCatching {
            GoogleFont.Provider(
                providerAuthority = "com.google.android.gms.fonts",
                providerPackage = "com.google.android.gms",
                certificates = R.array.com_google_android_gms_fonts_certs
            )
        }.getOrNull()
    }

    val display = remember(provider) { provider?.let { displayFamily(it) } ?: FontFamily.SansSerif }
    val ui = remember(provider) { provider?.let { uiFamily(it) } ?: FontFamily.SansSerif }

    val typography = Typography(
        displayLarge = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.

Light,
            fontSize = 96.sp,
            letterSpacing = (-3).sp
      
  ),
        displayMedium = TextStyle(
            fontFamily = display,
            fontWeight = FontWeight.Light,
            fontSize = 64.sp,
            letterSpacing = (-2).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = ui,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = ui,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = 0.1.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = ui,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            letterSpacing = 0.1.sp
        ),
        labelLarge = TextStyle(
            fontFamily = ui,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.2.sp
        ),
        labelSmall = TextStyle(
            fontFamily = ui,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp
        )
    )

    CompositionLocalProvider(
        LocalNazeDisplayFont provides display,
        LocalNazeUiFont provides ui
    ) {
        MaterialTheme(typography = typography, content = content)
    }
}
