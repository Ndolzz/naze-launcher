package com.naze.launcher.appdrawer

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naze.launcher.storage.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystemApp: Boolean
)

/**
 * Wraps PackageManager queries (the expensive part) behind suspend functions so callers
 * never block the UI thread, and keeps a tiny "last used" ledger in DataStore for the
 * App Drawer's "recently used" sort — no separate usage-stats service is spun up.
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val lastUsedKey = stringPreferencesKey("app_last_used_ledger") // "pkg:timestamp,pkg:timestamp"

    suspend fun getAllLaunchableApps(): List<AppInfo> = withContext(Dispatchers.Default) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        resolveInfos.map { resolveInfo ->
            val appInfo: ApplicationInfo = resolveInfo.activityInfo.applicationInfo
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                label = resolveInfo.loadLabel(packageManager).toString(),
                icon = resolveInfo.loadIcon(packageManager),
                isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun launch(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }

    suspend fun recordLaunch(packageName: String) {
        context.dataStore.edit { prefs ->
            val ledger = parseLedger(prefs[lastUsedKey])
                .toMutableMap()
                .apply { put(packageName, System.currentTimeMillis()) }
            prefs[lastUsedKey] = serializeLedger(ledger)
        }
    }

    suspend fun getRecentPackageNamesSortedByRecency(): List<String> {
        val prefs = context.dataStore.data.first()
        return parseLedger(prefs[lastUsedKey])
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }

    private fun parseLedger(raw: String?): Map<String, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toLongOrNull() ?: 0L) else null
        }.toMap()
    }

    private fun serializeLedger(ledger: Map<String, Long>): String =
        ledger.entries.joinToString(",") { "${it.key}:${it.value}" }
}
