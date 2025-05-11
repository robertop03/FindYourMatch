package com.example.findyourmatch.data.user

import android.content.Context
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

        val signupResponse = client.newCall(signupRequest).execute()

        if (!signupResponse.isSuccessful) {
            return@withContext Result.failure(
                Exception("Registrazione fallita, un account con questa email è già registrato")
            )
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

        val loginResponse = client.newCall(loginRequest).execute()
        if (!loginResponse.isSuccessful) {
            return@withContext Result.failure(Exception("Login fallito dopo la registrazione (${loginResponse.code})"))
        }

        val loginString = loginResponse.body?.string()
            ?: return@withContext Result.failure(Exception("Nessuna risposta dal login"))

        val session = Json { ignoreUnknownKeys = true }
            .decodeFromString(SignupResponse.serializer(), loginString)


        // 2. Salva i token
        SessionManager.saveTokens(context, session.accessToken, session.refreshToken)

        // 3. Controlla se l’utente è già nella tabella `utenti`
        val checkRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email.trim()}")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.accessToken}")
            .addHeader("Accept", "application/json")
            .build()

        val checkResponse = client.newCall(checkRequest).execute()

        if (!checkResponse.isSuccessful) {
            return@withContext Result.failure(Exception("Errore nel controllo duplicato email: ${checkResponse.code}"))
        }

        val checkBody = checkResponse.body?.string() ?: "[]"
        if (checkBody.contains(email.trim(), ignoreCase = true)) {
            return@withContext Result.failure(Exception("Un account con questa email è già registrato."))
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

        val utenteJson = Json.encodeToString(utente)
        val utenteBody = utenteJson.toRequestBody("application/json".toMediaType())

        val insertRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.accessToken}")
            .addHeader("Content-Type", "application/json")
            .post(utenteBody)
            .build()

        val tokenToUse: String

        var insertResponse = client.newCall(insertRequest).execute()

        if (insertResponse.code == 401 || insertResponse.code == 403) {
            // Token scaduto, tentiamo refresh
            val refreshResult = refreshTokenIfNeeded(context, client, session.refreshToken)
            if (refreshResult.isSuccess) {
                val newSession = refreshResult.getOrThrow()
                tokenToUse = newSession.accessToken

                // Nuova richiesta con token aggiornato
                val retryInsertRequest = insertRequest.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $tokenToUse")
                    .build()

                insertResponse = client.newCall(retryInsertRequest).execute()
            } else {
                return@withContext Result.failure(Exception("Refresh del token fallito"))
            }
        }

        if (!insertResponse.isSuccessful && insertResponse.code != 201) {
            val errorBody = insertResponse.body?.string()
            return@withContext Result.failure(
                Exception("Inserimento fallito (${insertResponse.code}): $errorBody")
            )
        }

        return@withContext Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return@withContext Result.failure(Exception("Refresh token fallito (${response.code})"))
        }

        val responseBody = response.body?.string()
            ?: return@withContext Result.failure(Exception("Corpo della risposta nullo"))

        val newSession = Json.decodeFromString(SignupResponse.serializer(), responseBody)
        SessionManager.saveTokens(context, newSession.accessToken, newSession.refreshToken)

        Result.success(newSession)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
