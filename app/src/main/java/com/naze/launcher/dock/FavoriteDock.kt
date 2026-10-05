package com.naze.launcher.dock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.naze.launcher.appdrawer.AppInfo
import com.naze.launcher.ui.pressScale

/**
 * The favorite dock: app icons on a soft translucent rail with consistent circular
 * backdrops (so mixed vendor icon shapes still read as one family) and the standard
 * Naze press micro-interaction. Long-press opens the per-app actions sheet
 * (pin/unpin + app info).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteDock(
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    onAppLongPress: ((AppInfo) -> Unit)? = null,
    backdrop: Color = Color.Transparent
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dockApps.forEach { app ->
            val interaction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier.size(62.dp)
                    .pressScale(interaction, pressedScale = 0.88f).clip(CircleShape)
                    .background(backdrop).combinedClickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongPress?.invoke(app) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = app.icon.toBitmap(128, 128).asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}
