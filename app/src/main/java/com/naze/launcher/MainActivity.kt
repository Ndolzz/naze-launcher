package com.naze.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.graphics.Color as AColor
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.naze.launcher.core.SystemIntent
import com.naze.launcher.crash.CrashReportScreen
import com.naze.launcher.home.HomeScreen
import com.naze.launcher.home.HomeViewModel
import com.naze.launcher.lock.LockScreenActivity
import com.naze.launcher.onboarding.OnboardingScreen
import com.naze.launcher.settings.PreferencesRepository
import com.naze.launcher.settings.SettingsActivity
import com.naze.launcher.theme.NazeLauncherTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var preferencesRepository: PreferencesRepository

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { homeViewModel.refreshWeather(forceRefresh = true) }

    private val requestDefaultLauncherLegacy = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the previous launch crashed, show the stack trace with plain Android
        // Views (NOT Compose/Material) so the reporter itself can never crash-loop.
        // The file is deleted BEFORE rendering so a broken reporter cannot loop.
        val crashFile = File(filesDir, NazeApplication.CRASH_FILE)
        if (crashFile.exists()) {
            val trace = runCatching { crashFile.readText() }.getOrDefault("(could not read crash file)")
            runCatching { crashFile.delete() }
            showCrashReportView(trace)
            return
        }

        preferencesRepository = PreferencesRepository(applicationContext)

        setContent {
            NazeLauncherTheme {
                val settings by preferencesRepository.settings.collectAsState(initial = null)

                when (settings?.onboardingComplete) {
                    null -> Unit
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

    /**
     * Zero-dependency crash viewer: classic Views only. Screenshot it and share
     * the trace so the root cause can be fixed.
     */
    private fun showCrashReportView(trace: String) {
        val scroll = android.widget.ScrollView(this).apply {
            setBackgroundColor(AColor.parseColor("#10131A"))
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
        }
        val title = TextView(this).apply {
            text = "Naze Launcher stopped"
            setTextColor(AColor.parseColor("#FF6B6B"))
            textSize = 18f
        }
        val hint = TextView(this).apply {
            text = "Screenshot this screen and share it to debug:"
            setTextColor(AColor.parseColor("#B8BFCC"))
            textSize = 13f
        }
        val body = TextView(this).apply {
            text = trace
            setTextColor(AColor.parseColor("#D8DDE6"))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val button = android.widget.Button(this).apply {
            text = "Clear and try again"
            setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }
        layout.addView(title)
        layout.addView(hint)
        layout.addView(body)
        layout.addView(button)
        scroll.addView(layout)
        setContentView(scroll)
    }

    override fun onStart() {
        super.onStart()
        homeViewModel.startMonitoring()
    }

    override fun onStop() {
        super.onStop()
        homeViewModel.stopMonitoring()
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.refreshApps()
        homeViewModel.refreshWeather()
    }

    override fun onBackPressed() {
        // Home is the root of the task stack — back does nothing here.
    }

    private fun openNazeLock() {
        startActivity(Intent(this, LockScreenActivity::class.java))
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun expandNotifications() {
        runCatching {
            val service = getSystemService("statusbar") ?: return
            service.javaClass.getMethod("expandNotificationsPanel").invoke(service)
        }
    }

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
                    Uri.parse("package:" + packageName)
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
