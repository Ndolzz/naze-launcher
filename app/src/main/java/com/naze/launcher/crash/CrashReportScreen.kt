package com.naze.launcher.crash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal, dependency-light screen that shows the last crash stack trace on device.
 * Screenshot this and share it — no adb required.
 */
@Composable
fun CrashReportScreen(trace: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10131A))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Naze Launcher stopped",
            color = Color(0xFFFF6B6B),
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Screenshot this screen and share it to debug:",
            color = Color(0xFFB8BFCC),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = trace,
            color = Color(0xFFD8DDE6),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Clear and try again")
        }
    }
}
