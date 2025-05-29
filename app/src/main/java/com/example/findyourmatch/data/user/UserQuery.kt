package com.example.findyourmatch.data.user

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

@Serializable
data class IndirizzoUtente(
    val stato: String,
    val provincia: String,
    val citta: String,
    val via: String,
    val civico: String
)

@Serializable
data class AnagraficaUtente(
    val nome: String,
    val cognome: String,
    @SerialName("data_nascita") val nascita: String,
    val sesso: String,
    @SerialName("data_iscrizione") val iscrizione: String,
    val email: String
)

suspend fun getLoggedUserEmail(context: Context): String? = withContext(Dispatchers.IO) {
    val accessToken = SessionManager.getAccessToken(context)
    if (accessToken.isNullOrBlank()) {
        return@withContext null
    }

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .get()
        .build()

    repeat(3) {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    delay(150L)
                    return@repeat
                }

                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    return@repeat
                }

                val json = Json { ignoreUnknownKeys = true }
                val jsonObject = json.parseToJsonElement(body).jsonObject
                val email = jsonObject["email"]?.jsonPrimitive?.content

                if (email != null) {
                    return@withContext email
                }
            }
        } catch (_: IOException) { } catch (_: Exception) { }

        delay(150L)
    }

    return@withContext null
}


suspend fun getIndirizzoUtente(context: Context): IndirizzoUtente? = withContext(Dispatchers.IO) {
    val email = getLoggedUserEmail(context) ?: return@withContext null
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email}&select=stato,provincia,citta,via,civico")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null

        val json = response.body?.string() ?: return@withContext null

        val indirizzi = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(IndirizzoUtente.serializer()),
            json
        )

        return@withContext indirizzi.firstOrNull()
    }
}

suspend fun getUserInfo(context: Context): AnagraficaUtente? = withContext(Dispatchers.IO) {
    val email = getLoggedUserEmail(context) ?: return@withContext null
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email}&select=nome,cognome,data_nascita,sesso,data_iscrizione,email")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val json = response.body?.string() ?: return@withContext null
        val users = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(AnagraficaUtente.serializer()),
            json
        )
        Log.d("SUPABASE", users.toString())
        return@withContext users.firstOrNull()
    }
}