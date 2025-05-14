package com.example.findyourmatch.data.user

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import java.util.Locale
import android.os.LocaleList
import androidx.compose.runtime.staticCompositionLocalOf


// Extension property per ottenere il DataStore
val Context.dataStore by preferencesDataStore("user_preferences")

class UserSettings(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val FINGERPRINT_KEY = booleanPreferencesKey("fingerprint_enabled")
        val MAX_DISTANCE_KEY = floatPreferencesKey("max_distance")
    }

    val language: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "it" }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_KEY] ?: true }

    val fingerprintEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[FINGERPRINT_KEY] ?: true }

    val maxDistance: Flow<Float> = context.dataStore.data
        .map { it[MAX_DISTANCE_KEY] ?: 50f }

    suspend fun saveSettings(
        language: String,
        notifications: Boolean,
        fingerprint: Boolean,
        distance: Float
    ) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
            prefs[NOTIFICATIONS_KEY] = notifications
            prefs[FINGERPRINT_KEY] = fingerprint
            prefs[MAX_DISTANCE_KEY] = distance
        }
    }
}


object LocaleHelper {
    fun updateLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }
}

val LocalLocalizedContext = staticCompositionLocalOf<Context> {
    error("No localized context provided")
}