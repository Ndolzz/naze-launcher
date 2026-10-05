package com.naze.launcher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp

/**
 * The Naze icon family. One deliberate style: 24×24 viewport, 2dp round strokes,
 * no fills, geometric. Every icon in the app comes from here — UI icons, quick
 * actions and weather glyphs alike — so nothing is ever drawn with emoji or with
 * mixed icon styles.
 */
object NazeIcons {

    private fun strokeIcon(name: String, build: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        )
            .path(
                name = "$name/stroke",
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathBuilder = build
            )
            .build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        val k = r * 0.5523f
        moveTo(cx - r, cy)
        curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r)
        curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy)
        curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r)
        curveTo(cx + k, cy + r, cx - r, cy + k, cx - r, cy)
    }

    private fun PathBuilder.dot(cx: Float, cy: Float, r: Float = 1.1f) = circle(cx, cy, r)

    /** Shared cloud silhouette used by the weather glyph family. */
    private fun PathBuilder.cloud() {
        moveTo(6.4f, 17.3f)
        quadTo(4.1f, 17.3f, 4.1f, 15.1f)
        quadTo(4.1f, 13.0f, 6.2f, 12.7f)
        quadTo(6.4f, 9.0f, 9.8f, 8.4f)
        quadTo(10.7f, 5.4f, 14.0f, 5.8f)
        quadTo(17.5f, 6.2f, 17.9f, 9.5f)
        quadTo(20.5f, 10.1f, 20.5f, 12.4f)
        quadTo(20.5f, 17.3f, 16.9f, 17.3f)
        close()
    }

    // ------------------------------------------------------------------ UI icons

    val Search: ImageVector by lazy {
        strokeIcon("Search") {
            circle(10.5f, 10.5f, 6.5f)
            moveTo(15.2f, 15.2f)
            lineTo(20.5f, 20.5f)
        }
    }

    val Settings: ImageVector by lazy {
        strokeIcon("Settings") {
            circle(12f, 12f, 3.2f)
            moveTo(17.5f, 12f); lineTo(20.5f, 12f)
            moveTo(15.89f, 15.89f); lineTo(18.02f, 18.02f)
            moveTo(12f, 17.5f); lineTo(12f, 20.5f)
            moveTo(8.11f, 15.89f); lineTo(5.98f, 18.02f)
            moveTo(6.5f, 12f); lineTo(3.5f, 12f)
            moveTo(8.11f, 8.11f); lineTo(5.98f, 5.98f)
            moveTo(12f, 6.5f); lineTo(12f, 3.5f)
            moveTo(15.89f, 8.11f); lineTo(18.02f, 5.98f)
        }
    }

    val Wifi: ImageVector by lazy {
        strokeIcon("Wifi") {
            moveTo(7.8f, 14.6f)
            curveTo(9.4f, 12.9f, 14.6f, 12.9f, 16.2f, 14.6f)
            moveTo(4.8f, 11.5f)
            curveTo(7.0f, 9.3f, 17.0f, 9.3f, 19.2f, 11.5f)
            moveTo(2.2f, 8.2f)
            curveTo(4.8f, 5.6f, 19.2f, 5.6f, 21.8f, 8.2f)
            dot(12f, 18.2f, 1.4f)
        }
    }

    val Bluetooth: ImageVector by lazy {
        strokeIcon("Bluetooth") {
            moveTo(12f, 2f); lineTo(12f, 22f)
            moveTo(12f, 2f); lineTo(7f, 7f); lineTo(12f, 12f)
            moveTo(12f, 12f); lineTo(7f, 17f); lineTo(12f, 22f)
        }
    }

    val Camera: ImageVector by lazy {
        strokeIcon("Camera") {
            moveTo(4f, 8f); lineTo(7.5f, 8f); lineTo(9f, 5.5f); lineTo(15f, 5.5f); lineTo(16.5f, 8f)
            lineTo(20f, 8f); lineTo(20f, 19f); lineTo(4f, 19f); close()
            circle(12f, 13.2f, 3.2f)
        }
    }

    val Flash: ImageVector by lazy {
        strokeIcon("Flash") {
            moveTo(13f, 2f)
            lineTo(5f, 13f)
            lineTo(11f, 13f)
            lineTo(10f, 22f)
            lineTo(19f, 10f)
            lineTo(13f, 10f)
            close()
        }
    }

    val Battery: ImageVector by lazy {
        strokeIcon("Battery") {
            moveTo(3f, 8.5f); lineTo(19.5f, 8.5f); lineTo(19.5f, 15.5f); lineTo(3f, 15.5f); close()
            moveTo(21.3f, 10.8f); lineTo(21.3f, 13.2f)
            moveTo(6.5f, 12f); lineTo(10f, 12f)
        }
    }

    val Brightness: ImageVector by lazy {
        strokeIcon("Brightness") {
            circle(12f, 12f, 4f)
            moveTo(12f, 2f); lineTo(12f, 4.5f)
            moveTo(12f, 19.5f); lineTo(12f, 22f)
            moveTo(2f, 12f); lineTo(4.5f, 12f)
            moveTo(19.5f, 12f); lineTo(22f, 12f)
            moveTo(4.9f, 4.9f); lineTo(6.7f, 6.7f)
            moveTo(17.3f, 17.3f); lineTo(19.1f, 19.1f)
            moveTo(4.9f, 19.1f); lineTo(6.7f, 17.3f)
            moveTo(17.3f, 6.7f); lineTo(19.1f, 4.9f)
        }
    }

    val Sort: ImageVector by lazy {
        strokeIcon("Sort") {
            moveTo(7f, 4f); lineTo(7f, 20f)
            moveTo(4f, 7f); lineTo(7f, 4f); lineTo(10f, 7f)
            moveTo(17f, 20f); lineTo(17f, 4f)
            moveTo(14f, 17f); lineTo(17f, 20f); lineTo(20f, 17f)
        }
    }

    val Grid: ImageVector by lazy {
        strokeIcon("Grid") {
            moveTo(4f, 4f); lineTo(9.5f, 4f); lineTo(9.5f, 9.5f); lineTo(4f, 9.5f); close()
            moveTo(14.5f, 4f); lineTo(20f, 4f); lineTo(20f, 9.5f); lineTo(14.5f, 9.5f); close()
            moveTo(4f, 14.5f); lineTo(9.5f, 14.5f); lineTo(9.5f, 20f); lineTo(4f, 20f); close()
            moveTo(14.5f, 14.5f); lineTo(20f, 14.5f); lineTo(20f, 20f); lineTo(14.5f, 20f); close()
        }
    }

    val Clock: ImageVector by lazy {
        strokeIcon("Clock") {
            circle(12f, 12f, 8.5f)
            moveTo(12f, 12f); lineTo(12f, 7.5f)
            moveTo(12f, 12f); lineTo(15.5f, 13.5f)
        }
    }

    val Close: ImageVector by lazy {
        strokeIcon("Close") {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Check: ImageVector by lazy {
        strokeIcon("Check") {
            moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 7f)
        }
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("ChevronDown") {
            moveTo(6f, 9.5f); lineTo(12f, 15.5f); lineTo(18f, 9.5f)
        }
    }

    val ChevronUp: ImageVector by lazy {
        strokeIcon("ChevronUp") {
            moveTo(6f, 14.5f); lineTo(12f, 8.5f); lineTo(18f, 14.5f)
        }
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight") {
            moveTo(9.5f, 6f); lineTo(15.5f, 12f); lineTo(9.5f, 18f)
        }
    }

    val Info: ImageVector by lazy {
        strokeIcon("Info") {
            circle(12f, 12f, 8.5f)
            moveTo(12f, 11f); lineTo(12f, 16.5f)
            dot(12f, 7.6f, 0.9f)
        }
    }

    val Location: ImageVector by lazy {
        strokeIcon("Location") {
            moveTo(12f, 21.5f)
            curveTo(12f, 21.5f, 5.5f, 15.2f, 5.5f, 10.2f)
            curveTo(5.5f, 6.4f, 8.4f, 3.5f, 12f, 3.5f)
            curveTo(15.6f, 3.5f, 18.5f, 6.4f, 18.5f, 10.2f)
            curveTo(18.5f, 15.2f, 12f, 21.5f, 12f, 21.5f)
            close()
            dot(12f, 10.2f, 2.0f)
        }
    }

    val Wallpaper: ImageVector by lazy {
        strokeIcon("Wallpaper") {
            moveTo(3.5f, 5.5f); lineTo(20.5f, 5.5f); lineTo(20.5f, 18.5f); lineTo(3.5f, 18.5f); close()
            moveTo(6.5f, 15.5f); lineTo(10f, 11.5f); lineTo(12.5f, 14.5f); lineTo(15f, 11f); lineTo(18f, 15.5f)
            dot(16f, 8.5f, 1.3f)
        }
    }

    val Refresh: ImageVector by lazy {
        strokeIcon("Refresh") {
            moveTo(18.9f, 12f)
            curveTo(18.9f, 15.81f, 15.81f, 18.9f, 12f, 18.9f)
            curveTo(8.19f, 18.9f, 5.1f, 15.81f, 5.1f, 12f)
            curveTo(5.1f, 8.19f, 8.19f, 5.1f, 12f, 5.1f)
            curveTo(14.9f, 5.1f, 17.4f, 6.85f, 18.5f, 9.35f)
            moveTo(18.9f, 4.6f); lineTo(18.6f, 9.5f); lineTo(13.7f, 9.2f)
        }
    }

    val Bell: ImageVector by lazy {
        strokeIcon("Bell") {
            moveTo(12f, 3.5f)
            curveTo(8.4f, 3.5f, 6f, 6.2f, 6f, 9.8f)
            lineTo(6f, 13.5f)
            lineTo(4.5f, 16f)
            lineTo(19.5f, 16f)
            lineTo(18f, 13.5f)
            lineTo(18f, 9.8f)
            curveTo(18f, 6.2f, 15.6f, 3.5f, 12f, 3.5f)
            close()
            moveTo(10f, 18.5f)
            curveTo(10f, 19.9f, 10.9f, 20.8f, 12f, 20.8f)
            curveTo(13.1f, 20.8f, 14f, 19.9f, 14f, 18.5f)
        }
    }

    val Plus: ImageVector by lazy {
        strokeIcon("Plus") {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    val Minus: ImageVector by lazy {
        strokeIcon("Minus") {
            moveTo(6f, 12f); lineTo(18f, 12f)
        }
    }

    // ------------------------------------------------------------- weather glyphs

    val WeatherClear: ImageVector by lazy { Brightness }

    val WeatherCloudy: ImageVector by lazy {
        strokeIcon("WeatherCloudy") { cloud() }
    }

    val WeatherRain: ImageVector by lazy {
        strokeIcon("WeatherRain") {
            cloud()
            moveTo(8.5f, 19.3f); lineTo(7.6f, 21.6f)
            moveTo(12.2f, 19.3f); lineTo(11.3f, 21.6f)
            moveTo(15.9f, 19.3f); lineTo(15f, 21.6f)
        }
    }

    val WeatherStorm: ImageVector by lazy {
        strokeIcon("WeatherStorm") {
            cloud()
            moveTo(13.5f, 17.8f)
            lineTo(10.2f, 21.2f)
            lineTo(12.4f, 21.2f)
            lineTo(11.2f, 23.6f)
        }
    }

    val WeatherFog: ImageVector by lazy {
        strokeIcon("WeatherFog") {
            cloud()
            moveTo(7.5f, 19.5f); lineTo(16.5f, 19.5f)
            moveTo(9.5f, 21.8f); lineTo(14.5f, 21.8f)
        }
    }

    val WeatherSnow: ImageVector by lazy {
        strokeIcon("WeatherSnow") {
            cloud()
            moveTo(7.8f, 19.8f); lineTo(9.2f, 19.8f)
            moveTo(8.5f, 19.1f); lineTo(8.5f, 20.5f)
            moveTo(11.3f, 19.8f); lineTo(12.7f, 19.8f)
            moveTo(12f, 19.1f); lineTo(12f, 20.5f)
            moveTo(14.8f, 19.8f); lineTo(16.2f, 19.8f)
            moveTo(15.5f, 19.1f); lineTo(15.5f, 20.5f)
        }
    }

    val WeatherUnknown: ImageVector by lazy {
        strokeIcon("WeatherUnknown") {
            moveTo(7f, 12f); lineTo(10f, 12f)
            moveTo(14f, 12f); lineTo(17f, 12f)
        }
    }
}
