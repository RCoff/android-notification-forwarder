package com.rco.notificationforwarder

import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                                when (target) {
                                    is HTTPTarget -> Text("- ${target.name} (HTTP, url: ${target.url}, method: ${target.method}, headers: ${target.headers})")
                                    is BluetoothTarget -> Text("- ${target.name} (Bluetooth, addr: ${target.deviceAddress})")
                                }
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
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf("") }
    var showAppPicker by remember { mutableStateOf(false) }
    var targetType by remember { mutableStateOf("HTTP") }

    // HTTP fields
    var httpTargetName by remember { mutableStateOf("") }
    var httpUrl by remember { mutableStateOf("") }
    var httpMethod by remember { mutableStateOf("POST") }
    var httpHeaders by remember { mutableStateOf("") } // Multiline, key:value per line

    // Bluetooth fields
    var btTargetName by remember { mutableStateOf("") }
    var btDeviceAddress by remember { mutableStateOf("") }
    var btServiceUUID by remember { mutableStateOf("") }
    var btCharacteristicUUID by remember { mutableStateOf("") }

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
                    value = selectedPackage,
                    onValueChange = {},
                    label = { Text("Source App Package") },
                    enabled = false,
                    modifier = Modifier
                        .clickable { showAppPicker = true }
                        .fillMaxWidth()
                )
                if (showAppPicker) {
                    AppPickerDialog(
                        onAppSelected = {
                            selectedPackage = it
                            showAppPicker = false
                        },
                        onDismiss = { showAppPicker = false }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("Target Type: ")
                    Spacer(Modifier.width(8.dp))
                    DropdownMenuBox(
                        selectedType = targetType,
                        onTypeSelected = { targetType = it }
                    )
                }
                Spacer(Modifier.height(8.dp))
                if (targetType == "HTTP") {
                    OutlinedTextField(
                        value = httpTargetName,
                        onValueChange = { httpTargetName = it },
                        label = { Text("HTTP Target Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = httpUrl,
                        onValueChange = { httpUrl = it },
                        label = { Text("HTTP URL") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = httpMethod,
                        onValueChange = { httpMethod = it },
                        label = { Text("HTTP Method (GET, POST, etc.)") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = httpHeaders,
                        onValueChange = { httpHeaders = it },
                        label = { Text("HTTP Headers (key:value per line)") },
                        singleLine = false,
                        maxLines = 4
                    )
                } else {
                    OutlinedTextField(
                        value = btTargetName,
                        onValueChange = { btTargetName = it },
                        label = { Text("Bluetooth Target Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = btDeviceAddress,
                        onValueChange = { btDeviceAddress = it },
                        label = { Text("Device Address") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = btServiceUUID,
                        onValueChange = { btServiceUUID = it },
                        label = { Text("Service UUID") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = btCharacteristicUUID,
                        onValueChange = { btCharacteristicUUID = it },
                        label = { Text("Characteristic UUID") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target: Target = if (targetType == "HTTP") {
                        val headersMap = httpHeaders.lines()
                            .mapNotNull { line ->
                                val idx = line.indexOf(':')
                                if (idx > 0) {
                                    val key = line.substring(0, idx).trim()
                                    val value = line.substring(idx + 1).trim()
                                    if (key.isNotEmpty()) key to value else null
                                } else null
                            }
                            .toMap()
                        HTTPTarget(
                            id = UUID.randomUUID().toString(),
                            name = httpTargetName,
                            url = httpUrl,
                            headers = headersMap,
                            method = httpMethod
                        )
                    } else {
                        BluetoothTarget(
                            id = UUID.randomUUID().toString(),
                            name = btTargetName,
                            deviceAddress = btDeviceAddress,
                            serviceUUID = btServiceUUID,
                            characteristicUUID = btCharacteristicUUID
                        )
                    }
                    val notification = ConfiguredNotification(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        sources = if (selectedPackage.isNotBlank()) listOf(selectedPackage) else emptyList(),
                        targets = listOf(target)
                    )
                    onSave(notification)
                },
                enabled = name.isNotBlank() && selectedPackage.isNotBlank() &&
                        (
                            (targetType == "HTTP" && httpTargetName.isNotBlank() && httpUrl.isNotBlank() && httpMethod.isNotBlank()) ||
                            (targetType == "Bluetooth" && btTargetName.isNotBlank() && btDeviceAddress.isNotBlank() && btServiceUUID.isNotBlank() && btCharacteristicUUID.isNotBlank())
                        )
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

@Composable
fun AppPickerDialog(onAppSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = remember {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .sortedBy { it.loadLabel(pm).toString() }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                items(apps) { app ->
                    Text(
                        text = "${app.loadLabel(pm)} (${app.packageName})",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAppSelected(app.packageName) }
                            .padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DropdownMenuBox(selectedType: String, onTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedType)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("HTTP") },
                onClick = {
                    onTypeSelected("HTTP")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Bluetooth") },
                onClick = {
                    onTypeSelected("Bluetooth")
                    expanded = false
                }
            )
        }
    }
}
