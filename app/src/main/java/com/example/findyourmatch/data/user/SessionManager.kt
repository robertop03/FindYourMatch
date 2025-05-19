package com.example.findyourmatch.data.user

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.findyourmatch.viewmodel.SessionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import kotlinx.coroutines.withContext
import okhttp3.Request

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

    suspend fun isTokenStillValid(context: Context): Boolean = withContext(Dispatchers.IO) {
        val token = getAccessToken(context) ?: return@withContext false

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
            .addHeader("Authorization", "Bearer $token")
            .addHeader(
                "apikey",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
            )
            .get()
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    fun logout(sessionViewModel: SessionViewModel) {
        sessionViewModel.updateLoginStatus(false)
    }

    fun isLoggedIn(sessionViewModel: SessionViewModel): Boolean {
        return sessionViewModel.isLoggedIn.value
    }
}
