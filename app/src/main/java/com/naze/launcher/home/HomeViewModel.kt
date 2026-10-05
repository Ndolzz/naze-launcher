package com.naze.launcher.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naze.launcher.appdrawer.AppInfo
import com.naze.launcher.appdrawer.AppRepository
import com.naze.launcher.appdrawer.AppSortMode
import com.naze.launcher.core.BatteryStatus
import com.naze.launcher.core.DeviceSnapshot
import com.naze.launcher.core.DeviceStatusMonitor
import com.naze.launcher.core.TimeOfDay
import com.naze.launcher.dock.DockRepository
import com.naze.launcher.location.LocationProvider
import com.naze.launcher.settings.AppSettings
import com.naze.launcher.settings.PreferencesRepository
import com.naze.launcher.theme.Ambience
import com.naze.launcher.theme.ThemeEngine
import com.naze.launcher.weather.OpenWeatherMapApi
import com.naze.launcher.weather.WeatherCondition
import com.naze.launcher.weather.WeatherRepository
import com.naze.launcher.weather.WeatherResult
import com.naze.launcher.weather.TemperatureBand
import com.naze.launcher.weather.toTemperatureBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: AppSettings = AppSettings(),
    val weather: WeatherResult? = null,
    val ambience: Ambience = ThemeEngine.resolve(
        TimeOfDay.current(), WeatherCondition.UNKNOWN,
        TemperatureBand.NORMAL, true, true, true
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
    private val deviceStatusMonitor = DeviceStatusMonitor(application)



    /** Rebuilt whenever the user saves a different OpenWeatherMap key in Settings. */
    private var weatherRepository: WeatherRepository? = null
    private var lastApiKey: String? = null

    val battery: StateFlow<BatteryStatus?> = deviceStatusMonitor.battery
    val torchOn: StateFlow<Boolean> = deviceStatusMonitor.torchOn

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            preferencesRepository.settings.collect { settings ->
                val allApps = if (_uiState.value.allApps.isEmpty()) {
                    appRepository.getAllLaunchableApps()
                } else {
                    _uiState.value.allApps
                }
                val dockApps = allApps.filter { it.packageName in settings.dockPackages }
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    allApps = allApps,
                    dockApps = dockApps
                )
                // If the API key changed, drop the cached repository and re-evaluate weather.
                if (settings.weatherApiKey != lastApiKey) {
                    weatherRepository = settings.weatherApiKey
                        ?.takeIf { it.isNotBlank() }
                        ?.let { WeatherRepository(application, OpenWeatherMapApi(apiKey = it)) }
                    lastApiKey = settings.weatherApiKey
                    refreshWeather()
                }
            }
        }
        refreshWeather()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                recentPackages = appRepository.getRecentPackageNamesSortedByRecency()
            )
        }
    }

    fun startMonitoring() = deviceStatusMonitor.start()
    fun stopMonitoring() = deviceStatusMonitor.stop()

    fun toggleTorch(): Boolean = deviceStatusMonitor.toggleTorch()

    suspend fun deviceSnapshot(): DeviceSnapshot = deviceStatusMonitor.snapshot()

    /** Re-scan installed apps (called from onResume so new installs show up). */
    fun refreshApps() {
        viewModelScope.launch {
            val allApps = appRepository.getAllLaunchableApps()
            _uiState.value = _uiState.value.copy(
                allApps = allApps,
                dockApps = allApps.filter { it.packageName in _uiState.value.settings.dockPackages }
            )
        }
    }

    /** Called on home-screen open/resume — never on a timer/background service. */
    fun refreshWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val settings = _uiState.value.settings
            val repository = weatherRepository

            val result = if (repository == null) {
                // Honest state: no key configured. Show a cached reading if one exists.
                repositoryOrNull()?.peekCache(WeatherResult.Reason.NO_API_KEY)
                    ?: WeatherResult.Unavailable(reason = WeatherResult.Reason.NO_API_KEY)
            } else {
                val point = if (settings.useAutomaticLocation && locationProvider.hasLocationPermission()) {
                    locationProvider.requestSingleLocation()
                } else null

                val lat = point?.latitude ?: settings.manualLat
                val lon = point?.longitude ?: settings.manualLon
                val label = if (point != null) null else settings.manualLocationName

                if (lat != null && lon != null) {
                    repository.getWeather(lat, lon, label, forceRefresh)
                } else {
                    WeatherResult.Unavailable(reason = WeatherResult.Reason.NO_LOCATION)
                }
            }

            val condition = (result as? WeatherResult.Success)?.reading?.condition
                ?: WeatherCondition.UNKNOWN
            val band = (result as? WeatherResult.Success)?.reading?.temperatureCelsius?.toTemperatureBand()
                ?: TemperatureBand.NORMAL

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

    private fun repositoryOrNull(): WeatherRepository? = weatherRepository

    fun launchApp(app: AppInfo) {
        appRepository.launch(app.packageName)
        viewModelScope.launch {
            appRepository.recordLaunch(app.packageName)
            _uiState.value = _uiState.value.copy(
                recentPackages = appRepository.getRecentPackageNamesSortedByRecency()
            )
        }
    }

    fun setDrawerSort(sortMode: AppSortMode) =
        viewModelScope.launch { preferencesRepository.setDrawerSort(sortMode) }

    fun addToDock(app: AppInfo) = viewModelScope.launch { dockRepository.addToDock(app.packageName) }
    fun removeFromDock(app: AppInfo) = viewModelScope.launch { dockRepository.removeFromDock(app.packageName) }
}
