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
import androidx.compose.ui.unit.dp
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.theme.Ambience
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Naze ambient background, matching the design mockup:
 *
 *   1. a deep ink-to-night vertical gradient (the base surface);
 *   2. a faint engineered grid, like blueprint paper under the whole UI;
 *   3. two very soft deep glows in the lower half — a big indigo haze to the
 *      lower-left and a violet one to the lower-right — that drift almost
 *      imperceptibly.
 *
 * There are no wallpaper layers, no blur passes and no per-frame allocations: the
 * brushes are re-created per frame only from cheap primitives, and the whole canvas
 * redraws at most once per animation frame while visible. On PERFORMANCE /
 * BATTERY_SAVER modes the glows sit still and the canvas is fully static.
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

        // 1. Base surface: ink at the top fading into night at the bottom.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ambience.backgroundTop, ambience.backgroundBottom),
                startY = 0f,
                endY = height
            )
        )

        // 2. Faint blueprint grid across the whole surface.
        val step = 34.dp.toPx()
        val gridColor = ambience.onBackground.copy(alpha = 0.05f)
        val strokeWidth = 1.dp.toPx()
        var x = step
        while (x < width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = strokeWidth
            )
            x += step
        }
        var y = step
        while (y < height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = strokeWidth
            )
            y += step
        }

        // 3. Two deep glows drifting near the bottom edge.
        if (showGlow) {
            val p = if (animate) phase else 0.35f
            val angle = (p * 2f * Math.PI).toFloat()
            val angleB = angle * 0.8f

            val glowCenterA = Offset(
                x = width * (0.16f + 0.05f * sin(angle)),
                y = height * (0.88f + 0.03f * cos(angle))
            )
            val glowCenterB = Offset(
                x = width * (0.86f + 0.04f * cos(angleB)),
                y = height * (0.94f + 0.02f * sin(angleB))
            )

            val radiusA = width * 1.15f
            val radiusB = width * 0.85f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambience.glowA.copy(alpha = 0.9f), Color.Transparent),
                    center = glowCenterA,
                    radius = radiusA
                ),
                radius = radiusA,
                center = glowCenterA
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ambience.glowB.copy(alpha = 0.55f), Color.Transparent),
                    center = glowCenterB,
                    radius = radiusB
                ),
                radius = radiusB,
                center = glowCenterB
            )
        }
    }
}
