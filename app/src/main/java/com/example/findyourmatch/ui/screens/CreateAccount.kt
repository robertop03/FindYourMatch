package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.findyourmatch.data.remote.createHttpClient
import com.example.findyourmatch.data.remote.fetchEUCountries
import com.example.findyourmatch.data.remote.fetchProvincesByCountry
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreaAccount(navController: NavHostController) {
    val showBackButton = navController.previousBackStackEntry != null
    var email by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermaPassword by remember { mutableStateOf("") }
    var confermaVisible by remember { mutableStateOf(false) }
    var dataNascita by remember { mutableStateOf("") }
    var sesso by remember { mutableStateOf("Maschio") }
    var stato by remember { mutableStateOf("Stato") }
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
            if (showBackButton) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Indietro",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Crea account",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

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

            campoObbligatorio("Email", email, { email = it }, "esempio@gmail.com")
            campoObbligatorio("Nome", nome, { nome = it }, "Inserisci il tuo nome")
            campoObbligatorio("Cognome", cognome, { cognome = it }, "Inserisci il tuo cognome")
            campoObbligatorioPasswords(
                "Password",
                password,
                { password = it },
                "almeno 8 caratteri"
            )
            campoObbligatorioPasswords(
                "Conferma password",
                confermaPassword,
                { confermaPassword = it },
                "almeno 8 caratteri"
            )
            campoObbligatorio("Data di nascita", dataNascita, { dataNascita = it }, "dd/mm/yyyy")

            Text(
                text = buildAnnotatedString {
                    append("Sesso")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
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
                listOf("Maschio", "Femmina").forEach { label ->
                    Row(
                        modifier = Modifier
                            .clickable { sesso = label }
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = sesso == label, onClick = { sesso = label })
                        Text(label)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            val prefissi = listOf("+43", "+32", "+359", "+357", "+385", "+45", "+372", "+358", "+33", "+49", "+30", "+353", "+39", "+371", "+370", "+352", "+356", "+31", "+48", "+351", "+420", "+40", "+421", "+386", "+34", "+46")
            // Numero di telefono con prefisso
            Text(
                text = buildAnnotatedString {
                    append("Cellulare")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
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
                    modifier = Modifier.width(98.dp)
                ) {
                    OutlinedTextField(
                        value = prefisso,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(),
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
                    placeholder = { Text("1234567890") },
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Indirizzo
            Text(
                text = buildAnnotatedString {
                    append("Indirizzo")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
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
                        .menuAnchor()
                        .width(330.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statoExpanded) },
                    placeholder = { Text("Stato") }
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

            Spacer(Modifier.height(12.dp))

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
                        .menuAnchor()
                        .width(330.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(provinciaExpanded) },
                    placeholder = { Text("Provincia") }
                )
                ExposedDropdownMenu(
                    expanded = provinciaExpanded,
                    onDismissRequest = { provinciaExpanded = false }
                ) {
                    provinceList.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                provincia = it
                                provinciaExpanded = false
                            }
                        )
                    }
                }
            }


            Spacer(Modifier.height(16.dp))

            // Campi con solo placeholder
            val campiIndirizzo = listOf(
                Triple("Città", citta, { nuovo: String -> citta = nuovo }),
                Triple("Via", via, { nuovo: String -> via = nuovo }),
                Triple("Civico", civico, { nuovo: String -> civico = nuovo })
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
                        .padding(vertical = 4.dp)
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
                    "Accetto i termini e l'informativa sulla privacy.",
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
                            "Email" to email,
                            "Nome" to nome,
                            "Cognome" to cognome,
                            "Password" to password,
                            "Conferma Password" to confermaPassword,
                            "Data di nascita" to dataNascita,
                            "Cellulare" to cellulare,
                            "Stato" to stato,
                            "Provincia" to provincia,
                            "Città" to citta,
                            "Via" to via,
                            "Civico" to civico
                        )
                        campi.forEach { (nomeCampo, valore) ->
                            if (valore.isBlank()) throw Exception("Il campo \"$nomeCampo\" non può essere vuoto.")
                        }

                        // 2. Password
                        if (password != confermaPassword) throw Exception("Le password non coincidono.")
                        if (password.length < 8) throw Exception("La password deve contenere almeno 8 caratteri.")

                        // 3. Email
                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                        if (!emailRegex.matches(email)) throw Exception("L'indirizzo email non è valido.")

                        // 4. Nome, Cognome, Via, Città → nessun numero
                        val noNumeriRegex = Regex(".*\\d.*")
                        if (noNumeriRegex.containsMatchIn(nome)) throw Exception("Il nome non può contenere numeri.")
                        if (noNumeriRegex.containsMatchIn(cognome)) throw Exception("Il cognome non può contenere numeri.")
                        if (noNumeriRegex.containsMatchIn(via)) throw Exception("La via non può contenere numeri.")
                        if (noNumeriRegex.containsMatchIn(citta)) throw Exception("La città non può contenere numeri.")

                        // 5. Cellulare → numero, con minimo di 8 e un max di 15 cifre
                        if (!cellulare.matches(Regex("^\\d+$"))) throw Exception("Il numero deve contenere solo cifre.")
                        if (cellulare.length !in 8..15) throw Exception("Il numero deve contenere da 8 a 15 cifre.")

                        // 6. Civico → numero tra 1 e 100000
                        val civicoInt = civico.toIntOrNull()
                            ?: throw Exception("Il civico deve essere un numero.")
                        if (civicoInt !in 1..100000) throw Exception("Il civico deve essere compreso tra 1 e 100000.")

                        // 7. Checkbox condizioni
                        if (!accettoCondizioni) throw Exception("Devi accettare i termini e la privacy.")

                        // Tutto ok
                        onSuccess()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Errore di validazione.")
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
                        // ✅ Solo se tutti i controlli passano
                        // Esegui registrazione qui
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Crea account", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Login link
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Hai già un account? ")
                Text(
                    text = "Login",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(NavigationRoute.Login)
                    }
                )
            }
        }
    }
}
