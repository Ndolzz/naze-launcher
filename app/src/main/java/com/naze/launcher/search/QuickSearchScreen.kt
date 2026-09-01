package com.naze.launcher.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.launcher.appdrawer.AppInfo

/**
 * Search results are app-name matches today. The web-search fallback and contacts
 * lookup are left as clearly marked extension points (contacts require an extra
 * runtime permission the onboarding flow deliberately does not pre-request).
 */
@Composable
fun QuickSearchScreen(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
    backgroundColor: Color,
    textColor: Color
) {
    var query by remember { mutableStateOf("") }
    val results = remember(apps, query) {
        if (query.isBlank()) emptyList() else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            textStyle = TextStyle(color = textColor, fontSize = 20.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(textColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text("Search", color = textColor.copy(alpha = 0.5f), fontSize = 20.sp)
                }
                inner()
            }
        )

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(results, key = { it.packageName }) { app ->
                Text(
                    text = app.label,
                    color = textColor,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                        .padding(vertical = 12.dp)
                )
            }
            if (query.isNotBlank() && results.isEmpty()) {
                item {
                    Text(
                        "Web search for \u201c$query\u201d",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}
