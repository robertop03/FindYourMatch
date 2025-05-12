package com.example.findyourmatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.findyourmatch.ui.screens.CambiaPassword
import com.example.findyourmatch.ui.screens.CambiaPasswordDeepLink

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estrai il token dal deep link: findyourmatch://password-reset#access_token=...
        val token = intent?.data?.fragment
            ?.split("&")
            ?.find { it.startsWith("access_token=") }
            ?.substringAfter("=")

        setContent {
            val navController = rememberNavController()

            // Naviga alla schermata CambiaPassword con il token (se presente)
            if (!token.isNullOrEmpty()) {
                CambiaPasswordDeepLink(navController, token)
            } else {
                // fallback in caso di token mancante
                CambiaPassword(navController)
            }
        }
    }
}