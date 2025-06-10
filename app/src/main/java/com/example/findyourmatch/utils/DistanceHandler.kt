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


suspend fun geocodeAddress(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {

    val url = "https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf624873b4da21bbd14d4ba52baa81b26f94f7dd060dd879c55769818eb503&text=${address.replace(" ", "+")}"
    val request = Request.Builder().url(url).get().build()

    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            Log.e("GEOCODE", "Errore geocoding: ${response.code} - indirizzo: $address")
            return@withContext null
        }


        val body = response.body?.string() ?: return@withContext null
        val json = Json.parseToJsonElement(body).jsonObject

        val coordinates = json["features"]?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("geometry")?.jsonObject?.get("coordinates")?.jsonArray

        val lon = coordinates?.getOrNull(0)?.jsonPrimitive?.doubleOrNull
        val lat = coordinates?.getOrNull(1)?.jsonPrimitive?.doubleOrNull

        if (lat != null && lon != null) Pair(lat, lon) else null
    }
}

 suspend fun calcolaDistanzaORS(
    from: Pair<Double, Double>,
    to: Pair<Double, Double>
): Double? = withContext(Dispatchers.IO) {

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
        .addHeader("Authorization", "5b3ce3597851110001cf624873b4da21bbd14d4ba52baa81b26f94f7dd060dd879c55769818eb503")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            return@withContext null
        }

        val body = response.body?.string() ?: return@withContext null

        val json = Json.parseToJsonElement(body).jsonObject
        val distanceMeters = json["routes"]
            ?.jsonArray?.getOrNull(0)?.jsonObject
            ?.get("summary")?.jsonObject
            ?.get("distance")?.jsonPrimitive?.doubleOrNull

        return@withContext distanceMeters?.div(1000) // in km
    }
}

suspend fun calcolaDistanzaTraIndirizzi(
    indirizzo1: String,
    indirizzo2: String
): Double? {

    val coord1 = parseCoordinates(indirizzo1)?.let {
        Pair(it.latitude, it.longitude)
    } ?: geocodeAddress(indirizzo1)

    val coord2 = parseCoordinates(indirizzo2)?.let {
        Pair(it.latitude, it.longitude)
    } ?: geocodeAddress(indirizzo2)

    val result = if (coord1 != null && coord2 != null) {
        calcolaDistanzaORS(coord1, coord2)
    } else null

    return result
}


 fun parseCoordinates(input: String): LatLng? {
    val pattern = Pattern.compile("^(-?\\d+\\.?\\d*)[,\\s]*(-?\\d+\\.?\\d*)$")
    val matcher = pattern.matcher(input.trim())

    return if (matcher.matches()) {
        try {
            val latitude = matcher.group(1)?.toDouble()
            val longitude = matcher.group(2)?.toDouble()
            if (latitude != null && longitude != null) {
                LatLng(latitude, longitude)
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    } else {
        null
    }
}
