package com.rco.notificationforwarder

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.util.Log

class MyNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
//        super.onListenerConnected()
        Log.d("NotificationForwarder", "Notification listener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d("NotificationForwarder", "Notification posted")
        val extras = sbn.notification.extras
        val app = sbn.packageName
        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: ""

        if (sbn.isOngoing ||
            sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        ) return

        Log.d("NotificationForwarder", "${sbn.packageName} → $title : $text")
    }
}
