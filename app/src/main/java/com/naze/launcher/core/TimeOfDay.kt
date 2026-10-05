package com.naze.launcher.core

import java.util.Calendar

/**
 * Segment of the day, derived purely from the device's local clock.
 * Never hardcode a timezone — always read from [Calendar.getInstance] (default TZ).
 */
enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, NIGHT;

    companion object {
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
            in 5..10 -> MORNING
            in 11..15 -> AFTERNOON
            in 16..18 -> EVENING
            else -> NIGHT // 19:00–04:59
        }

        fun current(): TimeOfDay {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return fromHour(hour)
        }
    }
}
