package com.naze.launcher.appdrawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.NazeChip
import com.naze.launcher.ui.NazeIconButton
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.SearchField
import com.naze.launcher.ui.pressScale
import kotlinx.coroutines.delay

enum class AppSortMode { NAME, RECENT }

/**
 * The modern Naze app drawer: a consistent responsive grid with proportional icon
 * sizing, in-memory search (heavy PackageManager work stays in the ViewModel), a
 * sort toggle, and a long-press per-app action sheet (dock management + app info).
 *
 * Items render against an already-loaded [apps] list, so filtering per keystroke is
 * pure in-memory work.
 */
@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    recentPackageNames: List<String>,
    sortMode: AppSortMode,
    showLabels: Boolean,
    columns: Int,
    performanceMode: PerformanceMode,
    onAppClick: (AppInfo) -> Unit,
    onAppLongPress: (AppInfo) -> Unit,
    onSortModeChange: (AppSortMode) -> Unit,
    onDismiss: () -> Unit,
    scrimColor: Color,
    textColor: Color,
    accent: Color
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(apps, query, sortMode, recentPackageNames) {
        val base = if (query.isBlank()) apps else apps.filter {
            it.label.contains(query, ignoreCase = true)
        }
        when (sortMode) {
            AppSortMode.NAME -> base.sortedBy { it.label.lowercase() }
            AppSortMode.RECENT -> {
                val order = recentPackageNames.withIndex().associate { (i, pkg) -> pkg to i }
                base.sortedBy { order[it.packageName] ?: Int.MAX_VALUE }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scrimColor)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchField(
                query = query,
                onQueryChange = { query = it },
                hint = "Search apps",
                textColor = textColor,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            NazeChip(
                text = if (sortMode == AppSortMode.NAME) "A–Z" else "Recent",
                icon = NazeIcons.Sort,
                tint = textColor.copy(alpha = 0.85f),
                background = textColor.copy(alpha = 0.08f),
                onClick = {
                    onSortModeChange(
                        if (sortMode == AppSortMode.NAME) AppSortMode.RECENT else AppSortMode.NAME
                    )
                },
                fontSize = 12.sp
            )
            Spacer(Modifier.width(10.dp))
            NazeIconButton(
                icon = NazeIcons.Close,
                contentDescription = "Close app drawer",
                onClick = onDismiss,
                tint = textColor.copy(alpha = 0.7f),
                background = textColor.copy(alpha = 0.08f)
            )
        }

        Spacer(Modifier.height(10.dp))

        if (apps.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No apps found",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    fontFamily = LocalNazeUiFont.current
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns.coerceIn(3, 6)),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val index = filtered.indexOf(app)
                    AppGridItem(
                        app = app,
                        index = index,
                        showLabel = showLabels,
                        animateEntrance = performanceMode.staggersEntrances,
                        textColor = textColor,
                        onClick = { onAppClick(app) },
                        onLongPress = { onAppLongPress(app) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: AppInfo,
    index: Int,
    showLabel: Boolean,
    animateEntrance: Boolean,
    textColor: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var appeared by remember(app.packageName) { mutableStateOf(!animateEntrance) }
    LaunchedEffect(app.packageName) {
        if (animateEntrance) {
            delay(minOf(index, 26) * 12L)
            appeared = true
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(240),
        label = "entrance"
    )

    val interaction = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                alpha = progress
                val s = 0.82f + 0.18f * progress
                scaleX = s
                scaleY = s
            }
            .pressScale(interaction, pressedScale = 0.90f)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(vertical = 4.dp)
    ) {
        Image(
            bitmap = app.icon.toBitmap(128, 128).asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(56.dp)
        )
        if (showLabel) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = app.label,
                color = textColor.copy(alpha = 0.88f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
