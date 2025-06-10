package com.example.findyourmatch.data.match

import kotlinx.serialization.Serializable
import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.findyourmatch.data.notifications.aggiungiNotificaRecensione
import com.example.findyourmatch.data.notifications.aggiungiNotificaRichiesta
import com.example.findyourmatch.data.rewards.addAchievement
import com.example.findyourmatch.data.rewards.caricaRaggiungimenti
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.data.user.StatsUtente
import com.example.findyourmatch.data.user.addReview
import com.example.findyourmatch.data.user.getMaxRewards
import com.example.findyourmatch.data.user.getStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
    val importo: Double,
    val squadra1: String,
    @SerialName("golsquadra1") val golSquadra1: Int?,
    val squadra2: String,
    @SerialName("golsquadra2") val golSquadra2: Int?,
    val visibile: Boolean,
    @SerialName("dataorascadenza") val dataOraScadenzaIscrizione: String
)

@Serializable
data class PartitaDaInserire(
    val tipo: String,
    val dataOraInizio: String,
    val dataOraScadenzaIscrizione: String,
    val importoPrevisto: Double,
    val maxGiocatori: Int,
    val visibile: Boolean,
    val luogo: Int,
    val creatore: String
)

@Serializable
data class Squadra(
    val nome: String,
    val partita: Int
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

@Serializable
data class Marcatore(
    val utente: String,
    val numeroGol: Int
)

@Serializable
data class AutoreAutogol(
    val utente: String,
    val numeroAutogol: Int
)

@Serializable
data class OrganizzatoreInSquadra(
    val utente: String,
    val squadra: String,
    val partita: Int
)

@Serializable
data class InserimentoStatsGiocatore(
    val email: String,
    val nomeCognome: String,
    var gol: MutableState<String> = mutableStateOf("0"),
    var autogol: MutableState<String> = mutableStateOf("0"),
    var rating: MutableState<Int> = mutableIntStateOf(0)
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

suspend fun getSportsFields(context: Context): List<CampoSportivo> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext listOf()

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/campi_sportivi?select=*")
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
            return@withContext listOf()
        }
        val json = response.body?.string() ?: return@withContext listOf()
        Log.d("JSON", json)
        val pitches = Json.decodeFromString(ListSerializer(CampoSportivo.serializer()), json)
        return@withContext pitches
    }
}

suspend fun addNewSportsField(
    context: Context,
    nation: String,
    province: String,
    city: String,
    street: String,
    houseNumber: String,
    name: String
): Int? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val newPlace = mapOf(
        "nazione" to nation,
        "provincia" to province,
        "citta" to city,
        "nome" to name,
        "via" to street,
        "civico" to houseNumber,
    )
    val placeBody = Json.encodeToString(newPlace).toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/campi_sportivi")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .post(placeBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        } else {
            val responseBody = response.body?.string()
            Log.d("Supabase", "Response: $responseBody")

            val jsonArray = Json.decodeFromString<List<Map<String, JsonElement>>>(responseBody!!)
            val id = jsonArray.firstOrNull()?.get("idCampo")?.jsonPrimitive?.intOrNull

            return@withContext id
        }
    }
}

suspend fun insertNewTeam(context: Context, name: String, idMatch: Int): Boolean? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val team = Squadra(
        name,
        idMatch
    )
    val teamBody = Json.encodeToString(team).toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/squadre")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(teamBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext false
        }
        true
    }
}

suspend fun insertOrganizerInATeam(context: Context, organizer: String, team1: String, team2: String, idMatch: Int) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val randomNumber = (1..2).random()
    val organizerInTeam = OrganizzatoreInSquadra(
        organizer,
        if (randomNumber == 1) team1 else team2,
        idMatch
    )
    val body = Json.encodeToString(organizerInTeam).toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/giocatori_squadra")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
        }
    }
}

suspend fun insertNewMatch(
    context: Context,
    type: String,
    existingPitch: CampoSportivo?,
    newPitchNation: String,
    newPitchProvince: String,
    newPitchCity: String,
    newPitchStreet: String,
    newPitchHouseNumber: String,
    newPitchName: String,
    gameDate: String,
    gameTime: String,
    expiringDate: String,
    expiringTime: String,
    expectedAmount: String,
    team1Name: String,
    team2Name: String,
    organizer: String
): Result<Unit> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext Result.failure(Exception("Token non valido"))

    var newPlaceID: Int? = null
    if (existingPitch == null)
        newPlaceID = addNewSportsField(
            context,
            newPitchNation,
            newPitchProvince,
            newPitchCity,
            newPitchStreet,
            newPitchHouseNumber,
            newPitchName
        )

    val date = java.time.LocalDate.parse(gameDate)
    val time = java.time.LocalTime.parse(gameTime)
    val gameDateTime = DateTimeFormatter.ISO_INSTANT.format(java.time.LocalDateTime.of(date,time).toInstant(
        ZoneOffset.UTC))
    val expDate = java.time.LocalDate.parse(expiringDate)
    val expTime = java.time.LocalTime.parse(expiringTime)
    val expDateTime = DateTimeFormatter.ISO_INSTANT.format(java.time.LocalDateTime.of(expDate,expTime).toInstant(
        ZoneOffset.UTC))
    val maxPlayers = when (type) {
        "5vs5" -> 10
        "7vs7" -> 14
        "8vs8" -> 16
        else -> 22
    }
    val newMatch = (existingPitch?.idCampo ?: newPlaceID)?.let {
        PartitaDaInserire(
            type,
            gameDateTime,
            expDateTime,
            expectedAmount.replace(",", ".").toDouble(),
            maxPlayers,
            true,
            it,
            organizer
        )
    }
    val matchBody = Json.encodeToString(newMatch).toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/partite")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .post(matchBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            return@withContext Result.failure(Exception("Inserimento fallito: (${response.code}): ${response.body?.string()}"))
        } else {
            val responseBody = response.body?.string()
            Log.d("Supabase", "Response: $responseBody")

            val jsonArray = Json.decodeFromString<List<Map<String, JsonElement>>>(responseBody!!)
            val id = jsonArray.firstOrNull()?.get("idPartita")?.jsonPrimitive?.intOrNull
            if (id?.let { insertNewTeam(context, team1Name, it) } == true && insertNewTeam(context, team2Name, id) == true) {
                insertOrganizerInATeam(context, organizer, team1Name, team2Name, id)
                return@withContext Result.success(Unit)
            }
            return@withContext Result.failure(Exception("Problemi nell'inserimento delle squadre"))
        }
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

suspend fun unsubscribePlayerFromMatch(context: Context, userEmail: String, team: String, idMatch: Int) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/giocatori_squadra?utente=eq.$userEmail&squadra=eq.$team&partita=eq.$idMatch")
        .delete()
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        } else {
            Log.d("SUCCESSO", "Cancellazione avvenuta con successo")
        }
    }
}

suspend fun isUserInRequestState(context: Context, userEmail: String, organizer: String, idMatch: Int) : Boolean? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val url = "https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche?select=gestita&richiedente=eq.$userEmail&destinatario=eq.$organizer&partita=eq.$idMatch&order=dataOraInvio"
    val request = Request.Builder()
        .url(url)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()
    
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        }
        val body = response.body?.string() ?: return@withContext null

        val b = JSONArray(body)
        if (b.length() > 0) {
            val managed = b.getJSONObject(b.length()-1).getBoolean("gestita")
            return@withContext !managed
        }
        return@withContext false
    }
}

suspend fun deleteMatch(context: Context, idMatch: Int) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/partite?idPartita=eq.$idMatch")
        .delete()
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext null
        } else {
            Log.d("SUCCESSO", "Cancellazione avvenuta con successo")
        }
    }
}

suspend fun getScorers(context: Context, idMatch: Int): List<Marcatore>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/marcatori?select=utente,numeroGol&partita=eq.$idMatch")
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
        val scorers = Json.decodeFromString(ListSerializer(Marcatore.serializer()), json)
        return@withContext scorers
    }
}

suspend fun getOwnGoalsScorers(context: Context, idMatch: Int): List<AutoreAutogol>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/autori_autogol?select=utente,numeroAutogol&partita=eq.$idMatch")
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
        val ownGoalsScorers = Json.decodeFromString(ListSerializer(AutoreAutogol.serializer()), json)
        return@withContext ownGoalsScorers
    }
}

suspend fun insertTeamGoals(context: Context, teamName: String, teamGoals: Int, idMatch: Int): Boolean = withContext(Dispatchers.IO){
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "golFatti": $teamGoals
        }
    """.trimIndent()
    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/squadre?nome=eq.$teamName&partita=eq.$idMatch")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .patch(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("Errore Supabase: ${response.code}", " - ${response.body?.string()}")
            return@withContext false
        }
        true
    }
}

suspend fun insertScorer(context: Context, player: String, idMatch: Int, numGoals: Int): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "utente": "$player",
            "partita": $idMatch,
            "numeroGol": $numGoals
        }
    """.trimIndent()
    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/marcatori")
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

suspend fun insertOwnGoalScorer(context: Context, player: String, idMatch: Int, numOwnGoals: Int): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "utente": "$player",
            "partita": $idMatch,
            "numeroAutogol": $numOwnGoals
        }
    """.trimIndent()
    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/autori_autogol")
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

suspend fun updateUserStats(context: Context, player: String, numGoals: Int, numOwnGoals: Int, win: Boolean) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "email": "$player",
            "numero_gol": $numGoals,
            "numero_autogol": $numOwnGoals,
            "vittoria": $win
        }
    """.trimIndent()
    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/rpc/update_stats")
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

suspend fun sendReviewNotification(
    context: Context,
    destinatario: String,
    idPartita: Int,
    destinatarioRecensione: String,
    autoreRecensione: String
) {
    aggiungiNotificaRecensione(
        context = context,
        titolo = "Recensione ricevuta",
        testo = "Hai appena ricevuto una nuova recensione",
        destinatario = destinatario,
        titoloEn = "Review received",
        testoEn = "You just received a new review",
        idPartita = idPartita,
        destinatarioRecensione = destinatarioRecensione,
        autoreRecensione = autoreRecensione
    )
}

fun getValueToCompare(count: Int): Int {
    Log.d("COUNT", count.toString())
    return when (count) {
        0 -> 10
        1 -> 15
        2 -> 20
        else -> -1
    }
}

fun getColor(value: Int): String {
    return when (value) {
        in 10..14 -> "bronzo"
        in 15..19 -> "argento"
        else -> "oro"
    }
}

suspend fun checkNewRewards(context: Context, user: String) = withContext(Dispatchers.IO) {
    val rewardsAchieved = caricaRaggiungimenti(context, user)
    if (user == "vincenzorossi665@gmail.com")
        Log.d("REWARDS", rewardsAchieved.toString())
    val userStats = getStats(context, user)
    if (user == "vincenzorossi665@gmail.com")
        Log.d("STATS", userStats.toString())

    if (rewardsAchieved.size < 9) {
        val valuePGToCompare = getValueToCompare(rewardsAchieved.count { it.tipologia == "partite_giocate" })
        if (user == "vincenzorossi665@gmail.com")
            Log.d("GIOCATE v", valuePGToCompare.toString())
        if (userStats!!.partiteGiocate == valuePGToCompare && valuePGToCompare != -1) {
            val color = getColor(valuePGToCompare)
            if (user == "vincenzorossi665@gmail.com")
                Log.d("COLORE GIOCATE", color)
            addAchievement(context, user, "partite_giocate", color)
        }

        val valueGToCompare = getValueToCompare(rewardsAchieved.count { it.tipologia == "goal_fatti" })
        if (user == "vincenzorossi665@gmail.com")
            Log.d("GOAL v", valueGToCompare.toString())
        if (userStats.golFatti >= valueGToCompare && valueGToCompare != -1) {
            val color = getColor(valueGToCompare)
            if (user == "vincenzorossi665@gmail.com")
                Log.d("COLORE GOL", color)
            addAchievement(context, user, "goal_fatti", color)
        }

        val valueWToCompare = getValueToCompare(rewardsAchieved.count { it.tipologia == "partite_vinte" })
        if (user == "vincenzorossi665@gmail.com")
            Log.d("VINTE v", valueWToCompare.toString())
        if (userStats.vittorie == valueWToCompare && valueWToCompare != -1) {
            val color = getColor(valueWToCompare)
            if (user == "vincenzorossi665@gmail.com")
                Log.d("COLORE VINTE", color)
            addAchievement(context, user, "partite_vinte", color)
        }
    }
}

suspend fun insertStatsMatch(
    context: Context,
    idMatch: Int,
    match: PartitaMostrata,
    team1Goals: Int,
    team2Goals: Int,
    players1Stats: List<InserimentoStatsGiocatore>,
    players2Stats: List<InserimentoStatsGiocatore>
): Boolean = withContext(Dispatchers.IO) {
    if (!insertTeamGoals(context, match.squadra1, team1Goals, idMatch) || !insertTeamGoals(context, match.squadra2, team2Goals, idMatch))
        return@withContext false

    players1Stats.forEach {
        if (it.gol.value.toInt() > 0)
            insertScorer(context, it.email, idMatch, it.gol.value.toInt())
        if (it.autogol.value.toInt() > 0)
            insertOwnGoalScorer(context, it.email, idMatch, it.autogol.value.toInt())
        updateUserStats(context, it.email, it.gol.value.toInt(), it.autogol.value.toInt(), team1Goals > team2Goals)
        if (addReview(context, it.email, match.creatore, idMatch, it.rating.value))
            sendReviewNotification(
                context,
                it.email,
                idMatch,
                it.email,
                match.creatore
            )
        checkNewRewards(context, it.email)
    }
    players2Stats.forEach {
        if (it.gol.value.toInt() > 0)
            insertScorer(context, it.email, idMatch, it.gol.value.toInt())
        if (it.autogol.value.toInt() > 0)
            insertOwnGoalScorer(context, it.email, idMatch, it.autogol.value.toInt())
        updateUserStats(context, it.email, it.gol.value.toInt(), it.autogol.value.toInt(), team2Goals > team1Goals)
        if (addReview(context, it.email, match.creatore, idMatch, it.rating.value))
            sendReviewNotification(
                context,
                it.email,
                idMatch,
                it.email,
                match.creatore
            )
        checkNewRewards(context, it.email)
    }

    return@withContext true
}