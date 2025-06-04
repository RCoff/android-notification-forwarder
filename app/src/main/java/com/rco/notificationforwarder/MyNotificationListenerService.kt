package com.rco.notificationforwarder

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onListenerConnected() {
        Log.d("NotificationForwarder", "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotificationForwarder", "Notification posted")
        val app = sbn.packageName
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: ""

        if (sbn.isOngoing ||
            sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        ) return

        Log.d("NotificationForwarder", "$app → $title : $text")

        // Forward if configured
        serviceScope.launch {
            val configs = ConfiguredNotificationRepository.loadNotifications(applicationContext)
            configs.forEach { config ->
                if (config.sources.contains(app)) {
                    config.targets.forEach { target ->
                        if (target is HTTPTarget) {
                            try {
                                val payload = Json.encodeToString(
                                    mapOf(
                                        "package" to app,
                                        "title" to title,
                                        "text" to text
                                    )
                                )
                                val responseCode = makeHttpRequest(
                                    target.url,
                                    target.method,
                                    target.headers,
                                    payload
                                )
                                Log.d(
                                    "NotificationForwarder",
                                    "HTTP forward response: $responseCode"
                                )
                            } catch (e: Exception) {
                                Log.e("NotificationForwarder", "HTTP forward failed: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
