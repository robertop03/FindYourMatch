package com.example.findyourmatch.ui.screens

import android.app.Application
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.authenticateWithBiometrics
import com.example.findyourmatch.data.user.isBiometricAvailable
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.launch
import com.example.findyourmatch.data.user.loginSupabase
import androidx.compose.ui.Alignment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory


@Composable
fun Login(navController: NavHostController, sessionViewModel: SessionViewModel, activity: FragmentActivity) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    val notificheViewModel: NotificheViewModel = viewModel(
        factory = NotificheViewModelFactory(context.applicationContext as Application)
    )

    val fingerprintEnabled by userSettings.fingerprintEnabled.collectAsState(initial = true)


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            val showBackButton = navController.previousBackStackEntry != null
            if (showBackButton) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = localizedContext.getString(R.string.indietro),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = localizedContext.getString(R.string.login),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.email))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(localizedContext.getString(R.string.email_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .width(330.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.password))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                modifier = Modifier
                    .width(330.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            fun loginUtente(
                email: String,
                password: String
            ) {
                coroutineScope.launch {
                    try {
                        // 1. Validazione campi
                        val campi = listOf(
                            localizedContext.getString(R.string.email) to email,
                            localizedContext.getString(R.string.password) to password)
                        campi.forEach { (nomeCampo, valore) ->
                            if (valore.isBlank()) throw Exception(
                                localizedContext.getString(
                                    R.string.campo_non_vuoto,
                                    nomeCampo
                                )
                            )
                        }

                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                        if (!emailRegex.matches(email)) throw Exception(localizedContext.getString(R.string.email_non_valida))

                        // 2. Chiamata alla loginSupabase
                        val result = loginSupabase(context, email, password, sessionViewModel)


                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.login_successo))
                            notificheViewModel.ricaricaNotifiche()
                            navController.navigate(NavigationRoute.Profile) {
                                popUpTo(NavigationRoute.Login) { inclusive = true }
                            }
                        } else {
                            val ex = result.exceptionOrNull()
                            throw ex ?: Exception(localizedContext.getString(R.string.email_non_valida))
                        }

                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            e.message ?: localizedContext.getString(R.string.login_errore))
                    }
                }
            }

            if (fingerprintEnabled && isBiometricAvailable(context)) {
                Button(
                    onClick = {
                        authenticateWithBiometrics(
                            activity = activity,
                            onSuccess = {
                                coroutineScope.launch {

                                    val accessToken = SessionManager.getAccessToken(context)
                                    val refreshToken = SessionManager.getRefreshToken(context)

                                    if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                                        sessionViewModel.updateLoginStatus(true)

                                        navController.navigate(NavigationRoute.Profile) {
                                            popUpTo(NavigationRoute.Login) { inclusive = true }
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar(localizedContext.getString(R.string.login_errore))
                                    }
                                }
                            },
                            onError = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(it)
                                }
                            },
                            localizedContext
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(330.dp)
                        .height(50.dp)
                ) {
                    Text(
                        localizedContext.getString(R.string.accedi_con_impronta),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    loginUtente(email.trim(), password.trim())
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(330.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(localizedContext.getString(R.string.accedi), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = localizedContext.getString(R.string.password_dimenticata),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { navController.navigate(NavigationRoute.RestorePassword) },
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(105.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(localizedContext.getString(R.string.no_account))
                Text(
                    text = localizedContext.getString(R.string.crea_account),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable {
                        navController.navigate(NavigationRoute.CreateAccount)
                    },
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}
