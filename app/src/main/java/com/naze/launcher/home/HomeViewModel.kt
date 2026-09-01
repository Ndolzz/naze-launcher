package com.naze.launcher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naze.launcher.appdrawer.AppInfo
import com.naze.launcher.appdrawer.AppRepository
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.dock.DockRepository
import com.naze.launcher.location.LocationProvider
import com.naze.launcher.settings.AppSettings
import com.naze.launcher.settings.PreferencesRepository
import com.naze.launcher.theme.Ambience
import com.naze.launcher.theme.ThemeEngine
import com.naze.launcher.weather.OpenWeatherMapApi
import com.naze.launcher.weather.WeatherRepository
import com.naze.launcher.weather.WeatherResult
import com.naze.launcher.weather.toTemperatureBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val weather: WeatherResult? = null,
    val ambience: Ambience = ThemeEngine.resolve(
        TimeOfDay.current(), com.naze.launcher.weather.WeatherCondition.UNKNOWN,
        com.naze.launcher.weather.TemperatureBand.NORMAL, true, true, true
    ),
    val allApps: List<AppInfo> = emptyList(),
    val dockApps: List<AppInfo> = emptyList(),
    val recentPackages: List<String> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val appRepository = AppRepository(application)
    private val dockRepository = DockRepository(application)
    private val locationProvider = LocationProvider(application)

    // NOTE: supply a real API key via BuildConfig/local.properties before shipping.
    private val weatherRepository = WeatherRepository(application, OpenWeatherMapApi(apiKey = "YOUR_API_KEY"))

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(preferencesRepository.settings, _uiState) { settings, state -> settings to state }
                .collect { (settings, _) ->
                    val allApps = if (_uiState.value.allApps.isEmpty()) loadApps() else _uiState.value.allApps
                    val dockApps = allApps.filter { it.packageName in settings.dockPackages }
                    _uiState.value = _uiState.value.copy(settings = settings, allApps = allApps, dockApps = dockApps)
                }
        }
        refreshWeather()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recentPackages = appRepository.getRecentPackageNamesSortedByRecency())
        }
    }

    private suspend fun loadApps() = appRepository.getAllLaunchableApps()

    /** Called on home-screen open/resume — never on a timer/background service. */
    fun refreshWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            val point = if (settings.useAutomaticLocation && locationProvider.hasLocationPermission()) {
                locationProvider.requestSingleLocation()
            } else null

            val lat = point?.latitude ?: settings.manualLat
            val lon = point?.longitude ?: settings.manualLon
            val label = if (point != null) null else settings.manualLocationName

            val result = if (lat != null && lon != null) {
                weatherRepository.getWeather(lat, lon, label, forceRefresh)
            } else {
                WeatherResult.Unavailable(lastKnown = null)
            }

            val condition = (result as? WeatherResult.Success)?.reading?.condition
                ?: com.naze.launcher.weather.WeatherCondition.UNKNOWN
            val band = (result as? WeatherResult.Success)?.reading?.temperatureCelsius?.toTemperatureBand()
                ?: com.naze.launcher.weather.TemperatureBand.NORMAL

            val ambience = ThemeEngine.resolve(
                timeOfDay = TimeOfDay.current(),
                condition = condition,
                temperatureBand = band,
                dynamicWeatherEnabled = settings.dynamicWeatherEnabled,
                dynamicTemperatureEnabled = settings.dynamicTemperatureEnabled,
                dynamicTimeEnabled = settings.dynamicTimeEnabled
            )

            _uiState.value = _uiState.value.copy(weather = result, ambience = ambience)
        }
    }

    fun launchApp(app: AppInfo) {
        appRepository.launch(app.packageName)
        viewModelScope.launch { appRepository.recordLaunch(app.packageName) }
    }

    fun addToDock(app: AppInfo) = viewModelScope.launch { dockRepository.addToDock(app.packageName) }
    fun removeFromDock(app: AppInfo) = viewModelScope.launch { dockRepository.removeFromDock(app.packageName) }
}
