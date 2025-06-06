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

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}


val Context.dataStore by preferencesDataStore("user_preferences")
val BIOMETRIC_READY_KEY = booleanPreferencesKey("biometric_ready")

class UserSettings(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        val FINGERPRINT_KEY = booleanPreferencesKey("fingerprint_enabled")
        val MAX_DISTANCE_KEY = floatPreferencesKey("max_distance")
        val THEME_PREF_KEY = stringPreferencesKey("theme_preference")
    }

    val language: Flow<String> = context.dataStore.data
        .map { it[LANGUAGE_KEY] ?: "it" }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS_KEY] ?: true }

    val fingerprintEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[FINGERPRINT_KEY] ?: false }

    val maxDistance: Flow<Float> = context.dataStore.data
        .map { it[MAX_DISTANCE_KEY] ?: 50f }

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { prefs ->
            when (prefs[THEME_PREF_KEY]) {
                "LIGHT" -> ThemePreference.LIGHT
                "DARK" -> ThemePreference.DARK
                else -> ThemePreference.SYSTEM
            }
        }


    suspend fun saveSettings(
        language: String,
        notifications: Boolean,
        fingerprint: Boolean,
        distance: Float,
        theme: ThemePreference
    ) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language
            prefs[NOTIFICATIONS_KEY] = notifications
            prefs[FINGERPRINT_KEY] = fingerprint
            prefs[MAX_DISTANCE_KEY] = distance
            prefs[THEME_PREF_KEY] = theme.name
        }
    }

    suspend fun setBiometricReady(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BIOMETRIC_READY_KEY] = enabled
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