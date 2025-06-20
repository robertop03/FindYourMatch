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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.inviaEmailRecuperoPassword
import com.example.findyourmatch.ui.theme.White
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@Composable
fun RecuperaPassword(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val showBackButton = navController.previousBackStackEntry != null
    val context = LocalContext.current
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
                title = localizedContext.getString(R.string.recupera_pw),
                showBackButton = showBackButton
            )

            Spacer(Modifier.height(50.dp))
            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.inserisci_email))
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
                    .padding(start = 16.dp)
            )

            Spacer(Modifier.height(34.dp))

            Text(
                text = buildAnnotatedString {
                    append(localizedContext.getString(R.string.email))
                    withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
                },
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
            )


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(localizedContext.getString(R.string.email_placeholder), color = MaterialTheme.colorScheme.onSecondaryContainer) },
                singleLine = true,
                modifier = Modifier
                    .width(330.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(105.dp))

            // Pulsante invia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (email.isBlank()) {
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.email_valida))
                                return@launch
                            }

                            val result = inviaEmailRecuperoPassword(email)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.controlla_email))
                            } else {
                                snackbarHostState.showSnackbar("${localizedContext.getString(R.string.errore_registrazione)}: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = White
                    )
                ) {
                    Text(localizedContext.getString(R.string.invia), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
