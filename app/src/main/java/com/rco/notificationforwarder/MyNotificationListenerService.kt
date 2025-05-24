package com.rco.notificationforwarder

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.util.Log

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.isOngoing ||
            sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0
        ) return

        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT) ?: ""

        Log.i("NotifForwarder", "${sbn.packageName} → $title : $text")
    }
}
