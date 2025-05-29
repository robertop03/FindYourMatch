package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.navigation.NavigationRoute
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import com.example.findyourmatch.R
import com.example.findyourmatch.data.notifications.aggiornaTokenFCMUtenteSeDiverso
import com.example.findyourmatch.data.notifications.impostaFcmTokenNull
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.data.user.getLoggedUserEmail
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController, sessionViewModel: SessionViewModel) {
    val showBackButton = navController.previousBackStackEntry != null

    val languages = listOf("it", "en")
    var expanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    val savedLanguage by userSettings.language.collectAsState(initial = "it")
    val savedNotificationsEnabled by userSettings.notificationsEnabled.collectAsState(initial = true)
    val savedFingerprintEnabled by userSettings.fingerprintEnabled.collectAsState(initial = true)
    val savedMaxDistance by userSettings.maxDistance.collectAsState(initial = 50f)

    var selectedLanguage by remember(savedLanguage) { mutableStateOf(savedLanguage) }
    var notificationsEnabled by remember(savedNotificationsEnabled) { mutableStateOf(savedNotificationsEnabled) }
    var fingerprintEnabled by remember(savedFingerprintEnabled) { mutableStateOf(savedFingerprintEnabled) }
    var maxDistance by remember(savedMaxDistance) { mutableFloatStateOf(savedMaxDistance) }

    var isLoggedIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLoggedIn = SessionManager.isLoggedIn(sessionViewModel)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.impostazioni),
                showBackButton = showBackButton
            )

            Spacer(Modifier.height(12.dp))

            Text(
                localizedContext.getString(R.string.preferenze),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        )
                        .fillMaxWidth()
                        .height(55.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                selectedLanguage = lang
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                localizedContext.getString(R.string.distanza_massima, maxDistance.toInt()),
                fontSize = 14.sp
            )

            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 1f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoggedIn) {
                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text("Privacy", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(localizedContext.getString(R.string.notifiche), fontSize = 14.sp)
                    Switch(checked = notificationsEnabled, onCheckedChange = {
                        notificationsEnabled = it
                        if(!notificationsEnabled){
                            // setto a null fcm_token se utente è loggato
                            if (isLoggedIn){
                                coroutineScope.launch {
                                    val email = getLoggedUserEmail(context)
                                    impostaFcmTokenNull(context, email!!)
                                }
                            }
                        }else{
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result

                                    CoroutineScope(Dispatchers.IO).launch {
                                        aggiornaTokenFCMUtenteSeDiverso(context = context, nuovoToken = token)
                                    }
                                } else {
                                    Log.e("FCM", "Errore ottenimento token", task.exception)
                                }
                            }
                        }
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(localizedContext.getString(R.string.impronta_digitale), fontSize = 14.sp)
                    Switch(checked = fingerprintEnabled, onCheckedChange = { fingerprintEnabled = it })
                }

                HorizontalDivider(Modifier.padding(vertical = 12.dp))

                Text(
                    localizedContext.getString(R.string.sicurezza),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = localizedContext.getString(R.string.cambia_pw),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { navController.navigate(NavigationRoute.ChangePassword) }
                        .align(Alignment.Start)
                )
            }

            Spacer(Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userSettings.saveSettings(
                                language = selectedLanguage,
                                notifications = notificationsEnabled,
                                fingerprint = fingerprintEnabled,
                                distance = maxDistance
                            )
                            snackbarHostState.showSnackbar(localizedContext.getString(R.string.impostazioni_salvate))
                        }
                    },
                    modifier = Modifier.width(150.dp).height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        localizedContext.getString(R.string.salva),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.width(150.dp).height(42.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        localizedContext.getString(R.string.annulla),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isLoggedIn) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.width(330.dp).height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(
                            localizedContext.getString(R.string.logout),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(localizedContext.getString(R.string.conferma_logout)) },
                text = { Text(localizedContext.getString(R.string.testo_conferma_logout)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            coroutineScope.launch {
                                SessionManager.logout(context, sessionViewModel)
                                navController.navigate(NavigationRoute.Login) {
                                    popUpTo(NavigationRoute.Profile) { inclusive = true }
                                }
                                snackbarHostState.showSnackbar(localizedContext.getString(R.string.logout_successo))
                            }
                        }
                    ) {
                        Text(localizedContext.getString(R.string.conferma))
                    }
                },
                dismissButton = {
                    Button(onClick = { showLogoutDialog = false }) {
                        Text(localizedContext.getString(R.string.annulla))
                    }
                }
            )
        }
    }
}
