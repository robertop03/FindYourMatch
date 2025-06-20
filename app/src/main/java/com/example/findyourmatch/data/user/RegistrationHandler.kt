package com.example.findyourmatch.data.user

import android.content.Context
import android.util.Log
import com.example.findyourmatch.utils.NetworkJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate


@Serializable
data class SignupRequest(val email: String, val password: String)

@Serializable
data class SignupResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class Utente(
    val email: String,
    val nome: String,
    val cognome: String,
    @SerialName("data_nascita") val dataNascita: String,
    val sesso: String,
    @SerialName("data_iscrizione") val dataIscrizione: String,
    val telefono: String,
    val stato: String,
    val citta: String,
    val provincia: String,
    val via: String,
    val civico: String
)

@Serializable
data class StatsUtente(
    val utente: String,
    val partiteGiocate: Int,
    val golFatti: Int,
    val autogol: Int,
    val vittorie: Int,
)

suspend fun registraUtenteSupabase(
    context: Context,
    email: String,
    password: String,
    nome: String,
    cognome: String,
    dataNascita: String,
    sesso: String,
    telefono: String,
    stato: String,
    citta: String,
    provincia: String,
    via: String,
    civico: String
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()

        // 1. Registrazione su Supabase Auth
        val signupBody = Json.encodeToString(SignupRequest(email, password))
        val requestBody = signupBody.toRequestBody("application/json".toMediaType())

        val signupRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/signup")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(signupRequest).execute().use { signupResponse ->
            if (!signupResponse.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Registrazione fallita, un account con questa email è già registrato")
                )
            }
        }

        val loginBody = """
    {
        "email": "$email",
        "password": "$password"
    }
""".trimIndent().toRequestBody("application/json".toMediaType())

        val loginRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/token?grant_type=password")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .post(loginBody)
            .build()

        val session = client.newCall(loginRequest).execute().use { loginResponse ->
            if (!loginResponse.isSuccessful) {
                return@withContext Result.failure(Exception("Login fallito dopo la registrazione (${loginResponse.code})"))
            }
            val body = loginResponse.body?.string()
                ?: return@withContext Result.failure(Exception("Nessuna risposta dal login"))
            NetworkJson.json.decodeFromString(SignupResponse.serializer(), body)
        }

        // 2. Salva i token
        SessionManager.saveTokens(context, session.accessToken, session.refreshToken)

        // 3. Controlla se l’utente è già nella tabella `utenti`
        val checkRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email.trim()}")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.accessToken}")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(checkRequest).execute().use { checkResponse ->
            if (!checkResponse.isSuccessful) {
                return@withContext Result.failure(Exception("Errore nel controllo duplicato email: ${checkResponse.code}"))
            }
            val checkBody = checkResponse.body?.string() ?: "[]"
            if (checkBody.contains(email.trim(), ignoreCase = true)) {
                return@withContext Result.failure(Exception("Un account con questa email è già registrato."))
            }
        }

        // 4. Inserimento nella tabella `utenti`
        val utente = Utente(
            email = email,
            nome = nome,
            cognome = cognome,
            dataNascita = dataNascita,
            sesso = sesso,
            dataIscrizione = LocalDate.now().toString(),
            telefono = telefono,
            stato = stato,
            citta = citta,
            provincia = provincia,
            via = via,
            civico = civico
        )

        val utenteBody = Json.encodeToString(utente).toRequestBody("application/json".toMediaType())

        var insertRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.accessToken}")
            .addHeader("Content-Type", "application/json")
            .post(utenteBody)
            .build()

        val insertResponse = client.newCall(insertRequest).execute()
        insertResponse.use { response ->
            if (response.code == 401 || response.code == 403) {
                val refreshResult = refreshTokenIfNeeded(context, client, session.refreshToken)
                if (refreshResult.isSuccess) {
                    val newSession = refreshResult.getOrThrow()
                    val tokenToUse = newSession.accessToken

                    insertRequest = insertRequest.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $tokenToUse")
                        .build()

                    client.newCall(insertRequest).execute().use { retryResponse ->
                        if (!retryResponse.isSuccessful && retryResponse.code != 201) {
                            val errorBody = retryResponse.body?.string()
                            return@withContext Result.failure(Exception("Inserimento fallito (${retryResponse.code}): $errorBody"))
                        }
                    }
                } else {
                    return@withContext Result.failure(Exception("Refresh del token fallito"))
                }
            } else if (!response.isSuccessful && response.code != 201) {
                val errorBody = response.body?.string()
                return@withContext Result.failure(Exception("Inserimento fallito (${response.code}): $errorBody"))
            }
        }
        return@withContext Result.success(Unit)


    } catch (e: Exception) {
        Result.failure(e)
    }
}

suspend fun inserisciStatisticheIniziali(context: Context, userEmail: String) = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val token = SessionManager.getAccessToken(context) ?: return@withContext null

    val initialStats = StatsUtente(
        utente = userEmail,
        partiteGiocate = 0,
        golFatti = 0,
        autogol = 0,
        vittorie = 0
    )
    val statsBody = Json.encodeToString(initialStats).toRequestBody("application/json".toMediaType())
    val statsRowInsertRequest = Request.Builder()
        .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/statistiche_utente")
        .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
        .addHeader("Authorization", "Bearer $token")
        .addHeader("Content-Type", "application/json")
        .post(statsBody)
        .build()

    client.newCall(statsRowInsertRequest).execute().use { statsResponse ->
        if (!statsResponse.isSuccessful) {
            Log.e("Errore Supabase: ${statsResponse.code}", " - ${statsResponse.body?.string()}")
        }
    }
}

suspend fun refreshTokenIfNeeded(
    context: Context,
    client: OkHttpClient,
    refreshToken: String
): Result<SignupResponse> = withContext(Dispatchers.IO) {
    try {
        val body = """
        {
            "refresh_token": "$refreshToken"
        }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/token?grant_type=refresh_token")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0") // usa sempre il tuo
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Refresh token fallito (${response.code})"))
            }

            val responseBody = response.body?.string()
                ?: return@withContext Result.failure(Exception("Corpo della risposta nullo"))

            val newSession = Json.decodeFromString(SignupResponse.serializer(), responseBody)
            SessionManager.saveTokens(context, newSession.accessToken, newSession.refreshToken)

            return@withContext Result.success(newSession)
        }

    } catch (e: Exception) {
        Result.failure(e)
    }
}
