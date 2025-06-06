package com.example.findyourmatch.data.match

import kotlinx.serialization.Serializable
import android.content.Context
import android.util.Log
import com.example.findyourmatch.data.user.Recensione
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.data.user.StatsUtente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Serial

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
    @Transient
    var distanzaKm: Double? = null
)

@Serializable
data class PartitaMostrata(
    val tipo: String,
    val creatore: String,
    val nazione: String,
    val citta: String,
    @SerialName("nomecampo") val nomeCampo: String,
    @SerialName("dataora") val dataOra: String,
    @SerialName("nomecreatore") val nomeCreatore: String,
    @SerialName("cognomecreatore") val cognomeCreatore: String,
    val telefono: String,
    val squadra1: String,
    val squadra2: String,
    val visibile: Boolean,
    @SerialName("dataorascadenza") val dataOraScadenzaIscrizione: String
)

@Serializable
data class Giocatore(
    val email: String,
    val nome: String,
    val cognome: String
)

@Serializable
data class GiocatoreWrapper(
    val utente: Giocatore
)

suspend fun getPartiteConCampo(context: Context): List<PartitaConCampo> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()
    val isValid = SessionManager.isTokenStillValid(context)

    val requestBuilder = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/partite?select=*,campo:luogo(*)")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Accept", "application/json")

    if (token.isNotEmpty() && isValid) {
        requestBuilder.addHeader("Authorization", "Bearer $token")
    }

    return@withContext client.newCall(requestBuilder.build()).execute().use { response ->
        if (!response.isSuccessful) return@use emptyList()

        val json = response.body?.string() ?: return@use emptyList()
        Json.decodeFromString(ListSerializer(PartitaConCampo.serializer()), json)
    }
}

suspend fun getMatch(context: Context, id: Int) : PartitaMostrata? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_partita")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .post("""{"id_partita":$id}""".toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        val match = Json.decodeFromString(ListSerializer(PartitaMostrata.serializer()), json)
        return@withContext match.firstOrNull()
    }
}

suspend fun getTeamPlayers(context: Context, team: String, idMatch: Int) : List<GiocatoreWrapper>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/giocatori_squadra?select=utente(email,nome,cognome)&squadra=eq.$team&partita=eq.$idMatch")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        Log.d("JSON", json)
        val players = Json.decodeFromString(ListSerializer(GiocatoreWrapper.serializer()), json)
        return@withContext players
    }
}