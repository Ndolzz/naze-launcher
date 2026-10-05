package com.naze.launcher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.theme.Ambience
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Naze ambient background: a deep vertical gradient plus (at most) two very soft
 * accent glows that drift almost imperceptibly. This is the entire "particle system" —
 * deliberately just two radial gradient circles, which a GPU draws for near-zero cost,
 * and which collapse to a fully static image on PERFORMANCE / BATTERY_SAVER modes.
 *
 * There are no wallpaper layers, no blur passes and no per-frame allocations: the
 * brushes are re-created per frame only from cheap primitives, and the whole canvas
 * redraws at most once per animation frame while visible.
 */
@Composable
fun AmbientBackground(
    ambience: Ambience,
    mode: PerformanceMode,
    modifier: Modifier = Modifier
) {
    val animate = mode.animatesAmbient
    val showGlow = mode.showsAmbientGlow

    val transition = rememberInfiniteTransition(label = "ambient")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 32_000, easing = LinearEasing)
        ),
        label = "ambientPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ambience.backgroundTop, ambience.backgroundBottom),
                startY = 0f,
                endY = height
            )
        )

        if (showGlow) {
            val p = if (animate) phase else 0.35f
            val angle = (p * 2f * Math.PI).toFloat()
            val angleB = angle * 0.8f

            val glowCenterA = Offset(
                x = width * (0.74f + 0.09f * sin(angle)),
                y = height * (0.28f + 0.05f * cos(angle))
            )
            val glowCenterB = Offset(
                x = width * (0.22f + 0.07f * cos(angleB)),
                y = height * (0.72f + 0.05f * sin(angleB))
            )

            val radiusA = width * 0.80f
            val radiusB = width * 0.65f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambience.accent.copy(alpha = 0.13f), Color.Transparent),
                    center = glowCenterA,
                    radius = radiusA
                ),
                radius = radiusA,
                center = glowCenterA
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambience.accent.copy(alpha = 0.09f), Color.Transparent),
                    center = glowCenterB,
                    radius = radiusB
                ),
                radius = radiusB,
                center = glowCenterB
            )
        }
    }
}
