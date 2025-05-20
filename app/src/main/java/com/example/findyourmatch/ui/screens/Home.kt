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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextDecoration
import com.example.findyourmatch.data.user.IndirizzoUtente
import com.example.findyourmatch.data.user.getIndirizzoUtente
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.utils.calcolaDistanzaTraIndirizzi
import com.example.findyourmatch.viewmodel.SessionViewModel
import kotlinx.datetime.Instant
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@Composable
fun Home(navController: NavHostController, sessionViewModel: SessionViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val showSettingsDialog = remember { mutableStateOf(false) }

    val partiteFiltrate = remember { mutableStateOf<List<PartitaConCampo>>(emptyList()) }
    val indirizzoUtente = remember { mutableStateOf<IndirizzoUtente?>(null) }
    val maxDistance by userSettings.maxDistance.collectAsState(initial = 50f)
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()


    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        isPermissionGranted = granted // Aggiorna lo stato del permesso in base alla risposta dell'utente
        Log.d("Home", "Permesso di localizzazione ${if (granted) "concesso" else "negato"} dal launcher.")
        // Non innescare qui il caricamento delle partite, lascialo a LaunchedEffect(isPermissionGranted)
    }

    var hasRequestedPermission by remember { mutableStateOf(false) }



    LaunchedEffect(key1 = isPermissionGranted, key2 = hasRequestedPermission) {
        if (!isPermissionGranted && !hasRequestedPermission) {
            hasRequestedPermission = true
            delay(200)
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
                (context as Activity),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (shouldShow) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                // Se non dovremmo mostrarlo, probabilmente è stato negato per sempre
                showSettingsDialog.value = true
            }
        }
    }



    // ✅ Carica le partite solo dopo che il permesso è concesso o l’utente è loggato
    LaunchedEffect(isLoggedIn, isPermissionGranted) {
        Log.d("Home", "LaunchedEffect(isLoggedIn, isPermissionGranted) avviato. isLoggedIn: $isLoggedIn, isPermissionGranted: $isPermissionGranted")

        val tuttePartite = getPartiteConCampo(context)
        Log.d("TUTTE PARTITE", tuttePartite.toString())
        val partiteFiltrateTemp = mutableListOf<PartitaConCampo>()

        if (isLoggedIn) {
            Log.d("Home", "Utente loggato. Recupero indirizzo utente.")
            indirizzoUtente.value = getIndirizzoUtente(context)
            // TODO: Filtra partite in base all'indirizzo salvato dell'utente
            tuttePartite.forEach { partita ->
                // Esempio: se hai la latitudine e longitudine dell'indirizzo utente
                val userAddr = indirizzoUtente.value
                if (userAddr != null) {
                    val distanzaKm = calcolaDistanzaTraIndirizzi(
                        indirizzo1 = "${userAddr.via}, ${userAddr.civico}, ${userAddr.citta}, ${userAddr.provincia}, ${userAddr.stato}",
                        indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                    )
                    if (distanzaKm != null && distanzaKm <= maxDistance) {
                        partiteFiltrateTemp.add(partita)
                    }
                } else {
                    // Nessun indirizzo salvato, magari non aggiungere o gestire diversamente
                }
            }
        } else if (isPermissionGranted) { // Solo se il permesso è concesso e non loggato
            Log.d("Home", "Permesso di localizzazione concesso (non loggato). Recupero posizione corrente.")
            try {
                // `await()` è una funzione di sospensione, richiede un contesto di coroutine
                val location = fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .await()

                val userLat = location.latitude
                val userLon = location.longitude
                Log.d("Home", "Posizione corrente: Lat=$userLat, Lon=$userLon")

                tuttePartite.forEach { partita ->
                    val distanzaKm = calcolaDistanzaTraIndirizzi(
                        indirizzo1 = "$userLat, $userLon",
                        indirizzo2 = "${partita.campo.via}, ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}, ${partita.campo.nazione}"
                    )
                    Log.d("DISTANZA KM", distanzaKm.toString())
                    Log.d("MAX DISTANCE", maxDistance.toString())
                    if (distanzaKm != null && distanzaKm <= maxDistance) {
                        Log.d("PARTITA", partita.toString())
                        partiteFiltrateTemp.add(partita)
                        Log.d("PARTITE FILTRATE TEMP", partiteFiltrateTemp.toString())
                    }
                }
            } catch (e: SecurityException) {
                // L'utente ha negato il permesso, o ci sono problemi con il provider di localizzazione
                Log.e("Home", "Errore nel recupero della posizione: ${e.message}")
                // Potresti voler mostrare un messaggio all'utente qui
            } catch (e: Exception) {
                Log.e("Home", "Errore generico nel recupero della posizione: ${e.message}")
            }
        } else {
            Log.d("Home", "Permesso di localizzazione non concesso e non loggato. Non recupero la posizione.")
            // Puoi mostrare un messaggio diverso qui se il permesso è negato
        }

        partiteFiltrate.value = partiteFiltrateTemp
        Log.d("PARTITE FILTRATE", partiteFiltrate.toString())
        Log.d("Home", "Trovate ${partiteFiltrate.value.size} partite filtrate.")
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
            Spacer(modifier = Modifier.height(16.dp))

            if (partiteFiltrate.value.isEmpty()) {
                Text("Nessuna partita trovata entro $maxDistance km.")
            } else {
                partiteFiltrate.value.forEach { partita ->
                    PartitaCard(partita)
                }
            }
        }
    }
    if (showSettingsDialog.value) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                    showSettingsDialog.value = false
                }) {
                    Text("Apri impostazioni")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog.value = false
                }) {
                    Text("Annulla")
                }
            },
            title = { Text("Permesso richiesto") },
            text = { Text("Per usare la posizione, abilita il permesso manualmente dalle impostazioni.") }
        )
    }
}






@Composable
fun PartitaCard(partita: PartitaConCampo) {
    val instant = Instant.parse(partita.dataOraInizio)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Black, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${dateTime.dayOfMonth} ${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.year}",
                            color = White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Ore: %02d:%02d".format(dateTime.hour, dateTime.minute),
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calcio a ${partita.tipo}",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = partita.campo.nome,
                color = White,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )

            Text(
                text = partita.campo.citta,
                color = White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Black, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${partita.partecipantiAttuali}/${partita.maxGiocatori}",
                        color = White
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}




fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
