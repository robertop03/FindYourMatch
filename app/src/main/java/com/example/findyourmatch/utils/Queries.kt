package com.example.findyourmatch.utils

import android.content.Context
import android.util.Log
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

suspend fun getLoggedUserEmail(context: Context): String? = withContext(Dispatchers.IO) {
    val accessToken = SessionManager.getAccessToken(context)
    if (accessToken.isNullOrBlank()) {
        Log.w("getLoggedUserEmail", "Access token is null or blank")
        return@withContext null
    }

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .get()
        .build()

    repeat(3) { attempt ->
        try {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("getLoggedUserEmail", "Attempt $attempt - HTTP ${response.code}: ${response.message}")
                delay(150L) // retry dopo un piccolo delay
                return@repeat
            }

            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                Log.e("getLoggedUserEmail", "Empty body in response")
                return@repeat
            }

            val json = Json { ignoreUnknownKeys = true }
            val jsonObject = json.parseToJsonElement(body).jsonObject
            val email = jsonObject["email"]?.jsonPrimitive?.content

            if (email != null) {
                return@withContext email
            } else {
                Log.e("getLoggedUserEmail", "Email not found in response JSON")
            }

        } catch (e: IOException) {
            Log.e("getLoggedUserEmail", "IOException: ${e.localizedMessage}")
        } catch (e: Exception) {
            Log.e("getLoggedUserEmail", "Exception: ${e.localizedMessage}")
        }

        delay(150L)
    }

    return@withContext null
}
