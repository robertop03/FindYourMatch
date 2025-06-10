package com.example.findyourmatch.data.notifications

import android.content.Context
import kotlinx.datetime.Instant
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.NotificationChannel
import android.app.NotificationManager
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class Notifica(
    val idNotifica: Int,
    val titolo: String,
    @SerialName("titolo_en") val titoloEn: String,
    val tipologia: String,
    val dataOraInvio: Instant,
    val destinatario: String,
    val stato: Boolean,
    val testo: String,
    @SerialName("testo_en") val testoEn: String,
    val richiedente: String? = null,
    val partita: Int? = null,
    @SerialName("colore_medaglia_raggiunta") val coloreMedagliaRaggiunta: String? = null,
    @SerialName("tipo_medaglia_raggiunta") val titoloMedagliaRaggiunta: String? = null,
    @SerialName("destinatario_recensione") val destinatarioRecensione: String? = null,
    @SerialName("autore_recensione") val autoreRecensione: String? = null,
    @SerialName("gestita") val gestita: Boolean? = false
    )

suspend fun caricaNotificheUtente(context: Context, email: String): List<Notifica> = withContext(Dispatchers.IO) {

    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche?destinatario=eq.${email}&order=dataOraInvio.desc")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        val json = response.body?.string() ?: return@withContext emptyList()
        return@withContext Json.decodeFromString(ListSerializer(Notifica.serializer()), json)
    }
}

suspend fun segnaNotificaComeLetta(context: Context, notifica: Notifica): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val requestBody = """
        {
            "stato": true
        }
    """.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche?idNotifica=eq.${notifica.idNotifica}")
        .method("PATCH", requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun prendiNomeCognomeDaEmail(context: Context, email: String): Pair<String, String>? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email}&select=nome,cognome")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val json = response.body?.string() ?: return@withContext null
        val result = Json.parseToJsonElement(json).jsonArray
        if (result.isNotEmpty()) {
            val obj = result[0].jsonObject
            val nome = obj["nome"]?.jsonPrimitive?.content ?: ""
            val cognome = obj["cognome"]?.jsonPrimitive?.content ?: ""
            return@withContext nome to cognome
        }
        return@withContext null
    }
}

suspend fun prendiPunteggioRecensione(
    context: Context,
    destinatario: String,
    autore: String,
    partita: Int
): Int? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/recensioni?" +
                "recensito=eq.${destinatario}&autore=eq.${autore}&partita=eq.${partita}&select=punteggio")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val json = response.body?.string() ?: return@withContext null
        val result = Json.parseToJsonElement(json).jsonArray
        if (result.isNotEmpty()) {
            val obj = result[0].jsonObject
            return@withContext obj["punteggio"]?.jsonPrimitive?.intOrNull
        }
        return@withContext null
    }
}

suspend fun prendiNomiSquadreDaPartita(context: Context, idPartita: Int): List<String> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext emptyList()

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/squadre?partita=eq.$idPartita&select=nome")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext emptyList()
        val json = response.body?.string() ?: return@withContext emptyList()
        val result = Json.parseToJsonElement(json).jsonArray
        return@withContext result.mapNotNull {
            it.jsonObject["nome"]?.jsonPrimitive?.content
        }
    }
}

suspend fun prendiNumeroMassimoPartecipanti(context: Context, idPartita: Int): Int? = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/partite?idPartita=eq.$idPartita&select=tipo")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext null
        val json = response.body?.string() ?: return@withContext null
        val result = Json.parseToJsonElement(json).jsonArray
        if (result.isNotEmpty()) {
            val tipo = result[0].jsonObject["tipo"]?.jsonPrimitive?.content ?: return@withContext null
            val numero = tipo.substringBefore("vs").toIntOrNull()
            return@withContext numero
        }
        return@withContext null
    }
}

suspend fun prendiNumeroPartecipantiInSquadra(
    context: Context,
    nomeSquadra: String,
    idPartita: Int
): Int = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext 0

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/giocatori_squadra?partita=eq.$idPartita&squadra=eq.$nomeSquadra&select=utente")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Accept", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return@withContext 0
        val json = response.body?.string() ?: return@withContext 0
        val result = Json.parseToJsonElement(json).jsonArray
        return@withContext result.size
    }
}

suspend fun aggiungiGiocatoreAllaSquadra(
    context: Context,
    email: String,
    squadra: String,
    idPartita: Int
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false
    val jsonBody = """
        {
            "utente": "$email",
            "squadra": "$squadra",
            "partita": $idPartita
        }
    """.trimIndent()

    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/giocatori_squadra")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun aggiungiNotificaRichiesta(
    context: Context,
    titolo: String,
    testo: String,
    destinatario: String,
    tipologia: String = "richiesta",
    titoloEn: String,
    testoEn: String,
    idPartita: Int,
    richiedente: String
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "titolo": "$titolo",
            "testo": "$testo",
            "destinatario": "$destinatario",
            "tipologia": "$tipologia",
            "titolo_en": "$titoloEn",
            "testo_en": "$testoEn",
            "partita":"$idPartita",
            "richiedente":"$richiedente"
        }
    """.trimIndent()
    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun aggiungiNotificaRifiuto(
    context: Context,
    titolo: String,
    testo: String,
    destinatario: String,
    tipologia: String = "rifiutato",
    titoloEn: String,
    testoEn: String,
    idPartita: Int
    ): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "titolo": "$titolo",
            "testo": "$testo",
            "destinatario": "$destinatario",
            "tipologia": "$tipologia",
            "titolo_en": "$titoloEn",
            "testo_en": "$testoEn",
            "partita":"$idPartita"
        }
    """.trimIndent()

    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun aggiungiNotificaEliminazioneDaAdmin(
    context: Context,
    titolo: String,
    testo: String,
    destinatario: String,
    tipologia: String = "annulla",
    titoloEn: String,
    testoEn: String
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "titolo": "$titolo",
            "testo": "$testo",
            "destinatario": "$destinatario",
            "tipologia": "$tipologia",
            "titolo_en": "$titoloEn",
            "testo_en": "$testoEn"
        }
    """.trimIndent()

    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun aggiungiNotificaAccettazione(
    context: Context,
    titolo: String,
    testo: String,
    destinatario: String,
    tipologia: String = "accettato",
    titoloEn: String,
    testoEn: String,
    idPartita: Int
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "titolo": "$titolo",
            "testo": "$testo",
            "destinatario": "$destinatario",
            "tipologia": "$tipologia",
            "titolo_en": "$titoloEn",
            "testo_en": "$testoEn",
            "partita": $idPartita
        }
    """.trimIndent()

    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun aggiungiNotificaRecensione(
    context: Context,
    titolo: String,
    testo: String,
    destinatario: String,
    tipologia: String = "recensione",
    titoloEn: String,
    testoEn: String,
    idPartita: Int,
    destinatarioRecensione: String,
    autoreRecensione: String
): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false

    val jsonBody = """
        {
            "titolo": "$titolo",
            "testo": "$testo",
            "destinatario": "$destinatario",
            "tipologia": "$tipologia",
            "titolo_en": "$titoloEn",
            "testo_en": "$testoEn",
            "partita":"$idPartita",
            "destinatario_recensione": "$destinatarioRecensione",
            "autore_recensione": "$autoreRecensione"
        }
    """.trimIndent()
    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche")
        .post(requestBody)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}

suspend fun segnaNotificaComeGestita(context: Context, idNotifica: Int): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext false
    val body = """{ "gestita": true }""".toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/notifiche?idNotifica=eq.$idNotifica")
        .method("PATCH", body)
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response -> response.isSuccessful }
}

fun creaCanaleNotifiche(context: Context) {
    val name = "Notifiche Partite"
    val descriptionText = "Notifiche relative a richieste, accettazioni, ecc."
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel("PARTITA_CHANNEL_ID", name, importance).apply {
        description = descriptionText
    }
    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

suspend fun inviaNotificaPush(titolo: String, testo: String, fcmToken: String): Boolean = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val bodyJson = """
    {
        "fcmToken": "$fcmToken",
        "notificaJson": {
            "titolo": "$titolo",
            "testo": "$testo"
        }
    }
""".trimIndent()


    val requestBody = bodyJson.toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("https://fcm-proxy.onrender.com/api/send-notification")
        .post(requestBody)
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).execute().use { response ->
        return@withContext response.isSuccessful
    }
}