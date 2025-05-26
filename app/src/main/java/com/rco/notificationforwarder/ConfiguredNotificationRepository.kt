package com.rco.notificationforwarder

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "configured_notifications")

object ConfiguredNotificationRepository {
    private val KEY = stringPreferencesKey("notifications")

    suspend fun saveNotifications(context: Context, notifications: List<ConfiguredNotification>) {
        val json = Json.encodeToString(notifications)
        context.dataStore.edit { prefs ->
            prefs[KEY] = json
        }
    }

    suspend fun loadNotifications(context: Context): List<ConfiguredNotification> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY] ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
