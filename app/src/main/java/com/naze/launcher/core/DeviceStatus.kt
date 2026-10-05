package com.naze.launcher.core

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File

data class BatteryStatus(
    val levelPercent: Int,
    val charging: Boolean,
    val temperatureCelsius: Float?
)

data class DeviceSnapshot(
    val battery: BatteryStatus?,
    val ramTotalBytes: Long,
    val ramAvailableBytes: Long,
    val storageTotalBytes: Long,
    val storageAvailableBytes: Long,
    val androidVersion: String,
    val deviceModel: String
)

/**
 * Live device status with the smallest possible footprint:
 *  - battery: a single receiver for the sticky ACTION_BATTERY_CHANGED system broadcast
 *    (system broadcasts are exempt from the API-33 export flags; the sticky intent fires
 *    immediately on registration, so we get the initial value for free);
 *  - torch (flashlight): CameraManager.TorchCallback — no permission required for
 *    setTorchMode, and it is only registered while the launcher is alive.
 *
 * RAM/storage are read on demand via [snapshot] (cheap statfs/ActivityManager calls
 * kept off the main thread) rather than polled continuously.
 */
class DeviceStatusMonitor(private val context: Context) {

    private val _battery = MutableStateFlow<BatteryStatus?>(null)
    val battery: StateFlow<BatteryStatus?> = _battery

    private val _torchOn = MutableStateFlow(false)
    val torchOn: StateFlow<Boolean> = _torchOn

    private var batteryReceiver: BroadcastReceiver? = null
    private var torchCallback: CameraManager.TorchCallback? = null
    private var flashCameraId: String? = null

    private val cameraManager: CameraManager
        get() = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    fun start() {
        if (batteryReceiver == null) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, intent: Intent?) {
                    intent?.let { _battery.value = parseBattery(it) }
                }
            }
            batteryReceiver = receiver
            runCatching { context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) }
        }
        if (torchCallback == null) {
            runCatching {
                flashCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    cameraManager.getCameraCharacteristics(id)
                        .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                val callback = object : CameraManager.TorchCallback() {
                    override fun onTorchModeChanged(id: String, enabled: Boolean) {
                        _torchOn.value = enabled
                    }
                }
                torchCallback = callback
                cameraManager.registerTorchCallback(callback, null)
            }
        }
    }

    fun stop() {
        batteryReceiver?.let { receiver ->
            runCatching { context.unregisterReceiver(receiver) }
            batteryReceiver = null
        }
        torchCallback?.let { callback ->
            runCatching { cameraManager.unregisterTorchCallback(callback) }
            torchCallback = null
        }
    }

    /** @return true if the device has a flash unit and a toggle was attempted. */
    fun toggleTorch(): Boolean {
        val id = flashCameraId ?: return false
        runCatching { cameraManager.setTorchMode(id, !_torchOn.value) }
        return true
    }

    suspend fun snapshot(): DeviceSnapshot = withContext(Dispatchers.IO) {
        val activityManager = context.get
SystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val dataDir: File = Environment.getDataDirectory()
        val stat = StatFs(dataDir.path)

        DeviceSnapshot(
            battery = _battery.value,
            ramTotalBytes = memoryInfo.totalMem,
            ramAvailableBytes = memoryInfo.availMem,
            storageTotalBytes = stat.totalBytes,
            storageAvailableBytes = stat.availableBytes,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = Build.MODEL ?: "Unknown device"
        )
    }

    companion object {
        fun parseBattery(intent: Intent): BatteryStatus {
            val level = intent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            val tempRaw = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            val charging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL
            val temperature = if (tempRaw != Int.MIN_VALUE) tempRaw / 10f else null
            return BatteryStatus(percent, charging, temperature)
        }
    }
}
