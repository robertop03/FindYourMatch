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
import com.example.findyourmatch.data.user.IndirizzoUtente
import com.example.findyourmatch.data.user.getIndirizzoUtente
import com.example.findyourmatch.utils.calcolaDistanzaTraIndirizzi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.system.measureTimeMillis

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val geocodingCache = mutableMapOf<String, Pair<Double, Double>>()

    var partiteFiltrate by mutableStateOf<List<PartitaConCampo>>(emptyList())
        private set
    var isFetching by mutableStateOf(false)
        private set

    private var ultimaMaxDistance: Float? = null
    var errore: String? = null

    fun loadPartite(
        isLoggedIn: Boolean,
        isPermissionGranted: Boolean,
        maxDistance: Float,
        fusedLocationClient: FusedLocationProviderClient,
        forzaRicarica: Boolean = false
    ) {
        val shouldReload = forzaRicarica || ultimaMaxDistance != maxDistance

        if (!shouldReload || isFetching) return

        isFetching = true
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            try {
                ultimaMaxDistance = maxDistance

                val tuttePartite = getPartiteConCampo(context)
                val indirizzoUtente = if (isLoggedIn) getIndirizzoUtente(context) else null

                partiteFiltrate = when {
                    isLoggedIn && indirizzoUtente != null -> {
                        tuttePartite.map { partita ->
                            async {
                                val distanzaKm = calcolaDistanzaTraIndirizzi(
                                    indirizzo1 = "${indirizzoUtente.via}, ${indirizzoUtente.civico}, ${indirizzoUtente.citta}, ${indirizzoUtente.provincia}, ${indirizzoUtente.stato}",
                                    indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                )
                                if (distanzaKm != null && distanzaKm <= maxDistance) partita else null
                            }
                        }.awaitAll().filterNotNull()
                    }

                    isPermissionGranted && ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        val location = fusedLocationClient
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .await()

                        tuttePartite.map { partita ->
                            async {
                                val distanzaKm = calcolaDistanzaTraIndirizzi(
                                    indirizzo1 = "${location.latitude}, ${location.longitude}",
                                    indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                )
                                if (distanzaKm != null && distanzaKm <= maxDistance) partita else null
                            }
                        }.awaitAll().filterNotNull()
                    }

                    else -> emptyList()
                }
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
        onComplete: () -> Unit
    ) {
        loadPartite(
            isLoggedIn = isLoggedIn,
            isPermissionGranted = isPermissionGranted,
            maxDistance = maxDistance,
            fusedLocationClient = fusedLocationClient,
            forzaRicarica = true
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