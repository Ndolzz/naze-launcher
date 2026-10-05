package com.naze.launcher.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.naze.launcher.appdrawer.AppInfo
import com.naze.launcher.theme.LocalNazeUiFont
import com.naze.launcher.ui.EmptyState
import com.naze.launcher.ui.NazeIcons
import com.naze.launcher.ui.SearchField
import com.naze.launcher.ui.pressScale
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A launcher action surfaced in search — e.g. "Toggle flashlight", "Open settings".
 * Only real, working actions may appear here; the old non-functional
 * "Web search for …" dead row was removed.
 */
data class SearchAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val run: () -> Unit
)

/**
 * Universal launcher search: apps plus real launcher actions, with smooth result
 * appearance and honest empty states. The keyboard never breaks the layout thanks
 * to imePadding.
 */
@Composable
fun QuickSearchScreen(
    apps: List<AppInfo>,
    actions: List<SearchAction>,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    accent: Color
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val matchedApps = remember(apps, query) {
        if (query.isBlank()) emptyList()
        else apps.filter { it.label.contains(query, ignoreCase = true) }.take(6)
    }
    val matchedActions = remember(actions, query) {
        if (query.isBlank()) emptyList()
        else actions.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.subtitle.contains(query, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        SearchField(
            query = query,
            onQueryChange = { query = it },
            hint = "Search apps and actions",
            textColor = textColor,
            fontSize = 17.sp,
            autoFocus = true,

            modifier = Modifier.focusRequester(focusRequester)
        )

        if (query.isBlank()) {
            Spacer(Modifier.height(28.dp))
            Text(
                "QUICK ACTIONS",
                color = textColor.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = LocalNazeUiFont.current,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(actions, key = { it.title }) { action ->
                    ActionRow(action, textColor, accent)
                }
            }
        } else {
            if (matchedApps.isEmpty() && matchedActions.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = NazeIcons.Search,
                        title = "No results for \"$query\"",
                        subtitle = "Try another app or action name",
                        tint = textColor
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (matchedApps.isNotEmpty()) {
                        item {
                            SectionLabel("APPS", textColor)
                        }
                        items(matchedApps, key = { it.packageName }) { app ->
                            AppResultRow(app, textColor) { onAppClick(app) }
                        }
                    }
                    if (matchedActions.isNotEmpty()) {
                        item {
                     
       SectionLabel("ACTIONS", textColor)
                        }
                        items(matchedActions, key = { it.title }) { action ->
                            ActionRow(action, textColor, accent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, textColor: Color) {
    Text(
        text = text,
        color = textColor.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = LocalNazeUiFont.current,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AppResultRow(app: AppInfo, textColor: Color, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.97f)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(38.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = app.label,
            color = textColor.copy(alpha = 0.92f),
            fontSize = 15.sp,
            fontFamily = LocalNazeUiFont.current,
            maxLines = 1
        )
    }
}

@Composable
private fun ActionRow(action: SearchAction, textColor: Color, accent: Color) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction, pressedScale = 0.97f)
            .clickable(interactionSource = interaction, indication = null, onClick = action.run)
          
  .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            action.icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = action.title,
                color = textColor.copy(alpha = 0.92f),
                fontSize = 15.sp,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
            Text(
                text = action.subtitle,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontFamily = LocalNazeUiFont.current,
                maxLines = 1
            )
        }
    }
}
