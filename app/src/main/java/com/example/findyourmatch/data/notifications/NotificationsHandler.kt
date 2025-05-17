package com.example.findyourmatch.data.notifications

import android.content.Context
import kotlinx.datetime.Instant
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class Notifica(
    val titolo: String,
    val titolo_en: String,
    val tipologia: String,
    val dataOraInvio: Instant,
    val destinatario: String,
    val stato: Boolean,
    val testo: String,
    val testo_en: String,
    val richiedente: String? = null,
    val partita: String? = null,
    val colore_medaglia_raggiunta: String? = null,
    val tipo_medaglia_raggiunta: String? = null,
    val destinatario_recensione: String? = null,
    val autore_recensione: String? = null,
    val partita_riferimento_recensione: String? = null
)

suspend fun caricaNotificheUtente(context: Context, email: String): List<Notifica> = withContext(Dispatchers.IO) {

    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche?destinatario=eq.${email}")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) return@withContext emptyList()

    val json = response.body?.string() ?: return@withContext emptyList()

    return@withContext Json.decodeFromString(ListSerializer(Notifica.serializer()), json)
}
