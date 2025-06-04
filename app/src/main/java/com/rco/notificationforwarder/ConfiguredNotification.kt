package com.rco.notificationforwarder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Target {
    abstract val id: String
    abstract val name: String
}

@Serializable
@SerialName("http")
data class HTTPTarget(
    override val id: String,
    override val name: String,
    val url: String,
    val headers: Map<String, String>,
    val method: String = "POST",
) : Target()

@Serializable
@SerialName("bluetooth")
data class BluetoothTarget(
    override val id: String,
    override val name: String,
    val deviceAddress: String,
    val serviceUUID: String,
    val characteristicUUID: String
) : Target()

@Serializable
data class ConfiguredNotification(
    val id: String,
    val name: String,
    val sources: List<String>,
    val targets: List<Target>
)
