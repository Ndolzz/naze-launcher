package com.naze.launcher.dock

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.unit.dp
import com.naze.launcher.appdrawer.AppInfo

@Composable
fun FavoriteDock(
    dockApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dockApps.forEach { app ->
            Image(
                bitmap = app.icon.toBitmap(112, 112).asImageBitmap(),
                contentDescription = app.label,
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onAppClick(app) }
            )
        }
    }
}
