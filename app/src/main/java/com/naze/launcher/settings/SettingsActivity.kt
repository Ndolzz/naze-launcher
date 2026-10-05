package com.naze.launcher.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.lock.LockScreenActivity
import com.naze.launcher.theme.NazeLauncherTheme
import com.naze.launcher.theme.ThemeEngine
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.WeatherCondition
import kotlinx.coroutines.launch

/**
 * Settings runs in its own task (as the system back gesture dismisses it
 * independently of the home screen) and shares the DataStore-backed settings flow
 * with the launcher, so any change is reflected on the home screen the moment it
 * is made.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PreferencesRepository(applicationContext)

        setContent {
            NazeLauncherTheme {
                val settings by repository.settings.collectAsState(initial = AppSettings())
                val ambience = remember(settings.dynamicTimeEnabled) {
                    ThemeEngine.resolve(
                        timeOfDay = TimeOfDay.current(),
                        condition = WeatherCondition.UNKNOWN,
                        temperatureBand = TemperatureBand.NORMAL,
                        dynamicWeatherEnabled = false,
                        dynamicTemperatureEnabled = false,
                        dynamicTimeEnabled = settings.dynamicTimeEnabled
                    )
                }

                SettingsScreen(
                    settings = settings,
                    ambience = ambience,
                    actions = SettingsActions(
                        setDynamicWeather = { v -> lifecycleScope.launch { repository.setDynamicWeatherEnabled(v) } },
                        setDynamicTemperature = { v -> lifecycleScope.launch { repository.setDynamicTemperatureEnabled(v) } },
                        setDynamicTime = { v -> lifecycleScope.launch { repository.setDynamicTimeEnabled(v) } },
                        setPerformanceMode = { v -> lifecycleScope.launch { repository.setPerformanceMode(v) } },
                        setClock24Hour = { v -> lifecycleScope.launch { repository.setClock24Hour(v) } },
                        setClockShowSeconds = { v -> lifecycleScope.launch { repository.setClockShowSeconds(v) } },
                        setClockSizeScale = { v -> lifecycleScope.launch { repository.setClockSizeScale(v) } },
                        setLockClockStyle = { v -> lifecycleScope.launch { repository.setLockClockStyle(v) } },
                        openNazeLock = { startActivity(Intent(this, LockScreenActivity::class.java)) },
                        setDrawerSort = { v -> lifecycleScope.launch { repository.setDrawerSort(v) } },
                        setDrawerColumns = { v -> lifecycleScope.launch { repository.setDrawerColumns(v) } },
                        setShowAppLabels = { v -> lifecycleScope.launch { repository.setShowAppLabels(v) } },
                        setAutomaticLocation = { v -> lifecycleScope.launch { repository.setUseAutomaticLocation(v) } },
                        setFahrenheit = { v -> lifecycleScope.launch { repository.setTempUnitFahrenheit(v) } },
                        setWeatherApiKey = { v -> lifecycleScope.launch { repository.setWeatherApiKey(v) } },
                        setManualLocation = { name, lat, lon ->
                            lifecycleScope.launch { repository.setManualLocation(name, lat, lon) }
                        },
                        setGestureAction = { trigger, action ->
                            lifecycleScope.launch { repository.setGestureAction(trigger,

 action) }
                        }
                    ),
                    onBack = { finish() },
                    versionName = "0.2.0"
                )
            }
        }
    }
}
