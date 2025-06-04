package com.rco.notificationforwarder

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val Context.dataStore by preferencesDataStore(name = "configured_notifications")

private val targetModule = SerializersModule {
    polymorphic(Target::class) {
        subclass(HTTPTarget::class, HTTPTarget.serializer())
        subclass(BluetoothTarget::class, BluetoothTarget.serializer())
    }
}

private val json = Json {
    serializersModule = targetModule
    classDiscriminator = "type"
}

object ConfiguredNotificationRepository {
    private val KEY = stringPreferencesKey("notifications")

    suspend fun saveNotifications(context: Context, notifications: List<ConfiguredNotification>) {
        val jsonString = json.encodeToString(notifications)
        context.dataStore.edit { prefs ->
            prefs[KEY] = jsonString
        }
    }

    suspend fun loadNotifications(context: Context): List<ConfiguredNotification> {
        val prefs = context.dataStore.data.first()
        val jsonString = prefs[KEY] ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
