package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.Icons
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.cambiaPasswordUtente
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Green
import com.example.findyourmatch.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@Composable
fun CambiaPassword(navController: NavHostController, sessionViewModel: SessionViewModel) {
    val showBackButton = navController.previousBackStackEntry != null
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.impostazioni),
                showBackButton = showBackButton
            )

            Spacer(Modifier.height(24.dp))

            // Nuova password
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.inserisci_nuova_pw))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text(localizedContext.getString(R.string.password_placeholder), color = MaterialTheme.colorScheme.onSecondaryContainer) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = localizedContext.getString(R.string.mostra_nascondi_pw)
                        )
                    }
                },
                modifier = Modifier.width(330.dp).align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Conferma password
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.conferma_password))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text(localizedContext.getString(R.string.password_placeholder), color = MaterialTheme.colorScheme.onSecondaryContainer) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = localizedContext.getString(R.string.mostra_nascondi_pw)
                        )
                    }
                },
                modifier = Modifier.width(330.dp).align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(32.dp))

            // Pulsanti: Salva e Annulla
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (newPassword != confirmPassword) {
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.password_non_coincide))
                                return@launch
                            }

                            if (newPassword.length < 8) {
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.errore_pw))
                                return@launch
                            }

                            val result = cambiaPasswordUtente(context, newPassword)
                            if (result.isSuccess) {
                                SessionManager.logout(context, sessionViewModel)
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.aggiornamento_pw))
                                navController.navigate(NavigationRoute.Login)
                            } else {
                                snackbarHostState.showSnackbar("${localizedContext.getString(R.string.aggiornamento_pw)}: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = White
                    )
                ) {
                    Text(localizedContext.getString(R.string.salva), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .width(150.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = White
                    )
                ) {
                    Text(localizedContext.getString(R.string.annulla), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CambiaPasswordDeepLink(navController: NavHostController, token: String, sessionViewModel: SessionViewModel) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = localizedContext.getString(R.string.recupera_pw),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 16.dp, bottom = 24.dp)
            )

            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.inserisci_nuova_pw))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = { Text(localizedContext.getString(R.string.password_placeholder)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = localizedContext.getString(R.string.mostra_nascondi_pw)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green,
                    focusedLabelColor = Green,
                    cursorColor =Green
                ),
                modifier = Modifier.width(330.dp).align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // Conferma password
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.conferma_password))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text(localizedContext.getString(R.string.password_placeholder)) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = localizedContext.getString(R.string.mostra_nascondi_pw)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green,
                    focusedLabelColor = Green,
                    cursorColor = Green
                ),
                modifier = Modifier.width(330.dp).align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (newPassword != confirmPassword) {
                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.password_non_coincide))
                            return@launch
                        }
                        if (newPassword.length < 8) {
                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.password_placeholder))
                            return@launch
                        }

                        val result = aggiornaPasswordConToken(token, newPassword)
                        if (result.isSuccess) {
                            SessionManager.logout(context, sessionViewModel)
                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.aggiornamento_pw))
                            navController.navigate(NavigationRoute.Login)
                        } else {
                            snackbarHostState.showSnackbar("${localizedContext.getString(R.string.errore_registrazione)}: ${result.exceptionOrNull()?.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(localizedContext.getString(R.string.salva), fontWeight = FontWeight.Bold)
            }
        }
    }
}

suspend fun aggiornaPasswordConToken(token: String, newPassword: String): Result<Unit> = withContext(
    Dispatchers.IO) {
    try {
        val json = Json.encodeToString(mapOf("password" to newPassword))
        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://ugtxgylfzblkvudpnagi.supabase.co/auth/v1/user")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVndHhneWxmemJsa3Z1ZHBuYWdpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY4ODI4NTUsImV4cCI6MjA2MjQ1ODg1NX0.cc0z6qkcWktvnh83Um4imlCBSfPlh7TelMNFIhxmjm0")
            .addHeader("Content-Type", "application/json")
            .put(body)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("${response.code}: ${response.body?.string()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
