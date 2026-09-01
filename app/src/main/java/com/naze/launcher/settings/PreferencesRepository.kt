package com.naze.launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.naze.launcher.gestures.GestureAction
import com.naze.launcher.gestures.GestureBindings
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
    val reduceAnimations: Boolean = false,
    val batterySaver: Boolean = false,

    // Clock
    val clock24Hour: Boolean = true,
    val clockShowSeconds: Boolean = false,
    val clockSizeScale: Float = 1.0f,

    // Weather / Location
    val useAutomaticLocation: Boolean = true,
    val manualLocationName: String? = null,
    val manualLat: Double? = null,
    val manualLon: Double? = null,
    val tempUnitFahrenheit: Boolean = false,

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
            reduceAnimations = prefs[PreferencesKeys.REDUCE_ANIMATIONS] ?: false,
            batterySaver = prefs[PreferencesKeys.BATTERY_SAVER] ?: false,
            clock24Hour = prefs[PreferencesKeys.CLOCK_24_HOUR] ?: true,
            clockShowSeconds = prefs[PreferencesKeys.CLOCK_SHOW_SECONDS] ?: false,
            clockSizeScale = prefs[PreferencesKeys.CLOCK_SIZE_SCALE] ?: 1.0f,
            useAutomaticLocation = prefs[PreferencesKeys.USE_AUTOMATIC_LOCATION] ?: true,
            manualLocationName = prefs[PreferencesKeys.MANUAL_LOCATION_NAME],
            manualLat = prefs[PreferencesKeys.MANUAL_LAT],
            manualLon = prefs[PreferencesKeys.MANUAL_LON],
            tempUnitFahrenheit = prefs[PreferencesKeys.TEMP_UNIT_FAHRENHEIT] ?: false,
            gestureBindings = GestureBindings(
                swipeUp = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_UP], GestureAction.OPEN_APP_DRAWER),
                swipeDown = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_DOWN], GestureAction.OPEN_SEARCH),
                doubleTap = readAction(prefs[PreferencesKeys.GESTURE_DOUBLE_TAP], GestureAction.LOCK_SCREEN),
                swipeLeft = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_LEFT], GestureAction.NEXT_PAGE),
                swipeRight = readAction(prefs[PreferencesKeys.GESTURE_SWIPE_RIGHT], GestureAction.PREVIOUS_PAGE)
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
    suspend fun setReduceAnimations(value: Boolean) = set(PreferencesKeys.REDUCE_ANIMATIONS, value)
    suspend fun setBatterySaver(value: Boolean) = set(PreferencesKeys.BATTERY_SAVER, value)
    suspend fun setClock24Hour(value: Boolean) = set(PreferencesKeys.CLOCK_24_HOUR, value)
    suspend fun setClockShowSeconds(value: Boolean) = set(PreferencesKeys.CLOCK_SHOW_SECONDS, value)
    suspend fun setClockSizeScale(value: Float) = set(PreferencesKeys.CLOCK_SIZE_SCALE, value)
    suspend fun setUseAutomaticLocation(value: Boolean) = set(PreferencesKeys.USE_AUTOMATIC_LOCATION, value)
    suspend fun setTempUnitFahrenheit(value: Boolean) = set(PreferencesKeys.TEMP_UNIT_FAHRENHEIT, value)

    suspend fun setManualLocation(name: String, lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MANUAL_LOCATION_NAME] = name
            prefs[PreferencesKeys.MANUAL_LAT] = lat
            prefs[PreferencesKeys.MANUAL_LON] = lon
        }
    }

    suspend fun setGestureAction(trigger: com.naze.launcher.gestures.GestureTrigger, action: GestureAction) {
        val key = when (trigger) {
            com.naze.launcher.gestures.GestureTrigger.SWIPE_UP -> PreferencesKeys.GESTURE_SWIPE_UP
            com.naze.launcher.gestures.GestureTrigger.SWIPE_DOWN -> PreferencesKeys.GESTURE_SWIPE_DOWN
            com.naze.launcher.gestures.GestureTrigger.DOUBLE_TAP -> PreferencesKeys.GESTURE_DOUBLE_TAP
            com.naze.launcher.gestures.GestureTrigger.SWIPE_LEFT -> PreferencesKeys.GESTURE_SWIPE_LEFT
            com.naze.launcher.gestures.GestureTrigger.SWIPE_RIGHT -> PreferencesKeys.GESTURE_SWIPE_RIGHT
        }
        set(key, action.name)
    }

    private suspend fun <T> set(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
