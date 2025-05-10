package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun CambiaPassword(navController: NavHostController) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Indietro",
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = "Cambia password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(Modifier.height(24.dp))

        // Campo: Nuova password
        Text(
            text = buildAnnotatedString {
                append("Inserisci nuova password")
                withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = { Text("deve essere di 8 caratteri") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Mostra/Nascondi password")
                }
            },
            modifier = Modifier.width(330.dp).align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        // Campo: Conferma password
        Text(
            text = buildAnnotatedString {
                append("Conferma password")
                withStyle(style = SpanStyle(color = Color.Red)) { append("*") }
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally).width(330.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("deve essere di 8 caratteri") },
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = icon, contentDescription = "Mostra/Nascondi password")
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
                onClick = { /* Salva azione */ },
                modifier = Modifier
                    .width(150.dp)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Salva", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .width(150.dp)
                    .height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF88001B), // rosso scuro
                    contentColor = Color.White
                )
            ) {
                Text("Annulla", fontWeight = FontWeight.Bold)
            }
        }
    }
}