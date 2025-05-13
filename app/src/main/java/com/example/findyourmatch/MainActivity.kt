package com.example.findyourmatch

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.findyourmatch.data.user.SessionViewModel
import com.example.findyourmatch.data.user.SessionViewModelFactory

import com.example.findyourmatch.navigation.NavGraph
import com.example.findyourmatch.ui.screens.CustomTopAppBar
import com.example.findyourmatch.ui.screens.Footer
import com.example.findyourmatch.ui.theme.FindYourMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindYourMatchTheme (dynamicColor = false) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val sessionViewModel: SessionViewModel = viewModel(
                    factory = SessionViewModelFactory(context.applicationContext as Application)
                )
                Scaffold(
                    topBar = { CustomTopAppBar(navController, sessionViewModel) },
                    bottomBar = {Footer(navController)},
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavGraph(navController, sessionViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

