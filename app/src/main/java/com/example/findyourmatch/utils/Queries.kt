package com.example.findyourmatch.utils

import android.content.Context
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

suspend fun getLoggedUserEmail(context: Context): String? = withContext(Dispatchers.IO) {
    val accessToken = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .get()
        .build()

    val client = OkHttpClient()
    val response = client.newCall(request).execute()
    if (!response.isSuccessful) return@withContext null

    val body = response.body?.string() ?: return@withContext null
    val json = Json { ignoreUnknownKeys = true }
    val jsonObject = json.parseToJsonElement(body).jsonObject
    return@withContext jsonObject["email"]?.jsonPrimitive?.content
}
