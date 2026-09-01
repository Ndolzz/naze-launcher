package com.naze.launcher.storage

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Single source of truth for every DataStore key used across the app.
 * Keeping them here avoids typo-drift between the repository classes that read/write them.
 */
object PreferencesKeys {
    // --- Weather cache ---
    val WEATHER_LOCATION_NAME = stringPreferencesKey("weather_location_name")
    val WEATHER_TEMP_C = doublePreferencesKey("weather_temp_c")
    val WEATHER_CONDITION = stringPreferencesKey("weather_condition")
    val WEATHER_FETCHED_AT = longPreferencesKey("weather_fetched_at")

    // --- Location settings ---
    val USE_AUTOMATIC_LOCATION = booleanPreferencesKey("use_automatic_location")
    val MANUAL_LOCATION_NAME = stringPreferencesKey("manual_location_name")
    val MANUAL_LAT = doublePreferencesKey("manual_lat")
    val MANUAL_LON = doublePreferencesKey("manual_lon")

    // --- Appearance ---
    val DYNAMIC_WEATHER_ENABLED = booleanPreferencesKey("dynamic_weather_enabled")
    val DYNAMIC_TEMPERATURE_ENABLED = booleanPreferencesKey("dynamic_temperature_enabled")
    val DYNAMIC_TIME_ENABLED = booleanPreferencesKey("dynamic_time_enabled")
    val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
    val BATTERY_SAVER = booleanPreferencesKey("battery_saver")

    // --- Clock ---
    val CLOCK_24_HOUR = booleanPreferencesKey("clock_24_hour")
    val CLOCK_SHOW_SECONDS = booleanPreferencesKey("clock_show_seconds")
    val CLOCK_SIZE_SCALE = floatPreferencesKey("clock_size_scale")

    // --- Weather settings ---
    val TEMP_UNIT_FAHRENHEIT = booleanPreferencesKey("temp_unit_fahrenheit")

    // --- Onboarding ---
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

    // --- Gestures (store the enum name of the assigned action) ---
    val GESTURE_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
    val GESTURE_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
    val GESTURE_DOUBLE_TAP = stringPreferencesKey("gesture_double_tap")
    val GESTURE_SWIPE_LEFT = stringPreferencesKey("gesture_swipe_left")
    val GESTURE_SWIPE_RIGHT = stringPreferencesKey("gesture_swipe_right")

    // --- Dock ---
    val DOCK_APPS = stringPreferencesKey("dock_apps") // comma-separated package names
}
