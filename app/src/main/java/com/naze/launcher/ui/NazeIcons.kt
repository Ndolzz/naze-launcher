package com.naze.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Naze's icon system: a single stroke-based vector set drawn on Canvas.
 * One consistent style (2dp round strokes, geometric) across the whole launcher —
 * no mixed icon sources, no emoji glyphs posing as UI icons.
 */
class NazeIconData internal constructor(internal val draw: (DrawScope, Color) -> Unit)

object NazeIcons {

    private val DrawScope.strokeWidth: Float get() = 2.dp.toPx()

    private fun DrawScope.line(tint: Color, x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(tint, Offset(x1, y1), Offset(x2, y2), strokeWidth, StrokeCap.Round)
    }

    private fun DrawScope.cloud(tint: Color, baseY: Float) {
        val w = size.width
        val h = size.height
        line(tint, w * 0.24f, h * baseY, w * 0.76f, h * baseY)
        drawArc(
            tint, 180f, 180f, false,
            topLeft = Offset(w * 0.28f, h * (baseY - 0.30f)),
            size = Size(w * 0.28f, w * 0.28f),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            tint, 180f, 160f, false,
            topLeft = Offset(w * 0.50f, h * (baseY - 0.26f)),
            size = Size(w * 0.22f, w * 0.22f),
            style = Stroke(strokeWidth, cap = StrokeCap.Round)
        )
    }

    /** The Naze mark: a minimal clock ring with hands — the launcher's signature. */
    val NazeMark = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            drawCircle(
                tint, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.5f),
                style = Stroke(strokeWidth * 1.2f, cap = StrokeCap.Round)
            )
            line(tint, w * 0.5f, h * 0.5f, w * 0.5f, h * 0.26f)
            line(tint, w * 0.5f, h * 0.5f, w * 0.68f, h * 0.60f)
        }
    }

    val Search = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            drawCircle(
                tint, radius = w * 0.27f, center = Offset(w * 0.38f, h * 0.38f),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            line(tint, w * 0.60f, h * 0.60f, w * 0.82f, h * 0.82f)
        }
    }

    /** Settings — three sliders, the "tune" metaphor. */
    val Tune = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            line(tint, w * 0.18f, h * 0.30f, w * 0.82f, h * 0.30f)
            line(tint, w * 0.18f, h * 0.50f, w * 0.82f, h * 0.50f)
            line(tint, w * 0.18f, h * 0.70f, w * 0.82f, h * 0.70f)
            drawCircle(tint, radius = strokeWidth * 1.9f, center = Offset(w * 0.62f, h * 0.30f))
            drawCircle(tint, radius = strokeWidth * 1.9f, center = Offset(w * 0.38f, h * 0.50f))
            drawCircle(tint, radius = strokeWidth * 1.9f, center = Offset(w * 0.70f, h * 0.70f))
        }
    }

    val Close = NazeIconData { scope, tint ->
        with(scope) {
            line(tint, size.width * 0.28f, size.height * 0.28f, size.width * 0.72f, size.height * 0.72f)
            line(tint, size.width * 0.72f, size.height * 0.28f, size.width * 0.28f, size.height * 0.72f)
        }
    }

    val ChevronUp = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val p = Path().apply {
                moveTo(w * 0.25f, h * 0.62f)
                lineTo(w * 0.5f, h * 0.38f)
                lineTo(w * 0.75f, h * 0.62f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val ChevronDown = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val p = Path().apply {
                moveTo(w * 0.25f, h * 0.38f)
                lineTo(w * 0.5f, h * 0.62f)
                lineTo(w * 0.75f, h * 0.38f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Check = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val p = Path().apply {
                moveTo(w * 0.26f, h * 0.52f)
                lineTo(w * 0.44f, h * 0.70f)
                lineTo(w * 0.76f, h * 0.32f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Sun = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.5f
            drawCircle(tint, radius = w * 0.18f, center = Offset(cx, cy), style = Stroke(strokeWidth, cap = StrokeCap.Round))
            val r1 = w * 0.28f
            val r2 = w * 0.40f
            for (i in 0 until 8) {
                val a = (Math.PI.toFloat() * i) / 4f
                line(
                    tint,
                    cx + r1 * cos(a), cy + r1 * sin(a),
                    cx + r2 * cos(a), cy + r2 * sin(a)
                )
            }
        }
    }

    val Cloud = NazeIconData { scope, tint -> with(scope) { cloud(tint, 0.66f) } }

    val Rain = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            cloud(tint, 0.58f)
            line(tint, w * 0.38f, h * 0.68f, w * 0.34f, h * 0.82f)
            line(tint, w * 0.52f, h * 0.68f, w * 0.48f, h * 0.82f)
            line(tint, w * 0.66f, h * 0.68f, w * 0.62f, h * 0.82f)
        }
    }

    val Storm = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            cloud(tint, 0.50f)
            val p = Path().apply {
                moveTo(w * 0.54f, h * 0.54f)
                lineTo(w * 0.42f, h * 0.70f)
                lineTo(w * 0.52f, h * 0.70f)
                lineTo(w * 0.46f, h * 0.88f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Fog = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            cloud(tint, 0.52f)
            line(tint, w * 0.30f, h * 0.70f, w * 0.70f, h * 0.70f)
            line(tint, w * 0.38f, h * 0.82f, w * 0.62f, h * 0.82f)
        }
    }

    val Snow = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            cloud(tint, 0.52f)
            drawCircle(tint, radius = strokeWidth * 1.3f, center = Offset(w * 0.38f, h * 0.74f))
            drawCircle(tint, radius = strokeWidth * 1.3f, center = Offset(w * 0.52f, h * 0.82f))
            drawCircle(tint, radius = strokeWidth * 1.3f, center = Offset(w * 0.66f, h * 0.74f))
        }
    }

    val Wifi = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.72f
            drawCircle(tint, radius = strokeWidth * 1.6f, center = Offset(cx, cy))
            drawArc(
                tint, 225f, 90f, false,
                topLeft = Offset(cx - w * 0.24f, cy - w * 0.24f),
                size = Size(w * 0.48f, w * 0.48f),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                tint, 225f, 90f, false,
                topLeft = Offset(cx - w * 0.38f, cy - w * 0.38f),
                size = Size(w * 0.76f, w * 0.76f),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    val Bluetooth = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val x = w * 0.42f
            line(tint, x, h * 0.14f, x, h * 0.86f)
            val p = Path().apply {
                moveTo(x, h * 0.32f)
                lineTo(w * 0.64f, h * 0.50f)
                lineTo(x, h * 0.68f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Flashlight = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val p = Path().apply {
                moveTo(w * 0.30f, h * 0.10f)
                lineTo(w * 0.70f, h * 0.10f)
                lineTo(w * 0.70f, h * 0.26f)
                lineTo(w * 0.58f, h * 0.34f)
                lineTo(w * 0.58f, h * 0.88f)
                lineTo(w * 0.42f, h * 0.88f)
                lineTo(w * 0.42f, h * 0.34f)
                lineTo(w * 0.30f, h * 0.26f)
                close()
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Wallpaper = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                tint,
                topLeft = Offset(w * 0.14f, h * 0.18f),
                size = Size(w * 0.72f, h * 0.64f),
                cornerRadius = CornerRadius(w * 0.10f),
                style = Stroke(strokeWidth)
            )
            drawCircle(tint, radius = w * 0.05f, center = Offset(w * 0.36f, h * 0.36f))
            val p = Path().apply {
                moveTo(w * 0.20f, h * 0.70f)
                lineTo(w * 0.44f, h * 0.46f)
                lineTo(w * 0.60f, h * 0.62f)
                lineTo(w * 0.72f, h * 0.50f)
                lineTo(w * 0.80f, h * 0.58f)
            }
            drawPath(p, tint, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    val Globe = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(strokeWidth))
            line(tint, w * 0.16f, h * 0.5f, w * 0.84f, h * 0.5f)
            drawOval(
                tint,
                topLeft = Offset(w * 0.34f, h * 0.16f),
                size = Size(w * 0.32f, h * 0.68f),
                style = Stroke(strokeWidth)
            )
        }
    }

    val Info = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            drawCircle(tint, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(strokeWidth))
            drawCircle(tint, radius = strokeWidth * 1.1f, center = Offset(w * 0.5f, h * 0.36f))
            line(tint, w * 0.5f, h * 0.48f, w * 0.5f, h * 0.66f)
        }
    }

    val Grid = NazeIconData { scope, tint ->
        with(scope) {
            val w = size.width
            val h = size.height
            val r = w * 0.16f
            val cr = CornerRadius(w * 0.06f)
            drawRoundRect(tint, topLeft = Offset(w * 0.18f, h * 0.18f), size = Size(r, r), cornerRadius = cr, style = Stroke(strokeWidth))
            drawRoundRect(tint, topLeft = Offset(w * 0.58f, h * 0.18f), size = Size(r, r), cornerRadius = cr, style = Stroke(strokeWidth))
            drawRoundRect(tint, topLeft = Offset(w * 0.18f, h * 0.58f), size = Size(r, r), cornerRadius = cr, style = Stroke(strokeWidth))
            drawRoundRect(tint, topLeft = Offset(w * 0.58f, h * 0.58f), size = Size(r, r), cornerRadius = cr, style = Stroke(strokeWidth))
        }
    }
}

@Composable
fun NazeIcon(
    icon: NazeIconData,
    tint: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val desc = contentDescription
    val finalModifier = if (desc != null) {
        modifier.semantics { this.contentDescription = desc }
    } else {
        modifier
    }
    Canvas(modifier = finalModifier) {
        icon.draw(this, tint)
    }
}

/**
 * Battery indicator with a real fill level and charging bolt — drawn, not emoji,
 * and honest about its data source (system ACTION_BATTERY_CHANGED broadcast).
 */
@Composable
fun NazeBatteryIcon(
    fraction: Float,
    charging: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 2.dp.toPx()
        drawRoundRect(
            tint,
            topLeft = Offset(w * 0.05f, h * 0.25f),
            size = Size(w * 0.78f, h * 0.50f),
            cornerRadius = CornerRadius(h * 0.10f),
            style = Stroke(sw)
        )
        drawRoundRect(
            tint,
            topLeft = Offset(w * 0.86f, h * 0.40f),
            size = Size(w * 0.09f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.03f)
        )
        val f = fraction.coerceIn(0f, 1f)
        if (f > 0.01f) {
            drawRoundRect(
                tint,
                topLeft = Offset(w * 0.05f + sw, h * 0.25f + sw),
                size = Size((w * 0.78f - 2 * sw) * f, h * 0.50f - 2 * sw),
                cornerRadius = CornerRadius(h * 0.06f)
            )
        }
        if (charging) {
            val p = Path().apply {
                moveTo(w * 0.50f, h * 0.32f)
                lineTo(w * 0.42f, h * 0.52f)
                lineTo(w * 0.50f, h * 0.52f)
                lineTo(w * 0.46f, h * 0.68f)
            }
            drawPath(p, Color.White, style = Stroke(sw * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
