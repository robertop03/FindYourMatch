package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.PartitaConCampo
import com.example.findyourmatch.data.match.getPartiteConCampo
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import android.util.Log
import com.example.findyourmatch.data.user.IndirizzoUtente
import com.example.findyourmatch.data.user.getIndirizzoUtente
import com.example.findyourmatch.utils.calcolaDistanzaTraIndirizzi


@Composable
fun Home(navController: NavHostController) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val partite = remember { mutableStateOf<List<PartitaConCampo>>(emptyList()) }
    val indirizzoUtente = remember { mutableStateOf<IndirizzoUtente?>(null) }

    LaunchedEffect(Unit) {
        val result = getPartiteConCampo(context)
        Log.d("PARTITA", "Numero partite ricevute: ${result.size}")
        partite.value = result
        result.forEach { partita ->
            Log.d("PARTITA", """
            ID: ${partita.idPartita}
            Tipo: ${partita.tipo}
            Data inizio: ${partita.dataOraInizio}
            Scadenza iscrizione: ${partita.dataOraScadenzaIscrizione}
            Importo: ${partita.importoPrevisto}
            Max giocatori: ${partita.maxGiocatori}
            Visibile: ${partita.visibile}
            Creatore: ${partita.creatore}
            Campo:
                Nome: ${partita.campo.nome}
                Indirizzo: ${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta} (${partita.campo.provincia}), ${partita.campo.nazione}
        """.trimIndent())
        }
        val primaPartita = result[0]
        Log.d("PARTITA", primaPartita.toString())
        indirizzoUtente.value = getIndirizzoUtente(context)
        Log.d("INDIRIZZO UTENTE", indirizzoUtente.value.toString())

        val distanzaKm = calcolaDistanzaTraIndirizzi(
            indirizzo1 = "Via ${indirizzoUtente.value?.via}, ${indirizzoUtente.value?.civico}, ${indirizzoUtente.value?.citta}, ${indirizzoUtente.value?.provincia}, ${indirizzoUtente.value?.stato}",
            indirizzo2 = "Via ${primaPartita.campo.via}, ${primaPartita.campo.civico}, ${primaPartita.campo.citta}, ${primaPartita.campo.provincia}, ${primaPartita.campo.nazione}"
        )

        if (distanzaKm != null) {
            Log.d("DISTANZA", "I due indirizzi distano ${distanzaKm.format(2)} km")
        }

    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = localizedContext.getString(R.string.miei_calcetti),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )




        }
    }
}

fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
