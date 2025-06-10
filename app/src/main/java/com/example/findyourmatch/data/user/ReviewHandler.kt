package com.example.findyourmatch.data.user

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun addReview(context: Context, recevier: String, author: String, idMatch: Int, rating: Int): Boolean = withContext(Dispatchers.IO){
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    if (author != recevier) {
        val jsonBody = """
            {
                "autore": "$author",
                "recensito": "$recevier",
                "partita": $idMatch,
                "punteggio": $rating
            }
        """.trimIndent()
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/recensioni")
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
    false
}