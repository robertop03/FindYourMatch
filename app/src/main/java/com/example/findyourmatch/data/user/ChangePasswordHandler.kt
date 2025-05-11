package com.example.findyourmatch.data.user

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun cambiaPasswordUtente(context: Context, nuovaPassword: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        var accessToken = SessionManager.getAccessToken(context)
            ?: return@withContext Result.failure(Exception("Token mancante. Effettua il login."))

        var result = tryUpdatePassword(accessToken, nuovaPassword)

        if (result.isFailure && result.exceptionOrNull()?.message?.contains("401") == true) {
            // Token scaduto → provo refresh
            val refreshResult = refreshAccessToken(context)
            if (refreshResult.isSuccess) {
                val nuovoToken = refreshResult.getOrThrow()
                result = tryUpdatePassword(nuovoToken, nuovaPassword)
            } else {
                return@withContext Result.failure(Exception("Sessione scaduta. Effettua nuovamente il login."))
            }
        }

        result
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun tryUpdatePassword(token: String, nuovaPassword: String): Result<Unit> {
    return try {
        val json = Json.encodeToString(mapOf("password" to nuovaPassword))
        val body = json.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .put(body)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val errorBody = response.body?.string()
            Result.failure(Exception("${response.code}: $errorBody"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}


suspend fun refreshAccessToken(context: Context): Result<String> = withContext(Dispatchers.IO) {
    try {
        val refreshToken = SessionManager.getRefreshToken(context)
        if (refreshToken == null) {
            Log.e("RefreshToken", "Refresh token mancante.")
            return@withContext Result.failure(Exception("Refresh token mancante."))
        }

        Log.d("RefreshToken", "Token attuale: $refreshToken")

        val json = Json.encodeToString(mapOf("refresh_token" to refreshToken))
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/token?grant_type=refresh_token")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        val responseBody = response.body?.string()
        Log.d("RefreshResponse", "Codice: ${response.code} - Corpo: $responseBody")

        if (!response.isSuccessful || responseBody == null) {
            return@withContext Result.failure(
                Exception("Errore nel refresh token (${response.code}): $responseBody")
            )
        }

        val session = Json { ignoreUnknownKeys = true }
            .decodeFromString(SessionData.serializer(), responseBody)

        SessionManager.saveTokens(context, session.accessToken, session.refreshToken)
        Result.success(session.accessToken)
    } catch (e: Exception) {
        Log.e("RefreshException", "Eccezione: ${e.message}", e)
        Result.failure(e)
    }
}
