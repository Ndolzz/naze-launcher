package com.naze.launcher.lock

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.naze.launcher.MainActivity
import com.naze.launcher.core.DeviceStatusMonitor
import com.naze.launcher.settings.PreferencesRepository
import com.naze.launcher.theme.NazeLauncherTheme
import kotlinx.coroutines.launch

/**
 * Naze Lock — a glanceable clock screen that may be shown over the system lock screen
 * (showWhenLocked / turnScreenOn in the manifest).
 *
 * It does NOT replace the system keyguard and holds no credentials of its own: unlocking
 * calls [KeyguardManager.requestDismissKeyguard], so the user's real PIN / pattern /
 * biometric is what gates access. Every control here is wired to a real capability.
 */
class LockScreenActivity : ComponentActivity() {

    private lateinit var deviceStatus: DeviceStatusMonitor
    private lateinit var preferences: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        deviceStatus = DeviceStatusMonitor(applicationContext)
        preferences = PreferencesRepository(applicationContext)

        setContent {
            NazeLauncherTheme {
                val settings by preferences.settings.collectAsState(initial = null)
                val battery by deviceStatus.battery.collectAsState()
                val torchOn by deviceStatus.torchOn.collectAsState()

                settings?.let { s ->
                    NazeLockScreen(
                        clockStyle = s.lockClockStyle,
                        clock24Hour = s.clock24Hour,
                        performanceMode = s.performanceMode,
                        battery = battery,
                        torchOn = torchOn,
                        onToggleTorch = { deviceStatus.toggleTorch() },
                        onOpenCamera = ::openCamera,
                        onCycleClock = {
                            lifecycleScope.launch { preferences.setLockClockStyle(s.lockClockStyle.next()) }
                        },
                        onUnlock = ::unlock
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        deviceStatus.start()
    }

    override fun onStop() {
        super.onStop()
        deviceStatus.stop()
    }

    private val keyguard: KeyguardManager?
        get() = getSystemService(KeyguardManager::class.java)

    /** Hands off to the system unlock (real PIN/biometric), then opens the home screen. */
    private fun unlock() {
        val km = keyguard
        if (km != null && km.isKeyguardLocked) {
            km.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() = goHome()
            })
        } else {
            goHome()
        }
    }

    private fun goHome() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }

    /** While locked, only the secure camera intent is allowed to open over the keyguard. */
    private fun openCamera() {
        val action = if (keyguard?.isKeyguardLocked == true) {
            MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE
        } else {
            MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
        }
        runCatching { startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
