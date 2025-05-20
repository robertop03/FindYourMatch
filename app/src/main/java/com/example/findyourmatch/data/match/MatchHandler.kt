package com.example.findyourmatch.data.match

import kotlinx.serialization.Serializable
import android.content.Context
import android.util.Log
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
data class CampoSportivo(
    val idCampo: Int,
    val nazione: String,
    val provincia: String,
    val citta: String,
    val via: String,
    val civico: Int,
    val nome: String
)

@Serializable
data class PartitaConCampo(
    val idPartita: Int,
    val tipo: String,
    val dataOraInizio: String,
    val dataOraScadenzaIscrizione: String,
    val importoPrevisto: Double,
    val maxGiocatori: Int,
    val visibile: Boolean,
    val creatore: String,
    val luogo: Int,
    val campo: CampoSportivo,
    val partecipantiAttuali: Int
)

suspend fun getPartiteConCampo(context: Context): List<PartitaConCampo> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()
    val isValid = SessionManager.isTokenStillValid(context)
    Log.d("TOKEN", token)
    val requestBuilder  = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/partite?select=*,campo:luogo(*)")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Accept", "application/json")

    if (token.isNotEmpty() && isValid) {
        requestBuilder .addHeader("Authorization", "Bearer $token")
    }

    val response = client.newCall(requestBuilder.build()).execute()
    if (!response.isSuccessful) return@withContext emptyList()

    val json = response.body?.string() ?: return@withContext emptyList()

    return@withContext Json.decodeFromString(ListSerializer(PartitaConCampo.serializer()), json)
}
