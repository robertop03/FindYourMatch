package com.example.findyourmatch.data.user

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
data class SignupResponse(val access_token: String, val refresh_token: String)

@Serializable
data class Utente(
    val email: String,
    val nome: String,
    val cognome: String,
    val data_nascita: String,
    val password: String,
    val salt: String,
    val sesso: String,
    val data_iscrizione: String,
    val telefono: String
)

suspend fun registraUtenteSupabase(
    context: Context,
    email: String,
    password: String,
    nome: String,
    cognome: String,
    dataNascita: String,
    salt: String,
    hashedPassword: String,
    sesso: String,
    telefono: String
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
        SessionManager.saveTokens(context, session.access_token, session.refresh_token)

        // 3. Controlla se l’utente è già nella tabella `utenti`
        val checkRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti?email=eq.${email.trim()}")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.access_token}")
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
            data_nascita = dataNascita,
            password = hashedPassword,
            salt = salt,
            sesso = sesso,
            data_iscrizione = LocalDate.now().toString(),
            telefono = telefono,
        )

        val utenteJson = Json.encodeToString(utente)
        println("UTENTE JSON: $utenteJson")
        val utenteBody = utenteJson.toRequestBody("application/json".toMediaType())
        println("UTENTE JSON: $utenteBody")

        val insertRequest = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/rest/v1/utenti")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Authorization", "Bearer ${session.access_token}")
            .addHeader("Content-Type", "application/json")
            .post(utenteBody)
            .build()

        val insertResponse = client.newCall(insertRequest).execute()
        if (!insertResponse.isSuccessful) {
            return@withContext Result.failure(Exception("Inserimento dati fallito: ${insertResponse.code}"))
        }

        Result.success(Unit)

    } catch (e: Exception) {
        Result.failure(e)
    }
}