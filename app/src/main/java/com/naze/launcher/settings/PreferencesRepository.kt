package com.naze.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.naze.launcher.appdrawer.AppSortMode
import com.naze.launcher.core.PerformanceMode
import com.naze.launcher.gestures.GestureAction
import com.naze.launcher.gestures.GestureBindings
import com.naze.launcher.gestures.GestureTrigger
import com.naze.launcher.lock.LockClockStyle
import com.naze.launcher.storage.PreferencesKeys
import com.naze.launcher.storage.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val onboardingComplete: Boolean = false,

    // Appearance
    val dynamicWeatherEnabled: Boolean = true,
    val dynamicTemperatureEnabled: Boolean = true,
    val dynamicTimeEnabled: Boolean = true,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,

    // Clock
    val clock24Hour: Boolean = true,
    val clockShowSeconds: Boolean = false,
    val clockSizeScale: Float = 1.0f,

    // Naze Lock
    val lockClockStyle: LockClockStyle = LockClockStyle.STACK,

    // App drawer
    val drawerSort: AppSortMode = AppSortMode.NAME,
    val drawerColumns: Int = 4,
    val showAppLabels: Boolean = true,

    // Weather / Location
    val useAutomaticLocation: Boolean = true,
    val manualLocationName: String? = null,
    val manualLat: Double? = null,
    val manualLon: Double? = null,
    val tempUnitFahrenheit: Boolean = false,
    val weatherApiKey: String? = null,

    // Gestures
    val gestureBindings: GestureBindings = GestureBindings(),

    // Dock
    val dockPackages: List<String> = emptyList()
)

class PreferencesRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            onboardingComplete = prefs[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
            dynamicWeatherEnabled = prefs[PreferencesKeys.DYNAMIC_WEATHER_ENABLED] ?: true,
            dynamicTemperatureEnabled = prefs[PreferencesKeys.DYNAMIC_TEMPERATURE_ENABLED] ?: true,
            dynamicTimeEnabled = prefs[PreferencesKeys.DYNAMIC_TIME_ENABLED] ?: true,
            performanceMode = PerformanceMode.fromName(prefs[PreferencesKeys.PERFORMANCE_MODE]),
            clock24Hour = prefs[PreferencesKeys.CLOCK_24_HOUR] ?: true,
            clockShowSeconds = prefs[PreferencesKeys.CLOCK_SHOW_SECONDS] ?: false,
            clockSizeScale = (prefs[PreferencesKeys.CLOCK_SIZE_SCALE] ?: 1.0f).coerceIn(0.7f, 1.4f),
            lockClockStyle = LockClockStyle.fromName(prefs[PreferencesKeys.LOCK_CLOCK_STYLE]),
            drawerSort = prefs[PreferencesKeys.DRAWER_SORT]
                ?.let { runCatching { AppSortMode.valueOf(it) }.getOrNull() } ?: AppSortMode.NAME,
            drawerColumns = (prefs[PreferencesKeys.DRAWER_COLUMNS] ?: 4).coerceIn(3, 6),
            showAppLabels = prefs[PreferencesKeys.SHOW_APP_LABELS] ?: true,
            useAutomaticLocation = prefs[PreferencesKeys.USE_AUTOMATIC_LOCATION] ?: true,
            manualLocationName = prefs[PreferencesKeys.MANUAL_LOCATION_NAME],
            manualLat = prefs[PreferencesKeys.MANUAL_LAT],
            manualLon = prefs[PreferencesKeys.MANUAL_LON],
            tempUnitFahrenheit = prefs[PreferencesKeys.TEMP_UNIT_FAHRENHEIT] ?: false,
            weatherApiKey = prefs[PreferencesKeys.WEATHER_API_KEY],
            gestureBindings = GestureBindings(
                swipeUp = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_UP], GestureAction.OPEN_APP_DRAWER),
                swipeDown = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_DOWN], GestureAction.OPEN_NOTIFICATIONS),
                doubleTap = readAction(prefs[PreferencesKeys.GESTURE_DOUBLE_TAP], GestureAction.OPEN_SEARCH),
                swipeLeft = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_LEFT], GestureAction.NONE),
                swipeRight = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_RIGHT], GestureAction.NONE),
                longPress = readAction(prefs[PreferencesKeys.GESTURE_LONG_PRESS], GestureAction.OPEN_QUICK_ACTIONS)
            ),
            dockPackages = prefs[PreferencesKeys.DOCK_APPS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        )
    }

    private fun readAction(raw: String?, default: GestureAction): GestureAction =
        raw?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: default

    suspend fun setOnboardingComplete(value: Boolean) = set(PreferencesKeys.ONBOARDING_COMPLETE, value)
    suspend fun setDynamicWeatherEnabled(value: Boolean) = set(PreferencesKeys.DYNAMIC_WEATHER_ENABLED, value)
    suspend fun setDynamicTemperatureEnabled(value: Boolean) = set(PreferencesKeys.DYNAMIC_TEMPERATURE_ENABLED, value)
    suspend fun setDynamicTimeEnabled(value: Boolean) = set(PreferencesKeys.DYNAMIC_TIME_ENABLED, value)
    suspend fun setPerformanceMode(value: PerformanceMode) = set(PreferencesKeys.PERFORMANCE_MODE, value.name)
    suspend fun setClock24Hour(value: Boolean) = set(PreferencesKeys.CLOCK_24_HOUR, value)
    suspend fun setClockShowSeconds(value: Boolean) = set(PreferencesKeys.CLOCK_SHOW_SECONDS, value)
    suspend fun setClockSizeScale(value: Float) = set(PreferencesKeys.CLOCK_SIZE_SCALE, value.coerceIn(0.7f, 1.4f))
    suspend fun setLockClockStyle(value: LockClockStyle) = set(PreferencesKeys.LOCK_CLOCK_STYLE, value.name)
    suspend fun setDrawerSort(value: AppSortMode) = set(PreferencesKeys.DRAWER_SORT, value.name)
    suspend fun setDrawerColumns(value: Int) = set(PreferencesKeys.DRAWER_COLUMNS, value.coerceIn(3, 6))
    suspend fun setShowAppLabels(value: Boolean) = set(PreferencesKeys.SHOW_APP_LABELS, value)
    suspend fun setUseAutomaticLocation(value: Boolean) = set(PreferencesKeys.USE_AUTOMATIC_LOCATION, value)
    suspend fun setTempUnitFahrenheit(value: Boolean) = set(PreferencesKeys.TEMP_UNIT_FAHRENHEIT, value)
    suspend fun setWeatherApiKey(value: String) = set(PreferencesKeys.WEATHER_API_KEY, value.trim())

    suspend fun setManualLocation(name: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MANUAL_LOCATION_NAME] = name
            prefs[PreferencesKeys.MANUAL_LAT] = lat
            prefs[PreferencesKeys.MANUAL_LON] = lon
        }
    }

    suspend fun setGestureAction(trigger: GestureTrigger, action: GestureAction) {
        val key = when (trigger) {
            GestureTrigger.SWIPE_UP -> PreferencesKeys.GESTURE_SWIPE_UP
            GestureTrigger.SWIPE_DOWN -> PreferencesKeys.GESTURE_SWIPE_DOWN
            GestureTrigger.DOUBLE_TAP -> PreferencesKeys.GESTURE_DOUBLE_TAP
            GestureTrigger.SWIPE_LEFT -> PreferencesKeys.GESTURE_SWIPE_LEFT
            GestureTrigger.SWIPE_RIGHT -> PreferencesKeys.GESTURE_SWIPE_RIGHT
            GestureTrigger.LONG_PRESS -> PreferencesKeys.GESTURE_LONG_PRESS
        }
        set(key, action.name)
    }

    private suspend fun <T> set(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
