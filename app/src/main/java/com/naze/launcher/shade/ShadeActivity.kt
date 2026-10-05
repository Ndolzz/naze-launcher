package com.naze.launcher.shade

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.naze.launcher.core.SystemIntent
import com.naze.launcher.theme.NazeLauncherTheme

/**
 * SPEC 4.3 — the quick shade, as its own translucent-feel activity so opening it
 * never disturbs the home screen's state (same pattern as Settings/Lock).
 * Opened by the notification gesture (swipe down by default).
 */
class ShadeActivity : ComponentActivity() {

    private val shadeViewModel: ShadeViewModel by viewModels()

    private var brightnessPercent by mutableIntStateOf(initialBrightnessPercent())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NazeLauncherTheme {
                val battery by shadeViewModel.battery.collectAsState()
                val torchOn by shadeViewModel.torchOn.collectAsState()
                val notifications by shadeViewModel.notifications.collectAsState()
                val accessGranted by shadeViewModel.notificationAccessGranted.collectAsState()

                QuickShadeScreen(
                    battery = battery,
                    torchOn = torchOn,
                    notifications = notifications,
                    notificationAccessGranted = accessGranted,
                    brightnessPercent = brightnessPercent,
                    onBrightnessChange = { percent ->
                        brightnessPercent = percent
                        applyBrightness(percent)
                    },
                    onToggleTorch = { shadeViewModel.toggleTorch() },
                    onOpenWifiPanel = { performSystemAction(SystemIntent.WIFI_PANEL) },
                    onOpenBluetooth = { performSystemAction(SystemIntent.BLUETOOTH_SETTINGS) },
                    onOpenWallpaperPicker = { performSystemAction(SystemIntent.WALLPAPER_PICKER) },
                    onOpenSettings = {
                        startActivity(Intent(this, com.naze.launcher.settings.SettingsActivity::class.java))
                        finish()
                    },
                    onOpenNotificationAccess = {
                        runCatching {
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                    },
                    onDismissNotification = { shadeViewModel.dismissNotification(it) },
                    onDismissAll = { shadeViewModel.dismissAll() },
                    onClose = { finish() }
                )
            }
        }
    }

    /**
     * Real brightness: always applies to this window instantly; also writes the
     * system setting when the user has granted WRITE_SETTINGS.
     */
    private fun applyBrightness(percent: Int) {
        val fraction = percent / 100f
        window.attributes = window.attributes.apply {
            screenBrightness = fraction.coerceIn(0.2f, 1f)
        }
        runCatching {
            if (Settings.System.canWrite(this)) {
                val value = (255 * fraction).toInt().coerceIn(20, 255)
                Settings.System.putInt(
                    contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    value
                )
            }
        }
    }

    private fun initialBrightnessPercent(): Int = runCatching {
        val raw = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        ((raw * 100) / 255).coerceIn(20, 100)
    }.getOrDefault(80)

    private fun performSystemAction(action: SystemIntent) {
        val intent = when (action) {
            SystemIntent.WIFI_PANEL ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
            SystemIntent.BLUETOOTH_SETTINGS -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            SystemIntent.WALLPAPER_PICKER -> Intent(Intent.ACTION_SET_WALLPAPER)
            SystemIntent.APP_INFO -> null
        } ?: return
        runCatching {
            startActivity(
                if (action == SystemIntent.WALLPAPER_PICKER) {
                    Intent.createChooser(intent, "Set wallpaper")
                } else {
                    intent
                }
            )
        }
    }
}
