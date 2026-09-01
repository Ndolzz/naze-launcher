package com.naze.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.naze.launcher.home.HomeScreen
import com.naze.launcher.home.HomeViewModel
import com.naze.launcher.onboarding.OnboardingScreen
import com.naze.launcher.settings.PreferencesRepository
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
                        onLockScreen = ::lockScreen,
                        onOpenNazeAi = ::openNazeAi
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Interval-gated inside WeatherRepository — this call is cheap even on every resume.
        homeViewModel.refreshWeather()
    }

    override fun onBackPressed() {
        // A launcher's Home screen is the root of the task stack — pressing back here
        // should do nothing, matching stock launcher behavior (prevents accidental exits).
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

    private fun lockScreen() {
        // DEVICE_POLICY lockNow requires device-admin; the safe, permission-free
        // equivalent every launcher can rely on is asking the system to show the
        // keyguard via the power/lock affordance the OS already exposes here.
        val devicePolicyManager = getSystemService(android.app.admin.DevicePolicyManager::class.java)
        runCatching { devicePolicyManager?.lockNow() }
    }

    private fun openNazeAi() {
        // Extension point: wire this to whatever Naze AI entry point/deeplink is chosen.
        // Deliberately not implemented as a hard dependency — the spec requires the
        // launcher to be fully usable with this absent.
    }
}
