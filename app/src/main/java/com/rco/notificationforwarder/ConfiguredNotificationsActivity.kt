package com.rco.notificationforwarder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rco.notificationforwarder.ui.theme.NotificationForwarderTheme
import kotlinx.coroutines.launch
import java.util.UUID

class ConfiguredNotificationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationForwarderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfiguredNotificationsScreen()
                }
            }
        }
    }
}

@Composable
fun ConfiguredNotificationsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var notifications by remember { mutableStateOf(listOf<ConfiguredNotification>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            notifications = ConfiguredNotificationRepository.loadNotifications(context)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Configured Notifications", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = { showAddDialog = true }) {
            Text("Add New Notification")
        }
        Spacer(Modifier.height(16.dp))
        if (notifications.isEmpty()) {
            Text("No configured notifications.")
        } else {
            LazyColumn {
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Name: ${notif.name}", style = MaterialTheme.typography.titleMedium)
                            Text("Sources: ${notif.sources.joinToString()}")
                            Text("Targets:")
                            notif.targets.forEach { target ->
                                Text("- ${target.name} (${target.type})")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddConfiguredNotificationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newNotification ->
                showAddDialog = false
                scope.launch {
                    val updated = notifications + newNotification
                    ConfiguredNotificationRepository.saveNotifications(context, updated)
                    notifications = updated
                }
            }
        )
    }
}

@Composable
fun AddConfiguredNotificationDialog(
    onDismiss: () -> Unit,
    onSave: (ConfiguredNotification) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var targetName by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Notification") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Notification Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (e.g. package)") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetName,
                    onValueChange = { targetName = it },
                    label = { Text("Target Name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetType,
                    onValueChange = { targetType = it },
                    label = { Text("Target Type (e.g. HTTP, Bluetooth)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val notification = ConfiguredNotification(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        sources = if (source.isNotBlank()) listOf(source) else emptyList(),
                        targets = if (targetName.isNotBlank() && targetType.isNotBlank())
                            listOf(
                                Target(
                                    id = UUID.randomUUID().toString(),
                                    name = targetName,
                                    type = targetType,
                                    info = emptyMap()
                                )
                            )
                        else emptyList()
                    )
                    onSave(notification)
                },
                enabled = name.isNotBlank() && source.isNotBlank() && targetName.isNotBlank() && targetType.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
