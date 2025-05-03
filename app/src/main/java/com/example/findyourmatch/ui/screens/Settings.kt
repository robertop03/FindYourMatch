package com.example.findyourmatch.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.navigation.NavigationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController) {
    val showBackButton = navController.previousBackStackEntry != null

    val languages = listOf("Italiano", "English")
    var selectedLanguage by remember { mutableStateOf(languages[0]) }
    var expanded by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var fingerprintEnabled by remember { mutableStateOf(true) }
    var maxDistance by remember { mutableFloatStateOf(50f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Top // evita spazio in fondo
    ) {
        // Titolo + Back
        if (showBackButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Indietro",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Impostazioni",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // SEZIONE: Preferenze
        Text("Preferenze", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLanguage,
                onValueChange = {},
                readOnly = true,
                label = null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
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

        Text("Distanza massima: ${maxDistance.toInt()} km", fontSize = 14.sp)
        Slider(
            value = maxDistance,
            onValueChange = { maxDistance = it },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        // SEZIONE: Privacy
        Text("Privacy", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifiche", fontSize = 14.sp)
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Impronta digitale", fontSize = 14.sp)
            Switch(
                checked = fingerprintEnabled,
                onCheckedChange = { fingerprintEnabled = it }
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        // SEZIONE: Sicurezza
        Text("Sicurezza", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Cambia password",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable { navController.navigate(NavigationRoute.ChangePassword) }
                .align(Alignment.Start)
        )

        Spacer(Modifier.height(64.dp))

        // Pulsanti azione
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* Salva */ },
                modifier = Modifier.width(150.dp).height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Salva", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier.width(150.dp).height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Annulla", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { /* Logout */ },
                modifier = Modifier
                    .width(330.dp)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
