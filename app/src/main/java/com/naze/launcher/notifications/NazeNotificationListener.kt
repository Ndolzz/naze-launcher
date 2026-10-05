package com.naze.launcher.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * SPEC 4.3 — a plain, reflection-free notification summary shown in the shade.
 */
data class NazeNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTimeMillis: Long
)

/**
 * Process-wide store bridging the system [NazeNotificationListener] and Compose.
 * The service pushes full snapshots (never partial deltas) so the UI can never
 * get out of sync with reality.
 */
object NotificationStore {

    private val _notifications = MutableStateFlow<List<NazeNotification>>(emptyList())
    val notifications: StateFlow<List<NazeNotification>> = _notifications

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    /** Set by the service; null when the listener is not connected. */
    var listener: NazeNotificationListener? = null
        private set

    fun isListenerEnabled(): Boolean = listener != null

    internal fun push(list: List<NazeNotification>, connected: Boolean) {
        _notifications.value = list
        _connected.value = connected
    }

    internal fun clearListener() {
        listener = null
        _connected.value = false
        _notifications.value = emptyList()
    }
}

/**
 * The real notification source. Android requires the user to grant notification
 * access in system settings before this service connects — the shade shows an
 * honest "enable access" state until then (SPEC 4.3: honest states everywhere).
 */
class NazeNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        NotificationStore.listener = this
        publish()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) = publish()

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = publish()

    override fun onListenerDisconnected() {
        NotificationStore.clearListener()
    }

    private fun publish() {
        val current = NotificationStore.listener
        if (current != this) return
        val pm = packageManager
        val list = runCatching {
            activeNotifications.orEmpty().mapNotNull { sbn ->
                val extras = sbn.notification?.extras ?: return@mapNotNull null
                val pkg = sbn.packageName ?: return@mapNotNull null
                val appLabel = runCatching {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                }.getOrDefault(pkg)
                NazeNotification(
                    key = sbn.key,
                    packageName = pkg,
                    appLabel = appLabel,
                    title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty(),
                    text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty(),
                    postTimeMillis = sbn.postTime
                )
            }.sortedByDescending { it.postTimeMillis }
        }.getOrDefault(emptyList())
        NotificationStore.push(list, connected = true)
    }

    fun dismiss(key: String) {
        runCatching { cancelNotification(key) }
        publish()
    }

    fun dismissAll() {
        runCatching {
            activeNotifications.orEmpty().forEach { cancelNotification(it.key) }
        }
        publish()
    }
}
