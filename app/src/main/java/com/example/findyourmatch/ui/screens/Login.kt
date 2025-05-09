package com.example.findyourmatch.ui.screens

import android.util.Log
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
import com.example.findyourmatch.data.PasswordUtils
import com.example.findyourmatch.data.database.AppDatabase
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun Login(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                            contentDescription = "Indietro",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "Login",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = buildAnnotatedString {
                    append("Email")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("esempio@gmail.com") },
                singleLine = true,
                modifier = Modifier
                    .width(330.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = buildAnnotatedString {
                    append("Password")
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("deve essere di 8 caratteri") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = "Mostra/Nascondi password")
                    }
                },
                modifier = Modifier
                    .width(330.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))

            fun validaCampiEUtenteEsistente(
                email: String,
                password: String,
                onSuccess: () -> Unit
            ) {
                coroutineScope.launch {
                    try {
                        val campi = listOf("Email" to email, "Password" to password)
                        campi.forEach { (nomeCampo, valore) ->
                            if (valore.isBlank()) throw Exception("Il campo \"$nomeCampo\" non può essere vuoto.")
                        }

                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
                        if (!emailRegex.matches(email)) throw Exception("L'indirizzo email non è valido.")

                        val db = AppDatabase.getInstance(context)
                        val utente = db.userDao().getByEmail(email)
                            ?: throw Exception("Email non registrata.")

                        val passwordValida = PasswordUtils.verifyPassword(password, utente.salt, utente.password)
                        if (!passwordValida) throw Exception("Password errata.")

                        // Tutto ok
                        onSuccess()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Errore di validazione.")
                    }
                }
            }

            Button(
                onClick = {
                    validaCampiEUtenteEsistente(
                        email = email.trim(),
                        password = password.trim(),
                        onSuccess = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Login effettuato con successo")
                                navController.navigate(NavigationRoute.Profile)
                            }
                        }
                    )
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
                Text("Accedi", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Password dimenticata? Recupera ora",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { /* navController.navigate(...) */ },
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(195.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Non hai un account? ")
                Text(
                    text = "Crea account",
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
