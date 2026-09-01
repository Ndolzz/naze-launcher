package com.naze.launcher.weather

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.naze.launcher.core.Constants
import com.naze.launcher.storage.PreferencesKeys
import com.naze.launcher.storage.dataStore
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * The Weather Engine described in the spec: fetches, caches, and hands out a
 * [WeatherResult] that the theme layer consumes. It never polls in the background —
 * [getWeather] is only ever called from the home screen's lifecycle (on open / on
 * resume after the min refresh interval has elapsed).
 */
class WeatherRepository(
    private val context: Context,
    private val api: WeatherApi
) {
    /**
     * @param forceRefresh bypasses the interval check (used by a manual "refresh" action),
     *   but still respects the cache on network failure.
     */
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        locationLabel: String?,
        forceRefresh: Boolean = false
    ): WeatherResult {
        val cached = readCache()
        val cacheIsFreshEnough = cached != null &&
            (System.currentTimeMillis() - cached.fetchedAtMillis) < Constants.WEATHER_MIN_REFRESH_INTERVAL_MS

        if (!forceRefresh && cacheIsFreshEnough) {
            return WeatherResult.Success(cached!!.copy(isFromCache = true))
        }

        return try {
            val fresh = api.fetchWeather(latitude, longitude).let {
                if (locationLabel != null) it.copy(locationName = locationLabel) else it
            }
            writeCache(fresh)
            WeatherResult.Success(fresh)
        } catch (e: IOException) {
            Log.w(TAG, "Weather fetch failed (network), falling back to cache", e)
            fallbackToCache(cached)
        } catch (e: WeatherApiException) {
            Log.w(TAG, "Weather fetch failed (API error), falling back to cache", e)
            fallbackToCache(cached)
        }
    }

    private fun fallbackToCache(cached: WeatherReading?): WeatherResult {
        return if (cached != null) {
            WeatherResult.Success(cached.copy(isFromCache = true))
        } else {
            WeatherResult.Unavailable(lastKnown = null)
        }
    }

    private suspend fun readCache(): WeatherReading? {
        val prefs = context.dataStore.data.first()
        val fetchedAt = prefs[PreferencesKeys.WEATHER_FETCHED_AT] ?: return null
        if (System.currentTimeMillis() - fetchedAt > Constants.WEATHER_CACHE_MAX_AGE_MS) {
            // Too old to show with confidence, but the caller can still decide to use it
            // as a last resort via Unavailable(lastKnown).
        }
        val name = prefs[PreferencesKeys.WEATHER_LOCATION_NAME] ?: return null
        val temp = prefs[PreferencesKeys.WEATHER_TEMP_C] ?: return null
        val conditionName = prefs[PreferencesKeys.WEATHER_CONDITION] ?: return null
        val condition = runCatching { WeatherCondition.valueOf(conditionName) }
            .getOrDefault(WeatherCondition.UNKNOWN)

        return WeatherReading(
            locationName = name,
            temperatureCelsius = temp,
            condition = condition,
            fetchedAtMillis = fetchedAt,
            isFromCache = true
        )
    }

    private suspend fun writeCache(reading: WeatherReading) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.WEATHER_LOCATION_NAME] = reading.locationName
            prefs[PreferencesKeys.WEATHER_TEMP_C] = reading.temperatureCelsius
            prefs[PreferencesKeys.WEATHER_CONDITION] = reading.condition.name
            prefs[PreferencesKeys.WEATHER_FETCHED_AT] = reading.fetchedAtMillis
        }
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}
