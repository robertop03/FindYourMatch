package com.example.findyourmatch.utils

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.regex.Pattern

private const val TAG = "GEOCODING"

private suspend fun geocodeAddress(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
    Log.d(TAG, "Geocoding indirizzo: $address")
    val url = "https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf62481dce0cc3e0604d79abbf05952d0d6c86&text=${address.replace(" ", "+")}"

    val request = Request.Builder().url(url).get().build()
    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e(TAG, "Geocoding fallito. HTTP code: ${response.code}")
            return@withContext null
        }

        val body = response.body?.string()
        if (body == null) {
            Log.e(TAG, "Corpo della risposta nullo.")
            return@withContext null
        }

        val json = Json.parseToJsonElement(body).jsonObject

        val coordinates = json["features"]?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("geometry")?.jsonObject?.get("coordinates")?.jsonArray

        val lon = coordinates?.getOrNull(0)?.jsonPrimitive?.doubleOrNull
        val lat = coordinates?.getOrNull(1)?.jsonPrimitive?.doubleOrNull

        Log.d(TAG, "Geocoding result: lat=$lat, lon=$lon")

        if (lat != null && lon != null) Pair(lat, lon) else null
    }
}

private suspend fun calcolaDistanzaORS(
    from: Pair<Double, Double>,
    to: Pair<Double, Double>
): Double? = withContext(Dispatchers.IO) {
    Log.d(TAG, "Calcolo distanza da $from a $to")

    val bodyJson = """
        {
          "coordinates": [
            [${from.second}, ${from.first}],
            [${to.second}, ${to.first}]
          ]
        }
    """.trimIndent()

    val requestBody = bodyJson.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://api.openrouteservice.org/v2/directions/driving-car")
        .addHeader("Authorization", "5b3ce3597851110001cf62481dce0cc3e0604d79abbf05952d0d6c86")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e(TAG, "Errore OpenRouteService: ${response.code}")
            return@withContext null
        }

        val body = response.body?.string()
        if (body == null) {
            Log.e(TAG, "Corpo risposta OpenRoute nullo.")
            return@withContext null
        }

        val json = Json.parseToJsonElement(body).jsonObject
        val distanceMeters = json["routes"]
            ?.jsonArray?.getOrNull(0)?.jsonObject
            ?.get("summary")?.jsonObject
            ?.get("distance")?.jsonPrimitive?.doubleOrNull

        Log.d(TAG, "Distanza calcolata: ${distanceMeters?.div(1000)} km")
        return@withContext distanceMeters?.div(1000) // in km
    }
}

suspend fun calcolaDistanzaTraIndirizzi(
    indirizzo1: String,
    indirizzo2: String
): Double? {
    Log.d(TAG, "Calcolo distanza tra:\n - Indirizzo1: $indirizzo1\n - Indirizzo2: $indirizzo2")

    val coord1 = parseCoordinates(indirizzo1)?.let {
        Log.d(TAG, "Coord1 riconosciute come lat/lon dirette: $it")
        Pair(it.latitude, it.longitude)
    } ?: geocodeAddress(indirizzo1).also {
        Log.d(TAG, "Coord1 ottenute da geocoding: $it")
    }

    val coord2 = parseCoordinates(indirizzo2)?.let {
        Log.d(TAG, "Coord2 riconosciute come lat/lon dirette: $it")
        Pair(it.latitude, it.longitude)
    } ?: geocodeAddress(indirizzo2).also {
        Log.d(TAG, "Coord2 ottenute da geocoding: $it")
    }

    if (coord1 == null) {
        Log.e(TAG, "Coord1 è null")
    }
    if (coord2 == null) {
        Log.e(TAG, "Coord2 è null")
    }

    return if (coord1 != null && coord2 != null) {
        calcolaDistanzaORS(coord1, coord2)
    } else {
        Log.e(TAG, "Una delle due coordinate è nulla, impossibile calcolare la distanza.")
        null
    }
}

private fun parseCoordinates(input: String): LatLng? {
    val pattern = Pattern.compile("^(-?\\d+\\.?\\d*)[,\\s]*(-?\\d+\\.?\\d*)$")
    val matcher = pattern.matcher(input.trim())

    return if (matcher.matches()) {
        try {
            val latitude = matcher.group(1)?.toDouble()
            val longitude = matcher.group(2)?.toDouble()
            if (latitude != null && longitude != null) {
                Log.d(TAG, "Coordinate parseate da stringa: ($latitude, $longitude)")
                LatLng(latitude, longitude)
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Errore nel parsing delle coordinate: ${e.message}")
            null
        }
    } else {
        Log.d(TAG, "Input non corrisponde a pattern coordinate: $input")
        null
    }
}
