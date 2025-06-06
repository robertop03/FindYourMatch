package com.example.findyourmatch.data.user

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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

@Serializable
data class MaxObiettivoRaggiunto(
    val tipologia: String,
    val colore: String,
    val obiettivo: Int
)

@Serializable
data class PartiteGiocateUtente(
    @SerialName("id_partita") val id: Int,
    val tipo: String,
    val citta: String,
    @SerialName("nome_campo") val nomeCampo: String,
    @SerialName("data_ora") val dataOra: String,
    val creatore: String,
    val squadra1: String,
    val gol1: Int?,
    val squadra2: String,
    val gol2: Int?,
    @SerialName("nome_squadra_utente") val squadraUtente: String,
    val esito: String
)

@Serializable
data class StatsUtentePartita(
    @SerialName("numerogol") val numeroGol: Int,
    @SerialName("numeroautogol") val numeroAutogol: Int
)

@Serializable
data class Recensione(
    @SerialName("nome") val nomeAutore: String,
    @SerialName("cognome") val cognomeAutore: String,
    val punteggio: Int,
    @SerialName("dataorainizio") val dataOraPartita: String,
    val partita: Int
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
                Log.d("EMAIL LOGGATO", email.toString())
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
        return@withContext users.firstOrNull()
    }
}

suspend fun updateProfileImage(context: Context, email: String, imagePath: Uri) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null
    val fileName = "$email.jpg"
    val inputStream = context.contentResolver.openInputStream(imagePath)
    val fileBytes = inputStream?.readBytes()
    inputStream?.close()
    val requestBody = fileBytes?.toRequestBody("image/jpeg".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/storage/v1/object/profilephotos/$fileName")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "image/jpeg")
        .addHeader("x-upsert", "true") // sovrascrive se esiste
        .post(requestBody!!)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore upload Supabase: ${response.code}", " - ${response.body?.string()}")
        }
    }
}

suspend fun checkIfImageExists(imageUrl: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(imageUrl)
        .head()
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun calculateNumOfRewardsAchieved(context: Context, userEMail: String): Int? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/raggiungimenti_medaglie?utente=eq.$userEMail")
        .addHeader("Authorization", "Bearer $token")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Prefer", "count=exact")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val count = response.header("Content-Range")?.substringAfter("/")?.toIntOrNull()
        return@withContext count
    }
}

suspend fun getMaxRewards(context: Context, userEMail: String): List<MaxObiettivoRaggiunto>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_max_medaglie_raggiunte")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(
            """{"utente_email":"$userEMail"}"""
                .toRequestBody("application/json".toMediaTypeOrNull())
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        val rewards = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(MaxObiettivoRaggiunto.serializer()),
            json
        )
        return@withContext rewards
    }
}

suspend fun getPlayedGames(context: Context, userEMail: String) : List<PartiteGiocateUtente>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_partite_utente_dettaglio")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(
            """{"email_input":"$userEMail"}"""
                .toRequestBody("application/json".toMediaTypeOrNull())
        )
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        val playedGames = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(PartiteGiocateUtente.serializer()),
            json
        )
        return@withContext playedGames
    }
}

suspend fun updateProfile(context: Context, dataToUpdate: Map<String, String>, userEmail: String) = withContext(Dispatchers.IO){
    if (dataToUpdate.isNotEmpty()) {
        val client = OkHttpClient()
        val token = SessionManager.getAccessToken(context) ?: return@withContext null

        val json = JSONObject(dataToUpdate).toString()
        val requestBody = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.$userEmail")
            .addHeader(
                "apikey",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
            )
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .patch(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful)
                Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
        }
    } else {
        Log.d("LISTA VUOTA", "Nessun valore da cambiare")
    }
}

suspend fun getStats(context: Context, userEmail: String): StatsUtente? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/statistiche_utente?utente=eq.${userEmail}&select=utente,partiteGiocate,golFatti,autogol,vittorie")
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
        val stats = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(StatsUtente.serializer()),
            json
        )
        return@withContext stats.firstOrNull()
    }
}

suspend fun getUserStatsInMatch(context: Context, userEmail: String, game: Long) : StatsUtentePartita? = withContext(Dispatchers.IO){
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val jsonBody = """
        {
            "utente_text": "$userEmail",
            "partita_int": $game
        }
    """
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_gol_autogol")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(jsonBody.trimIndent().toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        val stats = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(StatsUtentePartita.serializer()),
            json
        )

        return@withContext stats.firstOrNull()
    }
}

suspend fun getAverageRating(context: Context, userEmail: String) : Double? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_average_rating")
        .addHeader("Authorization", "Bearer $token")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Content-Type", "application/json")
        .post("""{"email_utente":"$userEmail"}""".toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val average = response.body?.string()
        return@withContext average?.toDoubleOrNull()
    }
}

suspend fun getReviews(context: Context, userEmail: String) : List<Recensione>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/get_recensioni")
        .addHeader(
            "apikey",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0"
        )
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .post("""{"email_recensito":"$userEmail"}""".toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val json = response.body?.string() ?: return@withContext null
        val reviews = Json.decodeFromString(
            kotlinx.serialization.builtins.ListSerializer(Recensione.serializer()),
            json
        )
        return@withContext reviews.sortedByDescending {
            LocalDateTime.parse(it.dataOraPartita)
        }
    }
}