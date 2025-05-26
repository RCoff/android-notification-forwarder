package com.rco.notificationforwarder

import kotlinx.serialization.Serializable

@Serializable
data class Target(
    val id: String,
    val name: String,
    val type: String, // e.g., "HTTP", "Bluetooth"
    val info: Map<String, String> // relevant info for sending messages
)

@Serializable
data class ConfiguredNotification(
    val id: String,
    val name: String,
    val sources: List<String>, // e.g., package names
    val targets: List<Target>
)
