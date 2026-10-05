package com.naze.launcher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.
launcher.theme.LocalNazeUiFont
import java.util.Locale
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.vector.ImageVector
import com.naze.launcher.theme.LocalNazeUiFont

/**
 * Shared Naze design-system components. Everything visual in the app is built from
 * these plus the tokens in [com.naze.launcher.theme.NazeTheme] so no screen ever
 * develops its own private style.
 */

/** Springy scale-down on press — the launcher-wide press micro-interaction. */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.93f
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = 1400f,
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** A quiet icon button used across home, drawer, search and settings. */
@Composable
fun NazeIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    background: Color = Color.Transparent,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .pressScale(interaction)
            .clip(CircleShape)
            .background(background)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Small pill with optional icon — used for chips, hints and status bits. */
@Composable
fun NazeChip(
  
  text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Color.White,
    background: Color = Color.White.copy(alpha = 0.08f),
    onClick: (() -> Unit)? = null,
    fontSize: TextUnit = 13.sp
) {
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .clip(RoundedCornerShape(50))
        .background(background)
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = onClick != null,
            onClick = { onClick?.invoke() }
        )
        .padding(horizontal = 12.dp, vertical = 7.dp)
    val body: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = tint,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
        }
    }
    if (onClick != null) {
        Box(base.pressScale(interaction)) { body() }
    } else {
        Box(base) { body() }
    }
}

/**
 * The Naze loading affordance — three dots phasing in and out. Used instead of the
 * default platform progress indicator so even loading states stay on-brand.
 */
@Composable
fun NazeLoader(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    dotSize: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "nazeLoader")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "nazeLoaderPhase"
    )
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(3) { index ->
            val offset = (phase - index * 0.18f).coerceIn(0f, 1f)
            val triangle = 1f - kotlin.math.abs(offset - 0.5f) * 2f
            Box(
                Modifier
                    .size(dotSize)
                    .graphicsLayer { alpha = 0.25f + 0.75f * triangle }
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

/** Consistent empty/error state block. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = tint.copy(alpha = 0.45f), modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = tint.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = LocalNazeUiFont.current
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = tint.copy(alpha = 0.55f),
                fontSize = 13.sp,
                fontFamily = LocalNazeUiFont.current
            )
        }
    }
}

/** The search input used by both the overlay search and the app drawer. */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    autoFocus: Boolean = false
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
         
   fontSize = fontSize,
            fontFamily = LocalNazeUiFont.current
        ),
        cursorBrush = SolidColor(textColor.copy(alpha = 0.7f)),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(textColor.copy(alpha = 0.07f))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    NazeIcons.Search,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = hint,
                            color = textColor.copy(alpha = 0.45f),
                            fontSize = fontSize,
                            fontFamily = LocalNazeUiFont.current
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 10.0) String.format(Locale.US, "%.0f GB", gb)
    else String.format(Locale.US, "%.1f GB", gb)
}
