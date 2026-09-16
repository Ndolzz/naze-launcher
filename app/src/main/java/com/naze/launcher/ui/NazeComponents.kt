package com.naze.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.naze.launcher.theme.Ambience
import com.naze.launcher.theme.NazeMotion
import com.naze.launcher.theme.NazeRadius
import com.naze.launcher.theme.NazeSpacing
import com.naze.launcher.theme.PerformanceMode

/**
 * Press micro-interaction: a subtle 0.94 scale while touched, no ripple.
 * Subtle by design — feedback, not a party trick.
 */
@Composable
fun Modifier.nazeClickable(
    performanceMode: PerformanceMode,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(NazeMotion.scaled(NazeMotion.MICRO, performanceMode)),
        label = "nazePress"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.nazeCombinedClickable(
    performanceMode: PerformanceMode,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(NazeMotion.scaled(NazeMotion.MICRO, performanceMode)),
        label = "nazePressLong"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onLongClick = onLongClick,
            onClick = onClick
        )
}

/** The Naze card: translucent ambience surface with a hairline border. */
@Composable
fun NazeSurface(
    ambience: Ambience,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = NazeRadius.lg,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .background(ambience.surface, shape)
            .border(1.dp, ambience.onBackground.copy(alpha = 0.08f), shape)
    ) {
        content()
    }
}

/** Uppercase accent section label — the quiet way Naze groups content. */
@Composable
fun NazeSectionLabel(text: String, ambience: Ambience, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = ambience.accent,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier.padding(start = NazeSpacing.xs)
    )
}

/** Selectable pill chip used for sort options, theme mode and performance mode. */
@Composable
fun NazeSelectableChip(
    text: String,
    selected: Boolean,
    ambience: Ambience,
    performanceMode: PerformanceMode,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    val bgColor = if (selected) ambience.accentSoft else ambience.onBackground.copy(alpha = 0.06f)
    val borderColor = if (selected) ambience.accent.copy(alpha = 0.55f) else Color.Transparent
    Box(
        modifier = Modifier
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape)
            .nazeClickable(performanceMode, onClick)
            .padding(horizontal = NazeSpacing.lg, vertical = NazeSpacing.sm)
    ) {
        Text(
            text = text,
            color = if (selected) ambience.accent else ambience.onBackground.copy(alpha = 0.80f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
