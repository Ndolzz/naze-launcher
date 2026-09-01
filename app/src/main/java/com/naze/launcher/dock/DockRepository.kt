package com.naze.launcher.dock

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.naze.launcher.core.Constants
import com.naze.launcher.storage.PreferencesKeys
import com.naze.launcher.storage.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DockRepository(private val context: Context) {

    val dockPackages = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DOCK_APPS]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    suspend fun setDock(packages: List<String>) {
        val trimmed = packages.take(MAX_DOCK_SLOTS)
        context.dataStore.edit { prefs -> prefs[PreferencesKeys.DOCK_APPS] = trimmed.joinToString(",") }
    }

    suspend fun addToDock(packageName: String) {
        val current = context.dataStore.data.first()[PreferencesKeys.DOCK_APPS]
            ?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
        if (packageName !in current && current.size < MAX_DOCK_SLOTS) {
            current.add(packageName)
            setDock(current)
        }
    }

    suspend fun removeFromDock(packageName: String) {
        val current = context.dataStore.data.first()[PreferencesKeys.DOCK_APPS]
            ?.split(",")?.filter { it.isNotBlank() }?.toMutableList() ?: mutableListOf()
        current.remove(packageName)
        setDock(current)
    }

    companion object {
        const val MAX_DOCK_SLOTS = Constants.DEFAULT_DOCK_SLOTS + 2 // user can go slightly beyond default
    }
}
