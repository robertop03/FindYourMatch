package com.example.findyourmatch.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.match.PartitaConCampo
import com.example.findyourmatch.data.match.getPartiteConCampo
import com.example.findyourmatch.data.user.getIndirizzoUtente
import com.example.findyourmatch.utils.calcolaDistanzaTraIndirizzi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    var partiteFiltrate by mutableStateOf<List<PartitaConCampo>>(emptyList())
        private set
    var isFetching by mutableStateOf(false)
        private set

    private var ultimaMaxDistance: Float? = null
    private var ultimoTrovaTesto: String? = null
    private var errore: String? = null
    private var partiteCached: List<PartitaConCampo>? = null


    fun loadPartite(
        isLoggedIn: Boolean,
        isPermissionGranted: Boolean,
        maxDistance: Float,
        fusedLocationClient: FusedLocationProviderClient,
        trovaTesto: String,
        userEmail: String?,
        tipoFiltro: String? = null,
        fasciaPrezzoFiltro: String? = null,
        forzaRicarica: Boolean = false,
        dataInizioFiltro: LocalDate? = null,
        dataFineFiltro: LocalDate? = null
    ) {
        val shouldReload = forzaRicarica || ultimaMaxDistance != maxDistance || trovaTesto != ultimoTrovaTesto

        if (!shouldReload && partiteCached != null) {
            partiteFiltrate = partiteCached!!
            return
        }

        isFetching = true
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            try {
                ultimaMaxDistance = maxDistance
                ultimoTrovaTesto = trovaTesto

                val tuttePartite = getPartiteConCampo(context).filter { it.visibile }

                val indirizzoUtente = if (isLoggedIn) getIndirizzoUtente(context) else null

                val partiteFiltrateUtente = tuttePartite.filter { partita ->
                    Log.d("TROVATESTO", trovaTesto)
                    when (trovaTesto) {
                        "Gestisci", "Manage" -> partita.creatore == userEmail
                        "Trova", "Find" -> partita.creatore != userEmail
                        else -> true
                    }
                }

                val partiteFiltrateTipoPrezzo = partiteFiltrateUtente.filter { partita ->
                    val tipoUtente = tipoFiltro?.removePrefix("Calcio a ")?.trim()
                    val tipoDb = when (tipoUtente) {
                        "11" -> "11vs11"
                        "8" -> "8vs8"
                        "7" -> "7vs7"
                        "5" -> "5vs5"
                        else -> null
                    }
                    val tipoOk = tipoDb == null || partita.tipo.equals(tipoDb, ignoreCase = true)

                    val prezzoOk = when (fasciaPrezzoFiltro) {
                        "0–5€" -> partita.importoPrevisto <= 5
                        "5–10€" -> partita.importoPrevisto > 5 && partita.importoPrevisto <= 10
                        "Sopra 10€" -> partita.importoPrevisto > 10
                        else -> true
                    }

                    val dataOk = if (dataInizioFiltro == null && dataFineFiltro == null) {
                        true
                    } else {
                        try {
                            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            val dataPartita = ZonedDateTime.parse(partita.dataOraInizio, formatter).toLocalDate()
                            (dataInizioFiltro == null || !dataPartita.isBefore(dataInizioFiltro)) &&
                                    (dataFineFiltro == null || !dataPartita.isAfter(dataFineFiltro))
                        } catch (e: Exception) {
                            false
                        }
                    }

                    tipoOk && prezzoOk && dataOk
                }


                partiteFiltrate = when {
                    isLoggedIn && indirizzoUtente != null -> {
                        partiteFiltrateTipoPrezzo.map { partita ->
                            async {
                                val distanzaKm = calcolaDistanzaTraIndirizzi(
                                    indirizzo1 = "${indirizzoUtente.via}, ${indirizzoUtente.civico}, ${indirizzoUtente.citta}, ${indirizzoUtente.provincia}, ${indirizzoUtente.stato}",
                                    indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                )
                                if (distanzaKm != null && distanzaKm <= maxDistance){
                                    partita.distanzaKm = distanzaKm
                                    partita
                                } else null
                            }
                        }.awaitAll().filterNotNull()
                    }

                    isPermissionGranted && ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        val location = fusedLocationClient
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .await()

                        partiteFiltrateTipoPrezzo.map { partita ->
                            async {
                                val distanzaKm = calcolaDistanzaTraIndirizzi(
                                    indirizzo1 = "${location.latitude}, ${location.longitude}",
                                    indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                )
                                if (distanzaKm != null && distanzaKm <= maxDistance){
                                    partita.distanzaKm = distanzaKm
                                    partita
                                } else null
                            }
                        }.awaitAll().filterNotNull()

                    }

                    else -> emptyList()
                }
                partiteCached = partiteFiltrate
            } catch (e: Exception) {
                errore = e.message
                partiteFiltrate = emptyList()
            } finally {
                isFetching = false
            }
        }
    }

    fun refreshPartite(
        isLoggedIn: Boolean,
        isPermissionGranted: Boolean,
        maxDistance: Float,
        fusedLocationClient: FusedLocationProviderClient,
        trovaTesto: String,
        userEmail: String?,
        onComplete: () -> Unit
    ) {
        loadPartite(
            isLoggedIn = isLoggedIn,
            isPermissionGranted = isPermissionGranted,
            maxDistance = maxDistance,
            fusedLocationClient = fusedLocationClient,
            trovaTesto = trovaTesto,
            userEmail = userEmail,
            forzaRicarica = true,

        )
        viewModelScope.launch {
            // Attendiamo fino al termine del caricamento
            while (isFetching) kotlinx.coroutines.delay(100)
            onComplete()
        }
    }

}

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}