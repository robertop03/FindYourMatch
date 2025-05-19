package com.example.findyourmatch.utils

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

private suspend fun geocodeAddress(address: String): Pair<Double, Double>? = withContext(
    Dispatchers.IO) {
    val url = "https://api.openrouteservice.org/geocode/search?api_key=5b3ce3597851110001cf62481dce0cc3e0604d79abbf05952d0d6c86&text=${address.replace(" ", "+")}"

    val request = Request.Builder().url(url).get().build()
    val response = OkHttpClient().newCall(request).execute()
    if (!response.isSuccessful) return@withContext null

    val body = response.body?.string() ?: return@withContext null
    val json = Json.parseToJsonElement(body).jsonObject

    val coordinates = json["features"]?.jsonArray?.getOrNull(0)
        ?.jsonObject?.get("geometry")?.jsonObject?.get("coordinates")?.jsonArray

    val lon = coordinates?.getOrNull(0)?.jsonPrimitive?.doubleOrNull
    val lat = coordinates?.getOrNull(1)?.jsonPrimitive?.doubleOrNull

    if (lat != null && lon != null) Pair(lat, lon) else null
}

private suspend fun calcolaDistanzaORS(
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
        .addHeader("Authorization", "5b3ce3597851110001cf62481dce0cc3e0604d79abbf05952d0d6c86")
        .post(requestBody)
        .build()

    val response = OkHttpClient().newCall(request).execute()
    if (!response.isSuccessful) return@withContext null

    val body = response.body?.string() ?: return@withContext null
    val json = Json.parseToJsonElement(body).jsonObject

    val distanceMeters = json["routes"]
        ?.jsonArray?.get(0)?.jsonObject
        ?.get("summary")?.jsonObject
        ?.get("distance")?.jsonPrimitive?.doubleOrNull

    distanceMeters?.div(1000) // in km
}

suspend fun calcolaDistanzaTraIndirizzi(
    indirizzo1: String,
    indirizzo2: String
): Double? {
    val coord1 = geocodeAddress(indirizzo1)
    val coord2 = geocodeAddress(indirizzo2)

    if (coord1 != null && coord2 != null) {
        return calcolaDistanzaORS(coord1, coord2)
    }
    return null
}
