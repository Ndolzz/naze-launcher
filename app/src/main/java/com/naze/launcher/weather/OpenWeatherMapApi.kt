package com.naze.launcher.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Deliberately implemented with plain [HttpURLConnection] + org.json (both built into
 * the Android platform) instead of pulling in Retrofit/OkHttp/Gson — this is the only
 * network call the whole app makes, so a full HTTP stack isn't worth the APK weight.
 *
 * Get a free key at https://openweathermap.org/api and put it in local.properties /
 * BuildConfig rather than hardcoding it here.
 */
class OpenWeatherMapApi(private val apiKey: String) : WeatherApi {

    override suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherReading =
        withContext(Dispatchers.IO) {
            val url = URL(
                String.format(
                    Locale.US,
                    "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=metric&appid=%s",
                    latitude, longitude, apiKey
                )
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 6_000
            connection.readTimeout = 6_000
            connection.requestMethod = "GET"

            try {
                val code = connection.responseCode
                if (code !in 200..299) {
                    throw WeatherApiException("Weather API returned HTTP $code")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseResponse(body)
            } finally {
                connection.disconnect()
            }
        }

    private fun parseResponse(body: String): WeatherReading {
        val json = JSONObject(body)
        val name = json.optString("name", "Unknown")
        val main = json.getJSONObject("main")
        val temp = main.getDouble("temp")
        val weatherArray = json.getJSONArray("weather")
        val conditionCode = if (weatherArray.length() > 0) {
            weatherArray.getJSONObject(0).optString("main", "")
        } else ""

        return WeatherReading(
            locationName = name,
            temperatureCelsius = temp,
            condition = mapCondition(conditionCode),
            fetchedAtMillis = System.currentTimeMillis()
        )
    }

    private fun mapCondition(owmMain: String): WeatherCondition = when (owmMain.lowercase(Locale.US)) {
        "clear" -> WeatherCondition.CLEAR
        "clouds" -> WeatherCondition.CLOUDY
        "rain", "drizzle" -> WeatherCondition.RAIN
        "thunderstorm" -> WeatherCondition.STORM
        "fog", "mist", "haze" -> WeatherCondition.FOG
        "snow" -> WeatherCondition.SNOW
        else -> WeatherCondition.UNKNOWN
    }
}
