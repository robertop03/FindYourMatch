package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.CampoSportivo
import com.example.findyourmatch.data.match.getSportsFields
import com.example.findyourmatch.data.match.insertNewMatch
import com.example.findyourmatch.data.remote.api.createHttpClient
import com.example.findyourmatch.data.remote.api.fetchEUCountries
import com.example.findyourmatch.data.remote.api.fetchProvincesByCountry
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.getLoggedUserEmail
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Green
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.White
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@Composable
fun CreaPartita(navController: NavHostController) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    var currentUser by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val httpClient = remember { createHttpClient() }
    var typeExpanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("") }
    var nationExpanded by remember { mutableStateOf(false) }
    var newPitchNation by remember { mutableStateOf("") }
    var provinceExpanded by remember { mutableStateOf(false) }
    var newPitchProvince by remember { mutableStateOf("") }
    var newPitchCity by remember { mutableStateOf("") }
    var newPitchStreet by remember { mutableStateOf("") }
    var newPitchHouseNumber by remember { mutableStateOf("") }
    var newPitchName by remember { mutableStateOf("") }
    var pitchExpanded by remember { mutableStateOf(false) }
    var pitch: CampoSportivo? = null
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var gameDate by remember { mutableStateOf("") }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var gameTime by remember { mutableStateOf("") }
    var showExpiringDatePickerDialog by remember { mutableStateOf(false) }
    var expiringDate by remember { mutableStateOf("") }
    var showExpringTimePickerDialog by remember { mutableStateOf(false) }
    var expiringTime by remember { mutableStateOf("") }
    var expectedAmount by remember { mutableStateOf("") }
    var team1Name by remember { mutableStateOf("") }
    var team2Name by remember { mutableStateOf("") }

    var euNations by remember { mutableStateOf(listOf<String>()) }
    var provinces by remember { mutableStateOf(listOf<String>()) }
    var sportsFields by remember { mutableStateOf(listOf<CampoSportivo>()) }

    LaunchedEffect (Unit) {
        currentUser = getLoggedUserEmail(context)
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        val showBackButton = navController.previousBackStackEntry != null

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.crea_partita),
                showBackButton = showBackButton
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = localizedContext.getString(R.string.creazione_in_corso),
                            color = Green
                        )
                    }
                }
            } else {
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.tipo_partita))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = true
                            )
                            .width(330.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded)
                        },
                        placeholder = {
                            Text(
                                localizedContext.getString(R.string.seleziona_tipo),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        val types = arrayOf("5vs5", "7vs7", "8vs8", "11vs11")
                        types.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    type = it
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.luogo) + " " + localizedContext.getString(R.string.spiegazione_campo))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                LaunchedEffect (Unit) {
                    sportsFields = getSportsFields(context)
                }
                ExposedDropdownMenuBox(
                    expanded = pitchExpanded,
                    onExpandedChange = { pitchExpanded = !pitchExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    OutlinedTextField(
                        value = if (pitch != null) pitch?.nome + " (${pitch?.citta}, ${pitch?.nazione})" else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = true
                            )
                            .width(330.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(pitchExpanded)
                        },
                        placeholder = {
                            Text(
                                localizedContext.getString(R.string.seleziona_campo),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = pitchExpanded,
                        onDismissRequest = { pitchExpanded = false }
                    ) {
                        sportsFields.forEach {
                            DropdownMenuItem(
                                text = { Text("${it.nome} (${it.citta}, ${it.nazione})") },
                                onClick = {
                                    pitch = CampoSportivo(
                                        idCampo = it.idCampo,
                                        nazione = it.nazione,
                                        provincia = it.provincia,
                                        citta = it.citta,
                                        via = it.via,
                                        civico = it.civico,
                                        nome = it.nome
                                    )
                                    pitchExpanded = false
                                    newPitchNation = ""
                                    newPitchProvince = ""
                                    newPitchCity = ""
                                    newPitchStreet = ""
                                    newPitchHouseNumber = ""
                                    newPitchName = ""
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(0.dp, 20.dp).width(330.dp).align(Alignment.CenterHorizontally)
                ) {
                    Divider(
                        thickness = 1.dp,
                        modifier = Modifier
                            .weight(1f),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = localizedContext.getString(R.string.oppure),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Divider(
                        thickness = 1.dp,
                        modifier = Modifier
                            .weight(1f),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = nationExpanded,
                    onExpandedChange = { nationExpanded = !nationExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    OutlinedTextField(
                        value = newPitchNation,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = true
                            )
                            .width(330.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                nationExpanded
                            )
                        },
                        placeholder = {
                            Text(
                                localizedContext.getString(R.string.stato),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    )
                    LaunchedEffect(Unit) {
                        euNations = fetchEUCountries(httpClient)
                    }
                    ExposedDropdownMenu(
                        expanded = nationExpanded,
                        onDismissRequest = { nationExpanded = false }
                    ) {
                        euNations.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    newPitchNation = it
                                    nationExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                LaunchedEffect(newPitchNation) {
                    if (newPitchNation.isNotBlank()) {
                        provinces = fetchProvincesByCountry(httpClient, newPitchNation)
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = !provinceExpanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    OutlinedTextField(
                        value = newPitchProvince,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryEditable,
                                enabled = true
                            )
                            .width(330.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                provinceExpanded
                            )
                        },
                        placeholder = { Text(localizedContext.getString(R.string.provincia)) }
                    )
                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false }
                    ) {
                        provinces.forEach {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        it,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                },
                                onClick = {
                                    newPitchProvince = it
                                    provinceExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPitchCity,
                    onValueChange = { newPitchCity = it },
                    placeholder = { Text(localizedContext.getString(R.string.citta)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPitchStreet,
                    onValueChange = { newPitchStreet = it },
                    placeholder = { Text(localizedContext.getString(R.string.via)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPitchHouseNumber,
                    onValueChange = { newPitchHouseNumber = it },
                    placeholder = { Text(localizedContext.getString(R.string.civico)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newPitchName,
                    onValueChange = { newPitchName = it },
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally),
                    placeholder = { Text(localizedContext.getString(R.string.campo_sportivo)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.data))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                TextField(
                    value = gameDate,
                    readOnly = true,
                    placeholder = { Text(localizedContext.getString(R.string.data_placeholder))},
                    onValueChange = {},
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showDatePickerDialog = true
                                    }
                                }
                            }
                        },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(330.dp)
                )

                if (showDatePickerDialog) {
                    val contextData = LocalContext.current
                    val calendar = Calendar.getInstance()
                    val dialog = DatePickerDialog(
                        contextData,
                        { _, year, month, dayOfMonth ->
                            gameDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            showDatePickerDialog = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    dialog.setOnCancelListener {
                        showDatePickerDialog = false
                    }
                    dialog.show()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.ore))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                TextField(
                    value = gameTime,
                    readOnly = true,
                    placeholder = { Text("HH:mm") },
                    onValueChange = {},
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showTimePickerDialog = true
                                    }
                                }
                            }
                        },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(330.dp)
                )

                if (showTimePickerDialog) {
                    LaunchedEffect (Unit) {
                        val calendar = Calendar.getInstance()
                        val dialog = TimePickerDialog(
                            context,
                            { _: TimePicker, hour: Int, minute: Int ->
                                gameTime = String.format("%02d:%02d", hour, minute)
                                showTimePickerDialog = false
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        dialog.setOnCancelListener {
                            showTimePickerDialog = false
                        }
                        dialog.show()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.data_scadenza))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                TextField(
                    value = expiringDate,
                    readOnly = true,
                    placeholder = { Text(localizedContext.getString(R.string.data_placeholder))},
                    onValueChange = {},
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showExpiringDatePickerDialog = true
                                    }
                                }
                            }
                        },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(330.dp)
                )

                if (showExpiringDatePickerDialog) {
                    val contextData = LocalContext.current
                    val calendar = Calendar.getInstance()
                    val dialog = DatePickerDialog(
                        contextData,
                        { _, year, month, dayOfMonth ->
                            expiringDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                            showExpiringDatePickerDialog = false
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    dialog.setOnCancelListener {
                        showExpiringDatePickerDialog = false
                    }
                    dialog.show()
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.orario_scadenza))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                TextField(
                    value = expiringTime,
                    readOnly = true,
                    placeholder = { Text("HH:mm") },
                    onValueChange = {},
                    singleLine = true,
                    interactionSource = remember { MutableInteractionSource() }
                        .also { interactionSource ->
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collect {
                                    if (it is PressInteraction.Release) {
                                        showExpringTimePickerDialog = true
                                    }
                                }
                            }
                        },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(330.dp)
                )

                if (showExpringTimePickerDialog) {
                    LaunchedEffect (Unit) {
                        val calendar = Calendar.getInstance()
                        val dialog = TimePickerDialog(
                            context,
                            { _: TimePicker, hour: Int, minute: Int ->
                                expiringTime = String.format("%02d:%02d", hour, minute)
                                showExpringTimePickerDialog = false
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        )
                        dialog.setOnCancelListener {
                            showExpringTimePickerDialog = false
                        }
                        dialog.show()
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.importo))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                OutlinedTextField(
                    value = expectedAmount,
                    onValueChange = { expectedAmount = it },
                    placeholder = { Text("0.0") },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Euro,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.nome_squadra_1))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                OutlinedTextField(
                    value = team1Name,
                    onValueChange = { team1Name = it },
                    placeholder = { Text(localizedContext.getString(R.string.ins_nome_squadra_1)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.nome_squadra_2))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                OutlinedTextField(
                    value = team2Name,
                    onValueChange = { team2Name = it },
                    placeholder = { Text(localizedContext.getString(R.string.ins_nome_squadra_2)) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            validaCampi(
                                localizedContext,
                                coroutineScope,
                                snackbarHostState,
                                type,
                                pitch,
                                newPitchNation,
                                newPitchProvince,
                                newPitchCity,
                                newPitchStreet,
                                newPitchHouseNumber,
                                newPitchName,
                                gameDate,
                                gameTime,
                                expiringDate,
                                expiringTime,
                                expectedAmount,
                                team1Name,
                                team2Name
                            ) {
                                if (pitch != null) {
                                    newPitchNation = ""
                                    newPitchProvince = ""
                                    newPitchCity = ""
                                    newPitchStreet = ""
                                    newPitchHouseNumber = ""
                                    newPitchName = ""
                                }
                                coroutineScope.launch {
                                    currentUser?.let {
                                        isLoading = true
                                        val result = insertNewMatch(
                                            context = context,
                                            type = type,
                                            existingPitch = pitch,
                                            newPitchNation = newPitchNation,
                                            newPitchProvince = newPitchProvince,
                                            newPitchCity = newPitchCity.trim(),
                                            newPitchStreet = newPitchStreet.trim(),
                                            newPitchHouseNumber = newPitchHouseNumber.trim(),
                                            newPitchName = newPitchName.trim(),
                                            gameDate = gameDate,
                                            gameTime = gameTime,
                                            expiringDate = expiringDate,
                                            expiringTime = expiringTime,
                                            expectedAmount = expectedAmount.trim(),
                                            team1Name = team1Name.trim(),
                                            team2Name = team2Name.trim(),
                                            organizer = currentUser!!
                                        )
                                        if (result.isSuccess) {
                                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.creazione_completata))
                                            navController.navigate(NavigationRoute.Home)
                                        } else {
                                            isLoading = false
                                            snackbarHostState.showSnackbar("${localizedContext.getString(R.string.errore_registrazione)}: ${result.exceptionOrNull()?.message}")
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.width(150.dp).height(42.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            localizedContext.getString(R.string.crea),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Button(
                        onClick = {
                            navController.navigateUp()
                        },
                        modifier = Modifier.width(150.dp).height(42.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red,
                            contentColor = White
                        )
                    ) {
                        Text(
                            localizedContext.getString(R.string.annulla),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun validaCampi(
    localizedContext: Context,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    type: String,
    pitch: CampoSportivo?,
    newPitchNation: String,
    newPitchProvince: String,
    newPitchCity: String,
    newPitchStreet: String,
    newPitchHouseNumber: String,
    newPitchName: String,
    gameDate: String,
    gameTime: String,
    expiringDate: String,
    expiringTime: String,
    amount: String,
    team1: String,
    team2: String,
    onSuccess: () -> Unit
) {
    scope.launch {
        try {
            // 1. Campi vuoti
            val campi = listOf(
                localizedContext.getString(R.string.tipo_partita) to type,
                localizedContext.getString(R.string.data) to gameDate,
                localizedContext.getString(R.string.ore) to gameTime,
                localizedContext.getString(R.string.data_scadenza) to expiringDate,
                localizedContext.getString(R.string.orario_scadenza) to expiringTime,
                localizedContext.getString(R.string.importo) to amount,
                localizedContext.getString(R.string.nome_squadra_1) to team1,
                localizedContext.getString(R.string.nome_squadra_2) to team2
            )
            campi.forEach { (nomeCampo, valore) ->
                if (valore.isBlank()) {
                    val message =
                        localizedContext.getString(R.string.campo_non_vuoto, nomeCampo)
                    throw Exception(message)
                }
            }

            // 2. Campo sportivo selezionato
            val campiNuovoLuogo = listOf(
                localizedContext.getString(R.string.stato) to newPitchNation,
                localizedContext.getString(R.string.provincia) to newPitchProvince,
                localizedContext.getString(R.string.citta) to newPitchCity,
                localizedContext.getString(R.string.via) to newPitchStreet,
                localizedContext.getString(R.string.civico) to newPitchHouseNumber,
                localizedContext.getString(R.string.campo_sportivo) to newPitchName,
            )
            campiNuovoLuogo.forEach { (nomeCampo, valore) ->
                if (valore.isBlank() && pitch == null) {
                    val message =
                        localizedContext.getString(R.string.campo_non_vuoto, nomeCampo)
                    throw Exception(message)
                }
            }

            // 3. Se si sta inserendo un nuovo campo sportivo, la città, la via e il numero civico devono essere validi
            if (pitch == null) {
                val noNumeriRegex = Regex(".*\\d.*")
                if (noNumeriRegex.containsMatchIn(newPitchCity)) throw Exception(
                    localizedContext.getString(
                        R.string.citta_con_numeri
                    )
                )
                if (noNumeriRegex.containsMatchIn(newPitchStreet)) throw Exception(
                    localizedContext.getString(
                        R.string.via_con_numeri
                    )
                )
                val civicoInt = newPitchHouseNumber.toIntOrNull() ?: throw Exception(localizedContext.getString(R.string.civico_non_numero))
                if (civicoInt !in 1..100000) throw Exception(localizedContext.getString(R.string.civico_fuori_range))
            }

            // 4. Data di gioco > data di oggi
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(gameDate, formatter)
            if (!date.isAfter(LocalDate.now())) throw Exception(localizedContext.getString(R.string.data_non_valida))

            // 5. Data scadenza iscrizione < data di gioco
            val expDate = LocalDate.parse(expiringDate, formatter)
            if (expDate.isAfter(date)) throw Exception(localizedContext.getString(R.string.data_iscrizione_non_valida))

            // 6. Se le due date coincidono, orario scadenza < orario di gioco
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val hour = LocalTime.parse(gameTime, timeFormatter)
            val expHour = LocalTime.parse(expiringTime, timeFormatter)
            if (expDate.isEqual(date) && !expHour.isBefore(hour)) throw Exception(localizedContext.getString(R.string.orario_scadenza_non_valido))

            // 7. Valore dell'importo valido
            val expAmount = if (amount.contains(",")) amount.replace(",", ".") else amount
            if (expAmount.toDoubleOrNull() == null) throw Exception(localizedContext.getString(R.string.importo_non_valido))

            // Tutto ok
            onSuccess()
        } catch (e: Exception) {
            snackbarHostState.showSnackbar(
                e.message ?: localizedContext.getString(R.string.errore_validazione))
        }
    }
}