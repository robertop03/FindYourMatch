package com.example.findyourmatch.data.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


suspend fun inviaEmailRecuperoPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val json = Json.encodeToString(
            mapOf(
                "email" to email,
                "redirect_to" to "findyourmatch://password-reset"
            )
        )
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/recover")
            .addHeader(
                "apikey",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
            )
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val client = OkHttpClient()
        client.newCall(request).execute().use { response ->
            return@withContext if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val error = response.body?.string()
                Result.failure(Exception("Errore (${response.code}): $error"))
            }
        }
    } catch (e: Exception) {
        return@withContext Result.failure(e)
    }
}
