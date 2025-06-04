package com.rco.notificationforwarder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.rco.notificationforwarder.ui.theme.NotificationForwarderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission if needed (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted, request it
                Log.d("NotificationForwarder", "Requesting notification permission")
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        if (!hasNotificationAccess(this)) {
            // Open notification listener settings
            Log.d("NotificationForwarder", "Requesting notification access")
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(this, intent, null)
            } catch (e: ActivityNotFoundException) {
                Log.e("NotificationForwarder", "Settings activity not found")
            }
        }

        setContent {
            NotificationForwarderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier.padding(16.dp)) {
        GreetingWithButton(name = "Android")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            context.startActivity(Intent(context, ConfiguredNotificationsActivity::class.java))
        }) {
            Text("Show Configured Notifications")
        }
        Spacer(Modifier.height(16.dp))
        SendTestHTTPRequestButton()
    }
}

@Composable
fun GreetingWithButton(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(onClick = { sendLocalTest(context) }) {
        Text("Send Test Notification")
    }
}

@Composable
fun SendTestHTTPRequestButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(onClick = {
        val activity = context as? ComponentActivity
        if (activity != null &&
            (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE),
                1002 // Unique request code for network permissions
            )
            Log.d("NotificationForwarder", "Requesting network permissions")
        } else {
            Log.d("NotificationForwarder", "Test HTTP request button clicked")
            Thread {
                val responseCode = makeHttpRequest(
                    "http://192.168.10.131", "POST",
                    mapOf("Content-Type" to "application/json"),
                    """{"key":"value"}"""
                )
                Log.d("NotificationForwarder", "HTTP Request returned code: $responseCode")
            }.start()
        }
    }, modifier = modifier) {
        Text("Send Test HTTP Request")
    }
}

private fun hasNotificationAccess(context: Context): Boolean {
    return Settings.Secure.getString(
        context.applicationContext.contentResolver,
        "enabled_notification_listeners"
    ).contains(context.applicationContext.packageName)
}

fun sendLocalTest(context: android.content.Context) {
    // This function sends a local test notification to check if the listener is working.

    // Check if the app has permission to post notifications
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.e("NotificationForwarder", "Permission to post notifications not granted")
        return
    }

    val channelId = "local_test"
    val nm = ContextCompat.getSystemService(context, NotificationManager::class.java)
    Log.d("NotificationForwarder", "Attempting to send notification")

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        nm?.getNotificationChannel(channelId) == null
    ) {
        nm?.createNotificationChannel(
            NotificationChannel(channelId, "Local test", NotificationManager.IMPORTANCE_DEFAULT)
        )
        Log.d("NotificationForwarder", "Notification channel created")
    }

    val notif = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // Use a guaranteed icon
        .setContentTitle("Local test")
        .setContentText("Did the listener see me?")
        .build()

    nm?.notify(7, notif)
    Log.d("NotificationForwarder", "Notification sent")
}
