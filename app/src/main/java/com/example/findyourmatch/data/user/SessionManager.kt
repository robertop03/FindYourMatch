package com.example.findyourmatch.data.user

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.findyourmatch.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object SessionManager {
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        context.applicationContext.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

     suspend fun getAccessToken(context: Context): String? {
         val token = context.applicationContext.dataStore.data.map {
             val at = it[ACCESS_TOKEN]
             at
         }.first()
         return token
    }

    suspend fun getRefreshToken(context: Context): String? {
        val refreshToken = context.applicationContext.dataStore.data.map {
            val rt = it[REFRESH_TOKEN]
            rt
        }.first()
        return refreshToken
    }

    fun logout(sessionViewModel: SessionViewModel) {
        sessionViewModel.updateLoginStatus(false)
    }

    fun isLoggedIn(sessionViewModel: SessionViewModel): Boolean {
        return sessionViewModel.isLoggedIn.value
    }
}
