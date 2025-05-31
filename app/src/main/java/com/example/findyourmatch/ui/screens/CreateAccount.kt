package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.data.remote.api.createHttpClient
import com.example.findyourmatch.data.remote.api.fetchEUCountries
import com.example.findyourmatch.data.remote.api.fetchProvincesByCountry
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import com.example.findyourmatch.data.user.registraUtenteSupabase
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreaAccount(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermaPassword by remember { mutableStateOf("") }
    var confermaVisible by remember { mutableStateOf(false) }
    var dataNascita by remember { mutableStateOf("") }
    var sesso by remember { mutableStateOf("") }
    var stato by remember { mutableStateOf("") }
    var citta by remember { mutableStateOf("") }
    var via by remember { mutableStateOf("") }
    var civico by remember { mutableStateOf("") }
    var accettoCondizioni by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var cellulare by remember { mutableStateOf("") }
    var prefissoExpanded by remember { mutableStateOf(false) }
    var prefisso by remember { mutableStateOf("+39") }
    val coroutineScope = rememberCoroutineScope()
    var stati by remember { mutableStateOf(listOf<String>()) }
    val httpClient = remember { createHttpClient() }
    var provinceList by remember { mutableStateOf(listOf<String>()) }
    var provincia by remember { mutableStateOf("") }
    var provinciaExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            TopBarWithBackButton(navController, localizedContext.getString(R.string.crea_account))


            @Composable
            fun campoObbligatorio(
                label: String,
                value: String,
                onValueChange: (String) -> Unit,
                placeholder: String = ""
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(label)
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
            }

            @Composable
            fun campoObbligatorioPasswords(
                label: String,
                value: String,
                onValueChange: (String) -> Unit,
                placeholder: String = ""
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(label)
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    visualTransformation = if (confermaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confermaVisible = !confermaVisible }) {
                            Icon(
                                if (confermaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    },
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
            }

            campoObbligatorio(
                localizedContext.getString(R.string.email),
                email,
                { email = it },
                localizedContext.getString(R.string.email_placeholder)
            )
            campoObbligatorio(
                localizedContext.getString(R.string.nome),
                nome,
                { nome = it },
                localizedContext.getString(R.string.nome_placeholder)
            )
            campoObbligatorio(
                localizedContext.getString(R.string.cognome),
                cognome,
                { cognome = it },
                localizedContext.getString(R.string.cognome_placeholder)
            )
            campoObbligatorioPasswords(
                localizedContext.getString(R.string.password),
                password,
                { password = it },
                localizedContext.getString(R.string.password_placeholder)
            )
            campoObbligatorioPasswords(
                localizedContext.getString(R.string.conferma_password),
                confermaPassword,
                { confermaPassword = it },
                localizedContext.getString(R.string.password_placeholder)
            )

            Spacer(Modifier.height(16.dp))


            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.data_di_nascita))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)

            )

            TextField(
                value = dataNascita,
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
                        dataNascita = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
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


            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.sesso))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    localizedContext.getString(R.string.maschio),
                    localizedContext.getString(R.string.femmina)
                ).forEach { label ->
                    Row(
                        modifier = Modifier
                            .clickable { sesso = label }
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sesso == label, onClick = { sesso = label })
                        Text(label, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            val prefissi = listOf("+43", "+32", "+359", "+357", "+385", "+45", "+372", "+358", "+33", "+49", "+30", "+353", "+39", "+371", "+370", "+352", "+356", "+31", "+48", "+351", "+420", "+40", "+421", "+386", "+34", "+46")
            // Numero di telefono con prefisso
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.cellulare))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = prefissoExpanded,
                    onExpandedChange = { prefissoExpanded = !prefissoExpanded },
                    modifier = Modifier.width(110.dp)
                ) {
                    OutlinedTextField(
                        value = prefisso,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prefissoExpanded) },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = prefissoExpanded,
                        onDismissRequest = { prefissoExpanded = false }
                    ) {
                        prefissi.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    prefisso = it
                                    prefissoExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = cellulare,
                    onValueChange = { cellulare = it },
                    placeholder = { Text(localizedContext.getString(R.string.cellulare_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Indirizzo
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.indirizzo))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)
            )

            // Dropdown Stato
            var statoExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statoExpanded,
                onExpandedChange = { statoExpanded = !statoExpanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                OutlinedTextField(
                    value = stato,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .width(330.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statoExpanded) },
                    placeholder = { Text(localizedContext.getString(R.string.stato), color = MaterialTheme.colorScheme.onSecondaryContainer) }
                )
                LaunchedEffect(Unit) {
                    stati = fetchEUCountries(httpClient)
                }
                ExposedDropdownMenu(
                    expanded = statoExpanded,
                    onDismissRequest = { statoExpanded = false }
                ) {
                    stati.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                stato = it
                                statoExpanded = false
                            }
                        )
                    }
                }
            }

            LaunchedEffect(stato) {
                if (stato.isNotBlank()) {
                    provinceList = fetchProvincesByCountry(httpClient, stato)
                    provincia = "" // reset della provincia selezionata
                }
            }

            Spacer(Modifier.height(16.dp))

            // Dropdown Provincia
            ExposedDropdownMenuBox(
                expanded = provinciaExpanded,
                onExpandedChange = { provinciaExpanded = !provinciaExpanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                OutlinedTextField(
                    value = provincia,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .width(330.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(provinciaExpanded) },
                    placeholder = { Text(localizedContext.getString(R.string.provincia))}
                )
                ExposedDropdownMenu(
                    expanded = provinciaExpanded,
                    onDismissRequest = { provinciaExpanded = false }
                ) {
                    provinceList.forEach {
                        DropdownMenuItem(
                            text = { Text(it, color = MaterialTheme.colorScheme.onSecondaryContainer) },
                            onClick = {
                                provincia = it
                                provinciaExpanded = false
                            }
                        )
                    }
                }
            }


            Spacer(Modifier.height(8.dp))

            // Campi con solo placeholder
            val campiIndirizzo: List<Triple<String, String, (String) -> Unit>> = listOf(
                Triple(
                    localizedContext.getString(R.string.citta),
                    citta
                ) { nuovo: String -> citta = nuovo },
                Triple(
                    localizedContext.getString(R.string.via),
                    via
                ) { nuovo: String -> via = nuovo },
                Triple(
                    localizedContext.getString(R.string.civico),
                    civico
                ) { nuovo: String -> civico = nuovo }
            )

            campiIndirizzo.forEach { (placeholder, value, onChange) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onChange,
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    modifier = Modifier
                        .width(330.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                )
            }


            // Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Checkbox(checked = accettoCondizioni, onCheckedChange = { accettoCondizioni = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append(localizedContext.getString(R.string.termini_privacy))
                        withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                    },
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            fun validaCampi(
                scope: CoroutineScope,
                snackbarHostState: SnackbarHostState,
                email: String,
                nome: String,
                cognome: String,
                password: String,
                confermaPassword: String,
                dataNascita: String,
                stato: String,
                provincia: String,
                citta: String,
                via: String,
                civico: String,
                accettoCondizioni: Boolean,
                onSuccess: () -> Unit
            ) {
                scope.launch {
                    try {
                        // 1. Campi vuoti
                        val campi = listOf(
                            localizedContext.getString(R.string.email) to email,
                            localizedContext.getString(R.string.nome) to nome,
                            localizedContext.getString(R.string.cognome) to cognome,
                            localizedContext.getString(R.string.password) to password,
                            localizedContext.getString(R.string.conferma_password) to confermaPassword,
                            localizedContext.getString(R.string.data_di_nascita) to dataNascita,
                            localizedContext.getString(R.string.cellulare) to cellulare,
                            localizedContext.getString(R.string.stato) to stato,
                            localizedContext.getString(R.string.provincia) to provincia,
                            localizedContext.getString(R.string.citta) to citta,
                            localizedContext.getString(R.string.via) to via,
                            localizedContext.getString(R.string.civico) to civico
                        )
                        campi.forEach { (nomeCampo, valore) ->
                            if (valore.isBlank()) {
                                val message =
                                    localizedContext.getString(R.string.campo_non_vuoto, nomeCampo)
                                throw Exception(message)
                            }
                        }

                        // 2. Password
                        if (password != confermaPassword) throw Exception(
                            localizedContext.getString(
                                R.string.password_non_coincide
                            )
                        )
                        if (password.length < 8) throw Exception(localizedContext.getString(R.string.password_troppo_corta))

                        // 3. Email
                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                        if (!emailRegex.matches(email)) throw Exception(localizedContext.getString(R.string.email_non_valida))

                        // 3.1 Età minima → almeno 14 anni eta_minima
                        try {
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val dataNascitaParsed = java.time.LocalDate.parse(dataNascita, formatter)
                            val oggi = java.time.LocalDate.now()
                            val anni = java.time.Period.between(dataNascitaParsed, oggi).years
                            if (anni < 14) throw Exception(localizedContext.getString(R.string.eta_minima))
                        } catch (e: Exception) {
                            throw Exception(localizedContext.getString(R.string.errore_eta))
                        }

                        // 4. Nome, Cognome, Via, Città → nessun numero
                        val noNumeriRegex = Regex(".*\\d.*")
                        if (noNumeriRegex.containsMatchIn(nome)) throw Exception(
                            localizedContext.getString(
                                R.string.nome_con_numeri
                            )
                        )
                        if (noNumeriRegex.containsMatchIn(cognome)) throw Exception(
                            localizedContext.getString(
                                R.string.cognome_con_numeri
                            )
                        )
                        if (noNumeriRegex.containsMatchIn(via)) throw Exception(
                            localizedContext.getString(
                                R.string.via_con_numeri
                            )
                        )
                        if (noNumeriRegex.containsMatchIn(citta)) throw Exception(
                            localizedContext.getString(
                                R.string.citta_con_numeri
                            )
                        )

                        // 5. Cellulare → numero, con minimo di 8 e un max di 15 cifre
                        if (!cellulare.matches(Regex("^\\d+$"))) throw Exception(
                            localizedContext.getString(
                                R.string.numero_telefono_non_valido
                            )
                        )
                        if (cellulare.length !in 8..15) throw Exception(localizedContext.getString(R.string.numero_telefono_lunghezza))

                        // 6. Civico → numero tra 1 e 100000
                        val civicoInt = civico.toIntOrNull()
                            ?: throw Exception(localizedContext.getString(R.string.civico_non_numero))
                        if (civicoInt !in 1..100000) throw Exception(localizedContext.getString(R.string.civico_fuori_range))

                        // 7. Checkbox condizioni
                        if (!accettoCondizioni) throw Exception(localizedContext.getString(R.string.checkbox_non_accettata))

                        // Tutto ok
                        onSuccess()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            e.message ?: localizedContext.getString(R.string.errore_validazione))
                    }
                }
            }

            // Crea account
            Button(
                onClick = {
                    validaCampi(
                        scope = coroutineScope,
                        snackbarHostState = snackbarHostState,
                        email = email,
                        nome = nome,
                        cognome = cognome,
                        password = password,
                        confermaPassword = confermaPassword,
                        dataNascita = dataNascita,
                        stato = stato,
                        provincia = provincia,
                        citta = citta,
                        via = via,
                        civico = civico,
                        accettoCondizioni = accettoCondizioni
                    ) {
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(100)
                            val result = registraUtenteSupabase(
                                context = context,
                                email = email.trim(),
                                password = password.trim(),
                                nome = nome.trim(),
                                cognome = cognome.trim(),
                                dataNascita = dataNascita,
                                sesso = sesso,
                                telefono = prefisso + cellulare.trim(),
                                stato = stato.trim(),
                                provincia = provincia.trim(),
                                citta = citta.trim(),
                                via = via.trim(),
                                civico = civico.trim()
                            )

                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.registrazione_completata))
                                navController.navigate(NavigationRoute.Home)
                            } else {
                                snackbarHostState.showSnackbar("${localizedContext.getString(R.string.errore_registrazione)}: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    localizedContext.getString(R.string.crea_account),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Login link
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(localizedContext.getString(R.string.gia_account) + " ", color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    text = localizedContext.getString(R.string.login),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(NavigationRoute.Login)
                    }
                )
            }
        }
    }
}