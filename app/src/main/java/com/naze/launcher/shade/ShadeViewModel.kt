package com.naze.launcher.shade

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naze.launcher.core.DeviceStatusMonitor
import com.naze.launcher.notifications.NazeNotification
import com.naze.launcher.notifications.NotificationStore
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * SPEC 4.3 — state for the quick shade: real battery, real torch, real
 * notifications. Every field is backed by a live system source; nothing is mocked.
 */
class ShadeViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceStatusMonitor = DeviceStatusMonitor(application)

    val battery = deviceStatusMonitor.battery
    val torchOn = deviceStatusMonitor.torchOn
    val notifications: StateFlow<List<NazeNotification>> = NotificationStore.notifications
    val notificationAccessGranted = NotificationStore.connected

    init {
        deviceStatusMonitor.start()
    }

    override fun onCleared() {
        deviceStatusMonitor.stop()
        super.onCleared()
    }

    fun toggleTorch(): Boolean = deviceStatusMonitor.toggleTorch()

    fun dismissNotification(key: String) {
        NotificationStore.listener?.dismiss(key)
    }

    fun dismissAll() {
        NotificationStore.listener?.dismissAll()
        viewModelScope.launch {
            // Optimistically clear the UI; the service republishes the truth.
            NotificationStore.push(emptyList(), true)
        }
    }
}
