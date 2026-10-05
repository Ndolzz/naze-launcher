package com.naze.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.naze.launcher.core.SystemIntent
import com.naze.launcher.home.HomeScreen
import com.naze.launcher.home.HomeViewModel
import com.naze.launcher.lock.LockScreenActivity
import com.naze.launcher.onboarding.OnboardingScreen
import com.naze.launcher.settings.PreferencesRepository
import com.naze.launcher.settings.SettingsActivity
import com.naze.launcher.theme.NazeLauncherTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var preferencesRepository: PreferencesRepository

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Regardless of outcome, the app degrades gracefully — see LocationProvider. */
        homeViewModel.refreshWeather(forceRefresh = true)
    }

    private val requestDefaultLauncherLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* No-op: user may or may not have picked Naze; MainActivity simply proceeds either way. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepository = PreferencesRepository(applicationContext)

        setContent {
            NazeLauncherTheme {
                val settings by preferencesRepository.settings.collectAsState(initial = null)

                when (settings?.onboardingComplete) {
                    null -> Unit // still loading initial DataStore read
                    false -> OnboardingScreen(
                        onRequestDefaultLauncher = ::requestDefaultLauncher,
                        onRequestLocationPermission = ::launchLocationPermissionRequest,
                        onFinish = {
                            lifecycleScope.launch { preferencesRepository.setOnboardingComplete(true) }
                        }
                    )
                    true -> HomeScreen(
                        viewModel = homeViewModel,
                        onOpenSettings = ::openSettings,
                        onExpandNotifications = ::expandNotifications,
                        onSystemAction = ::performSystemAction,
                        onOpenLock = ::openNazeLock
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Battery receiver + torch callback only live while the launcher is visible.
        homeViewModel.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.stopMonitoring()
    }

    override fun onResume() {
        super.onResume()
        // New installs/uninstalls show up immediately; weather is interval-gated
        // inside WeatherRepository, so this call stays cheap even on every resume.
        homeViewModel.refreshApps()
        homeViewModel.refreshWeather()
    }

    override fun onBackPressed() {
        // A launcher's Home screen is the root of the task stack — pressing back here
        // should do nothing, matching stock launcher behavior (prevents accidental exits).
        // Overlay/sheet dismissal is handled inside HomeScreen via BackHandler.
    }

    private fun openNazeLock() {
        startActivity(Intent(this, LockScreenActivity::class.java))
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /**
     * Real notification-shade expansion via StatusBarManager — permitted for the
     * HOME role holder, which a launcher by definition is (or is asking to be).
     */
    private fun expandNotifications() {
        val statusBar = getSystemService(StatusBarManager::class.java)
        runCatching { statusBar?.expandNotificationsPanel() }
    }

    /** Every [SystemIntent] maps to a real system capability — no dead ends. */
    private fun performSystemAction(action: SystemIntent, packageName: String?) {
        val intent = when (action) {
            SystemIntent.WIFI_PANEL ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
            SystemIntent.BLUETOOTH_SETTINGS -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            SystemIntent.WALLPAPER_PICKER -> Intent(Intent.ACTION_SET_WALLPAPER)
            SystemIntent.APP_INFO -> {
                if (packageName == null) return
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
            }
        }
        runCatching {
            val toStart = if (action == SystemIntent.WALLPAPER_PICKER) {
                Intent.createChooser(intent, "Set wallpaper")
            } else {
                intent
            }
            startActivity(toStart)
        }
    }

    private fun requestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    requestDefaultLauncherLegacy.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                    return
                }
            }
        }
        // Pre-Q fallback: send the user to the system "Home app" chooser.
        requestDefaultLauncherLegacy.launch(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    private fun launchLocationPermissionRequest() {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}
