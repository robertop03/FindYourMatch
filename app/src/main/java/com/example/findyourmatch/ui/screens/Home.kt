package com.example.findyourmatch.ui.screens

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.example.findyourmatch.data.user.getLoggedUserEmail
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Home(navController: NavHostController, sessionViewModel: SessionViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val showSettingsDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val maxDistanceState = userSettings.maxDistance.collectAsState(initial = null)
    val maxDistance = maxDistanceState.value
    val readyToLoad = maxDistance != null
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var trovaTesto by remember { mutableStateOf("Trova") }
    var menuEspanso by remember { mutableStateOf(false) }
    var dropdownWidth by remember { mutableIntStateOf(0) }
    var userEmail by remember { mutableStateOf<String?>(null) }

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
        if (userEmail != null && maxDistance != null) {
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
                        .clickable { /* logica filtro */ }
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
                        Text("Filtra", color = White, fontWeight = FontWeight.Bold)
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
                        val alternativa = if (trovaTesto == "Trova") "Gestisci" else "Trova"
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
                        Text("Caricando partite...", color = MaterialTheme.colorScheme.primary)
                    }
                } else if (homeViewModel.partiteFiltrate.isEmpty()) {
                    Text("Nessuna partita trovata entro $maxDistance km.")
                } else {
                    homeViewModel.partiteFiltrate.forEach { partita ->
                        PartitaCard(
                            partita = partita,
                            sessionViewModel = sessionViewModel,
                            onLoginRequired = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Effettua il login per partecipare",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
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
                    Text("Apri impostazioni")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog.value = false }) {
                    Text("Annulla")
                }
            },
            title = { Text("Permesso richiesto") },
            text = { Text("Per usare la posizione, abilita il permesso manualmente dalle impostazioni.") }
        )
    }
}



@Composable
fun PartitaCard(partita: PartitaConCampo, sessionViewModel: SessionViewModel, onLoginRequired: () -> Unit) {
    val instant = Instant.parse(partita.dataOraInizio)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .clickable {
                if(sessionViewModel.isLoggedIn.value){

                }else{
                    onLoginRequired()
                }
            }
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

            Text(
                text = "Distanza: ${"%.1f".format(partita.distanzaKm)} km",
                color = White,
                fontSize = 14.sp
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