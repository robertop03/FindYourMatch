package com.example.findyourmatch

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.viewmodel.SessionViewModelFactory
import com.example.findyourmatch.ui.screens.CambiaPassword
import com.example.findyourmatch.ui.screens.CambiaPasswordDeepLink

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Estrae il token dal deep link: findyourmatch://password-reset#access_token=...
        val token = intent?.data?.fragment
            ?.split("&")
            ?.find { it.startsWith("access_token=") }
            ?.substringAfter("=")

        setContent {
            val navController = rememberNavController()

            val context = LocalContext.current

            val sessionViewModel: SessionViewModel = viewModel(
                factory = SessionViewModelFactory(context.applicationContext as Application)
            )

            // Naviga alla schermata CambiaPassword con il token (se presente)
            if (!token.isNullOrEmpty()) {
                CambiaPasswordDeepLink(navController, token, sessionViewModel)
            } else {
                // fallback in caso di token mancante
                CambiaPassword(navController, sessionViewModel)
            }
        }
    }
}