package com.example.findyourmatch.data.user

import android.content.Context
import com.example.findyourmatch.utils.NetworkJson
import com.example.findyourmatch.viewmodel.SessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class SessionData(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int
)

suspend fun loginSupabase(
    context: Context,
    email: String,
    password: String,
    sessionViewModel: SessionViewModel
): Result<String> = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val jsonBody = Json.encodeToString(LoginRequest(email, password))
        val userSettings = UserSettings(context)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/token?grant_type=password")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            return@withContext Result.failure(Exception("Login fallito, credenziali errate"))
        }else{
            userSettings.setBiometricReady(true)
        }

        val bodyString = response.body?.string()
            ?: return@withContext Result.failure(Exception("Corpo della risposta vuoto"))

        val session = NetworkJson.json.decodeFromString(SessionData.serializer(), bodyString)

        SessionManager.saveTokens(context.applicationContext, session.accessToken, session.refreshToken)
        sessionViewModel.updateLoginStatus(true)
        Result.success(session.accessToken)

    } catch (e: Exception) {
        Result.failure(e)
    }
}