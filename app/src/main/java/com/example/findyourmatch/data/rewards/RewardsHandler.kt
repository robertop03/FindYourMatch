package com.example.findyourmatch.data.rewards

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class Badge(
    val value: Int,
    val reached: Boolean,
    val icon: ImageVector,
    val description: String
)

@Serializable
data class RewardAchievement(
    val tipologia: String,
    val colore: String,
    val utente: String
)

suspend fun caricaRaggiungimenti(context: Context, email: String): List<RewardAchievement> = withContext(
    Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/raggiungimenti_medaglie?utente=eq.${email}")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        val json = response.body?.string() ?: return@withContext emptyList()
        return@withContext Json.decodeFromString(ListSerializer(RewardAchievement.serializer()), json)
    }
}

suspend fun addAchievement(context: Context, user: String, type: String, color: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "tipologia": "$type",
            "colore": "$color",
            "utente": "$user"
        }
    """.trimIndent()
    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/raggiungimenti_medaglie")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext false
        }
        true
    }
}