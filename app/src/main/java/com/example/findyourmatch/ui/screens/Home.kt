package com.example.findyourmatch.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.PartitaConCampo
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.HomeViewModel
import com.example.findyourmatch.viewmodel.HomeViewModelFactory
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.example.findyourmatch.data.notifications.prendiNomiSquadreDaPartita
import com.example.findyourmatch.data.notifications.prendiNumeroPartecipantiInSquadra
import com.example.findyourmatch.data.user.getLoggedUserEmail
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import androidx.compose.material3.ModalBottomSheet
import java.util.Calendar
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavHostController, sessionViewModel: SessionViewModel) {
    val context = LocalContext.current
    val dataInizio = remember { mutableStateOf<LocalDate?>(null) }
    val dataFine = remember { mutableStateOf<LocalDate?>(null) }
    val calendar = Calendar.getInstance()

    val datePickerInizio = remember {
        DatePickerDialog(context).apply {
            datePicker.minDate = calendar.timeInMillis
            setOnDateSetListener { _, year, month, dayOfMonth ->
                dataInizio.value = LocalDate.of(year, month + 1, dayOfMonth)
            }
        }
    }

    val datePickerFine = remember {
        DatePickerDialog(context).apply {
            datePicker.minDate = calendar.timeInMillis
            setOnDateSetListener { _, year, month, dayOfMonth ->
                dataFine.value = LocalDate.of(year, month + 1, dayOfMonth)
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val showSettingsDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val maxDistanceState = userSettings.maxDistance.collectAsState(initial = null)
    val maxDistance = maxDistanceState.value
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var trovaTesto by remember { mutableStateOf("Trova") }
    var menuEspanso by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableIntStateOf(0) }
    var userEmail by remember { mutableStateOf<String?>(null) }
    val showFilterSheet = remember { mutableStateOf(false) }
    val selectedTipoPartita = remember { mutableStateOf<String?>(null) }
    val selectedPrezzo = remember { mutableStateOf<String?>(null) }



    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(context.applicationContext as Application))

    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (maxDistance != null) {
                isRefreshing = true
                homeViewModel.refreshPartite(
                    isLoggedIn = isLoggedIn,
                    isPermissionGranted = isPermissionGranted,
                    maxDistance = maxDistance,
                    fusedLocationClient = fusedLocationClient,
                    trovaTesto = trovaTesto,
                    userEmail = userEmail,

                ) {
                    isRefreshing = false
                }
            }
        }
    )

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        isPermissionGranted = granted
        if (!granted) showSettingsDialog.value = true
    }

    var hasRequestedPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userEmail = getLoggedUserEmail(context)
        if (!isPermissionGranted && !hasRequestedPermission) {
            hasRequestedPermission = true
            val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
                (context as Activity),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (shouldShow) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                showSettingsDialog.value = true
            }
        }
    }

    // Caricamento iniziale
    LaunchedEffect(userEmail, maxDistance) {
        if (maxDistance != null) {
            homeViewModel.loadPartite(
                isLoggedIn = isLoggedIn,
                isPermissionGranted = isPermissionGranted,
                maxDistance = maxDistance,
                fusedLocationClient = fusedLocationClient,
                trovaTesto = trovaTesto,
                userEmail = userEmail
            )
        }
    }


    // Chiamata per il cambio di modalità
    LaunchedEffect(key1 = trovaTesto) {
        if (userEmail != null && maxDistance != null) {
            homeViewModel.loadPartite(
                isLoggedIn = isLoggedIn,
                isPermissionGranted = isPermissionGranted,
                maxDistance = maxDistance,
                fusedLocationClient = fusedLocationClient,
                trovaTesto = trovaTesto,
                userEmail = userEmail,
                forzaRicarica = true
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .pullRefresh(pullRefreshState)
            .padding(8.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = localizedContext.getString(R.string.miei_calcetti),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Black, shape = RoundedCornerShape(50))
                        .clickable {
                            showFilterSheet.value = true
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(localizedContext.getString(R.string.filtra), color = White, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            dropdownWidth = coordinates.size.width
                        }
                ) {
                    // Bottone
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Black, shape = RoundedCornerShape(50))
                            .clickable {
                                menuEspanso = true
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(trovaTesto, color = White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Dropdown con stessa larghezza del bottone
                    DropdownMenu(
                        expanded = menuEspanso,
                        onDismissRequest = { menuEspanso = false },
                        modifier = Modifier.width(with(LocalDensity.current) { dropdownWidth.toDp() })
                    ) {
                        val alternativa = if (trovaTesto == localizedContext.getString(R.string.trova)) localizedContext.getString(R.string.gestisci) else localizedContext.getString(R.string.trova)
                        DropdownMenuItem(
                            text = { Text(alternativa) },
                            onClick = {
                                trovaTesto = alternativa
                                menuEspanso = false
                            }
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (homeViewModel.isFetching) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(localizedContext.getString(R.string.caricamento_partite), color = MaterialTheme.colorScheme.primary)
                    }
                } else if (homeViewModel.partiteFiltrate.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.no_matches),
                            contentDescription = localizedContext.getString(R.string.nessun_calcetto),
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 16.dp)
                        )

                        Text(
                            text = localizedContext.getString(R.string.nessun_calcetto_trovato),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = localizedContext.getString(R.string.cambia_limite_km),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { navController.navigate(NavigationRoute.Settings) }
                        )
                    }
                } else {
                    homeViewModel.partiteFiltrate.forEach { partita ->
                        PartitaCard(
                            partita = partita,
                            sessionViewModel = sessionViewModel,
                            onLoginRequired = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = localizedContext.getString(R.string.effettua_login),
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            navController = navController
                        )
                    }
                }
            }
        }


        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
                    Text(localizedContext.getString(R.string.apri_impostazioni))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog.value = false }) {
                    Text(localizedContext.getString(R.string.annulla))
                }
            },
            title = { Text(localizedContext.getString(R.string.permesso_richiesto)) },
            text = { Text(localizedContext.getString(R.string.permesso_richiesto_testo)) }
        )
    }

    if (showFilterSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet.value = false }
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(localizedContext.getString(R.string.tipo_partita), fontWeight = FontWeight.Bold)
                listOf(localizedContext.getString(R.string.calcio_a_11), localizedContext.getString(R.string.calcio_a_8), localizedContext.getString(R.string.calcio_a_7), localizedContext.getString(R.string.calcio_a_5)).forEach { tipo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTipoPartita.value = tipo }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedTipoPartita.value == tipo,
                            onClick = { selectedTipoPartita.value = tipo }
                        )
                        Text(tipo)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(localizedContext.getString(R.string.periodo), fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { datePickerInizio.show() }) {
                        Text("${localizedContext.getString(R.string.dal)}: ${dataInizio.value?.toString() ?: localizedContext.getString(R.string.seleziona)}")
                    }
                    TextButton(onClick = { datePickerFine.show() }) {
                        Text("${localizedContext.getString(R.string.al)}: ${dataFine.value?.toString() ?: localizedContext.getString(R.string.seleziona)}")
                    }
                }


                Spacer(Modifier.height(12.dp))

                Text(localizedContext.getString(R.string.prezzo_previsto), fontWeight = FontWeight.Bold)
                listOf("0–5€", "5–10€", localizedContext.getString(R.string.sopra_10)).forEach { fascia ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPrezzo.value = fascia }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedPrezzo.value == fascia,
                            onClick = { selectedPrezzo.value = fascia }
                        )
                        Text(fascia)
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        showFilterSheet.value = false
                        homeViewModel.loadPartite(
                            isLoggedIn = isLoggedIn,
                            isPermissionGranted = isPermissionGranted,
                            maxDistance = maxDistance ?: 10f,
                            fusedLocationClient = fusedLocationClient,
                            trovaTesto = trovaTesto,
                            userEmail = userEmail,
                            tipoFiltro = selectedTipoPartita.value,
                            fasciaPrezzoFiltro = selectedPrezzo.value,
                            dataInizioFiltro = dataInizio.value,
                            dataFineFiltro = dataFine.value,
                            forzaRicarica = true
                        )

                    }
                ) {
                    Text(localizedContext.getString(R.string.applica_filtri), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PartitaCard(partita: PartitaConCampo, sessionViewModel: SessionViewModel, onLoginRequired: () -> Unit, navController: NavHostController) {
    val instant = Instant.parse(partita.dataOraInizio)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val coroutineScope = rememberCoroutineScope()

    var partecipantiAttuali by remember { mutableIntStateOf(0) }

    LaunchedEffect(partita.idPartita) {
        coroutineScope.launch {
            val squadre = prendiNomiSquadreDaPartita(context, partita.idPartita)
            val squadra1 = squadre.getOrNull(0) ?: "?"
            val squadra2 = squadre.getOrNull(1) ?: "?"
            val partecipanti1 = prendiNumeroPartecipantiInSquadra(context, squadra1, partita.idPartita)
            val partecipanti2 = prendiNumeroPartecipantiInSquadra(context, squadra2, partita.idPartita)
            partecipantiAttuali = partecipanti1 + partecipanti2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .clickable {
                if (!sessionViewModel.isLoggedIn.value) {
                    onLoginRequired()
                }else{
                    val id = partita.idPartita
                    navController.navigate("partita/$id")
                }
            }
            .padding(16.dp)
    ) {
        Column {
            // Data e ora
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
                        text = "${localizedContext.getString(R.string.ore)}: %02d:%02d".format(dateTime.hour, dateTime.minute),
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${localizedContext.getString(R.string.calcio_a)} ${partita.tipo}",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable {
                            val indirizzoCompleto = "${partita.campo.via} ${partita.campo.civico}, ${partita.campo.citta}, ${partita.campo.provincia}"
                            val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(indirizzoCompleto)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            context.startActivity(mapIntent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }

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

            Text(
                text = "${localizedContext.getString(R.string.distanza)}: ${"%.1f".format(partita.distanzaKm)} km",
                color = White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Black, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${partecipantiAttuali}/${partita.maxGiocatori}",
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

                Text(
                    text = "%.2f€".format(partita.importoPrevisto),
                    color = White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
