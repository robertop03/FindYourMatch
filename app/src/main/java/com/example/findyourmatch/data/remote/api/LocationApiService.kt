package com.example.findyourmatch.data.remote.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import kotlinx.serialization.json.*

suspend fun fetchEUCountries(client: HttpClient): List<String> {
    val response: HttpResponse = client.get("https://parseapi.back4app.com/classes/list_of_eu_union_countries") {
        headers {
            append("X-Parse-Application-Id", "9Bha7GuEMZ1gq1ayAox509YkBhBYCFnwTTvV1QoU")
            append("X-Parse-REST-API-Key", "0072qRjDbUoPphEGUXKAfVVMzlwpfqXFpcPE1U14")
        }
    }

    val json = Json.decodeFromString<JsonObject>(response.bodyAsText())
    val results = json["results"]?.jsonArray
        ?: throw Exception("La risposta non contiene 'results'")
    return results.map { it.jsonObject["country"]!!.jsonPrimitive.content }

}

suspend fun fetchProvincesByCountry(client: HttpClient, country: String): List<String> {
    val response = client.get("https://parseapi.back4app.com/classes/provinces_by_country") {
        headers {
            append("X-Parse-Application-Id", "9Bha7GuEMZ1gq1ayAox509YkBhBYCFnwTTvV1QoU")
            append("X-Parse-REST-API-Key", "0072qRjDbUoPphEGUXKAfVVMzlwpfqXFpcPE1U14")
        }
        url {
            parameters.append("where", """{"country":"$country"}""")
        }
    }

    if (!response.status.isSuccess()) {
        throw Exception("Errore HTTP: ${response.status}")
    }

    val json = Json.decodeFromString<JsonObject>(response.bodyAsText())
    val results = json["results"]?.jsonArray ?: return emptyList()
    return results.map { it.jsonObject["province"]!!.jsonPrimitive.content }
}
