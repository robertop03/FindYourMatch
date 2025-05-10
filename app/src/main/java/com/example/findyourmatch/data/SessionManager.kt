package com.example.findyourmatch.data

import android.content.Context
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

object SessionManager {
    private val Context.dataStore by preferencesDataStore(name = "user_session")

    private val LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")

    suspend fun login(context: Context, email: String) {
        context.dataStore.edit { prefs -> prefs[LOGGED_IN_EMAIL] = email }
    }

    suspend fun logout(context: Context) {
        context.dataStore.edit { prefs -> prefs.remove(LOGGED_IN_EMAIL) }
    }

    suspend fun getLoggedInUser(context: Context): String? {
        return context.dataStore.data.first()[LOGGED_IN_EMAIL]
    }
}
