package com.naze.launcher.core

object Constants {
    // Weather is refreshed at most this often — never poll continuously.
    const val WEATHER_MIN_REFRESH_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    // A cached reading older than this is still shown, just marked as possibly stale.
    const val WEATHER_CACHE_MAX_AGE_MS = 3 * 60 * 60 * 1000L // 3 hours

    // Single-shot location request timeout; we never keep GPS listening in the background.
    const val LOCATION_REQUEST_TIMEOUT_MS = 8_000L

    const val PREFS_NAME = "naze_launcher_prefs"

    const val DEFAULT_DOCK_SLOTS = 4
}
