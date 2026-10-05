package com.naze.launcher.weather

/**
 * Normalized weather condition — the UI/theme layer only ever reasons about this enum,
 * never about a specific weather provider's raw codes. This is what makes the weather
 * API swappable (see [WeatherApi]).
 */
enum class WeatherCondition {
    CLEAR, CLOUDY, RAIN, STORM, FOG, SNOW, UNKNOWN
}

data class WeatherReading(
    val locationName: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val fetchedAtMillis: Long,
    val isFromCache: Boolean = false
)

enum class TemperatureBand { LOW, NORMAL, HIGH }

fun Double.toTemperatureBand(): TemperatureBand = when {
    this < 15.0 -> TemperatureBand.LOW
    this > 30.0 -> TemperatureBand.HIGH
    else -> TemperatureBand.NORMAL
}

sealed class WeatherResult {
    data class Success(val reading: WeatherReading) : WeatherResult()

    /**
     * @param reason an honest explanation the UI can show (and act on) — e.g. a
     * missing API key links to Settings instead of pretending weather is loading.
     */
    data class Unavailable(
        val lastKnown: WeatherReading? = null,
        val reason: Reason = Reason.UNKNOWN
    ) : WeatherResult()

    enum class Reason { NO_API_KEY, NO_LOCATION, NETWORK, UNKNOWN }
}
