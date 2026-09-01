package com.naze.launcher.appdrawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppSortMode { NAME, RECENT }

/**
 * Renders instantly against an already-loaded [apps] list — heavy PackageManager work
 * happens once in the ViewModel, not per-keystroke. Search filters in-memory.
 */
@Composable
fun AppDrawerScreen(
    apps: List<AppInfo>,
    recentPackageNames: List<String>,
    sortMode: AppSortMode,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    textColor: Color
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
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            textStyle = TextStyle(color = textColor, fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(textColor.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Search apps", color = textColor.copy(alpha = 0.5f), fontSize = 18.sp)
                }
                inner()
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                AppRow(app, textColor) { onAppClick(app) }
            }
        }
    }
}

@Composable
private fun AppRow(app: AppInfo, textColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
            contentDescription = app.label,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = app.label,
            color = textColor,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
