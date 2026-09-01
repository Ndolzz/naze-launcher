package com.naze.launcher.weather

/**
 * Abstraction over "some weather provider". Swap OpenWeatherMapApi for any other
 * implementation without touching WeatherRepository or the UI/theme layers.
 */
interface WeatherApi {
    /**
     * @throws java.io.IOException on network failure
     * @throws WeatherApiException on a valid-but-unsuccessful API response
     */
    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherReading
}

class WeatherApiException(message: String) : Exception(message)
