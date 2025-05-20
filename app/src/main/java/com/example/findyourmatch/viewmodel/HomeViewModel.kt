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

    var areDataLoaded = false
        private set
    var errore: String? = null

    fun loadPartite(
        isLoggedIn: Boolean,
        isPermissionGranted: Boolean,
        maxDistance: Float,
        fusedLocationClient: FusedLocationProviderClient,
        forzaRicarica: Boolean = false
    ) {
        if ((areDataLoaded && !forzaRicarica) || isFetching) return

        isFetching = true
        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {
            try {
                val tempoTotale = measureTimeMillis {
                    Log.d("HomeVM", "Inizio loadPartite()")

                    val tempoPartite = measureTimeMillis {
                        Log.d("HomeVM", "Inizio fetch partite da Supabase")
                        val tuttePartite = getPartiteConCampo(context)
                        Log.d("HomeVM", "Fine fetch partite: ${tuttePartite.size} partite")

                        val indirizzoUtente = if (isLoggedIn) getIndirizzoUtente(context) else null
                        Log.d("HomeVM", "Indirizzo utente: $indirizzoUtente")

                        val tempoFiltraggio = measureTimeMillis {
                            partiteFiltrate = when {
                                isLoggedIn && indirizzoUtente != null -> {
                                    tuttePartite.map { partita ->
                                        async {
                                            val inizio = System.currentTimeMillis()
                                            val distanzaKm = calcolaDistanzaTraIndirizzi(
                                                indirizzo1 = "${indirizzoUtente.via}, ${indirizzoUtente.civico}, ${indirizzoUtente.citta}, ${indirizzoUtente.provincia}, ${indirizzoUtente.stato}",
                                                indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                            )
                                            val fine = System.currentTimeMillis()
                                            Log.d("HomeVM", "Distanza (${partita.campo.nome}): $distanzaKm km in ${fine - inizio}ms")
                                            Log.d("AAAA",
                                                (distanzaKm != null && distanzaKm <= maxDistance).toString()
                                            )
                                            if (distanzaKm != null && distanzaKm <= maxDistance) partita else null
                                        }
                                    }.awaitAll().filterNotNull()
                                }

                                isPermissionGranted && ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    try {
                                        val location = fusedLocationClient
                                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                            .await()
                                        Log.d("HomeVM", "Posizione corrente: ${location.latitude}, ${location.longitude}")

                                        tuttePartite.map { partita ->
                                            async {
                                                val inizio = System.currentTimeMillis()
                                                val distanzaKm = calcolaDistanzaTraIndirizzi(
                                                    indirizzo1 = "${location.latitude}, ${location.longitude}",
                                                    indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                                                )
                                                val fine = System.currentTimeMillis()
                                                Log.d("HomeVM", "Distanza (${partita.campo.nome}): $distanzaKm km in ${fine - inizio}ms")
                                                Log.d("AAAA",
                                                    (distanzaKm != null && distanzaKm <= maxDistance).toString()
                                                )
                                                Log.d("maxDistance", maxDistance.toString())
                                                if (distanzaKm != null && distanzaKm <= maxDistance) partita else null
                                            }
                                        }.awaitAll().filterNotNull()
                                    } catch (e: Exception) {
                                        errore = "Errore localizzazione: ${e.message}"
                                        Log.e("HomeVM", errore ?: "")
                                        emptyList()
                                    }
                                }

                                else -> emptyList()
                            }
                        }
                        Log.d("HomeVM", "Tempo filtraggio partite: ${tempoFiltraggio}ms")
                    }

                    Log.d("HomeVM", "Tempo totale caricamento: ${tempoPartite}ms")
                    areDataLoaded = true
                }
                Log.d("HomeVM", "Fine loadPartite(). Durata totale: ${tempoTotale}ms")
            } catch (e: Exception) {
                errore = e.message
                Log.e("HomeVM", "Errore durante il caricamento: ${e.message}")
            } finally {
                isFetching = false
            }
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