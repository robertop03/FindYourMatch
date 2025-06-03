package com.example.findyourmatch.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.findyourmatch.data.remote.api.createHttpClient
import com.example.findyourmatch.data.remote.api.fetchEUCountries
import com.example.findyourmatch.data.remote.api.fetchProvincesByCountry
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificaProfilo(navController: NavHostController, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    val httpClient = remember { createHttpClient() }
    val user by profileViewModel.user.collectAsState()
    val address by profileViewModel.userAddress.collectAsState()
    var email by remember { mutableStateOf(user!!.email) }
    var name by remember { mutableStateOf(user!!.nome) }
    var lastName by remember { mutableStateOf(user!!.cognome) }
    var nation by remember { mutableStateOf(address!!.stato) }
    var province by remember { mutableStateOf(address!!.provincia) }
    var city by remember { mutableStateOf(address!!.citta) }
    var street by remember { mutableStateOf(address!!.via) }
    var houseNumber by remember { mutableStateOf(address!!.civico) }

    var nationExpanded by remember { mutableStateOf(false) }
    var provinceExpanded by remember { mutableStateOf(false) }

    var euNations by remember { mutableStateOf(listOf<String>()) }
    var provinces by remember { mutableStateOf(listOf<String>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
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
                title = localizedContext.getString(R.string.btn_modifica),
                showBackButton = showBackButton
            )

            MandatoryField(
                localizedContext.getString(R.string.email),
                email,
                { email = it },
                localizedContext.getString(R.string.email_placeholder)
            )
            MandatoryField(
                localizedContext.getString(R.string.nome),
                name,
                { name = it },
                localizedContext.getString(R.string.nome_placeholder)
            )
            MandatoryField(
                localizedContext.getString(R.string.cognome),
                lastName,
                { lastName = it },
                localizedContext.getString(R.string.cognome_placeholder)
            )
            Column (
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column (
                    modifier = Modifier.width(330.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(localizedContext.getString(R.string.indirizzo))
                            withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                        },
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    ExposedDropdownMenuBox(
                        expanded = nationExpanded,
                        onExpandedChange = { nationExpanded = !nationExpanded },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        OutlinedTextField(
                            value = nation,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                                .width(330.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(nationExpanded) },
                            placeholder = { Text(localizedContext.getString(R.string.stato), color = MaterialTheme.colorScheme.onSecondaryContainer) }
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
                                        nation = it
                                        nationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LaunchedEffect(nation) {
                        if (nation.isNotBlank()) {
                            provinces = fetchProvincesByCountry(httpClient, nation)
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = provinceExpanded,
                        onExpandedChange = { provinceExpanded = !provinceExpanded },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        OutlinedTextField(
                            value = province,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                                .width(330.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(provinceExpanded) },
                            placeholder = { Text(localizedContext.getString(R.string.provincia))}
                        )
                        ExposedDropdownMenu(
                            expanded = provinceExpanded,
                            onDismissRequest = { provinceExpanded = false }
                        ) {
                            provinces.forEach {
                                DropdownMenuItem(
                                    text = { Text(it, color = MaterialTheme.colorScheme.onSecondaryContainer) },
                                    onClick = {
                                        province = it
                                        provinceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    val campiIndirizzo: List<Triple<String, String, (String) -> Unit>> = listOf(
                        Triple(
                            localizedContext.getString(R.string.citta),
                            city
                        ) { nuovo: String -> city = nuovo },
                        Triple(
                            localizedContext.getString(R.string.via),
                            street
                        ) { nuovo: String -> street = nuovo },
                        Triple(
                            localizedContext.getString(R.string.civico),
                            houseNumber
                        ) { nuovo: String -> houseNumber = nuovo }
                    )
                    Spacer(Modifier.height(8.dp))
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
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ){
                Button(
                    onClick = {
                    },
                    modifier = Modifier.width(150.dp).height(42.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(localizedContext.getString(R.string.salva),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold)
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
                        contentColor = White)
                ) {
                    Text(localizedContext.getString(R.string.annulla),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MandatoryField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Column (
            modifier = Modifier.width(330.dp),
            horizontalAlignment = Alignment.Start
        ){
            Text(
                text = buildAnnotatedString {
                    append(label)
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder) },
                singleLine = true,
                modifier = Modifier.width(330.dp)
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}