package com.example.findyourmatch.data.notifications

import android.content.Context
import android.util.Log
import com.example.findyourmatch.data.user.SessionManager.getAccessToken
import com.example.findyourmatch.data.user.getLoggedUserEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun aggiornaTokenFCMUtenteSeDiverso(context: Context, nuovoToken: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val email = getLoggedUserEmail(context) ?: return@withContext false
        val accessToken = getAccessToken(context) ?: return@withContext false

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.$email&select=fcm_token")
            .get()
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e("FCM", "Errore nel recupero del token attuale da Supabase")
            return@withContext false
        }

        val responseBody = response.body?.string() ?: return@withContext false
        val json = Json.parseToJsonElement(responseBody).jsonArray
        val tokenAttuale = if (json.isNotEmpty())
            json[0].jsonObject["fcm_token"]?.jsonPrimitive?.content
        else null

        if (tokenAttuale == nuovoToken) {
            return@withContext true
        }

        val body = """
            {
                "fcm_token": "$nuovoToken"
            }
        """.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())
        val patchRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.$email")
            .method("PATCH", body)
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .build()

        val patchResponse = client.newCall(patchRequest).execute()
        val ok = patchResponse.isSuccessful
        return@withContext ok

    } catch (e: Exception) {
        Log.e("FCM", "Errore durante l’aggiornamento del token FCM", e)
        return@withContext false
    }
}

suspend fun prendiTokenFCMDaEmail(context: Context, email: String): String? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = getAccessToken(context) ?: return@withContext null
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.$email&select=fcm_token")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val json = response.body?.string() ?: return@withContext null
        val result = Json.parseToJsonElement(json).jsonArray
        return@withContext result.getOrNull(0)?.jsonObject?.get("fcm_token")?.jsonPrimitive?.content
    }
}

