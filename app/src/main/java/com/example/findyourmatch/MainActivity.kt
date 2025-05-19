package com.example.findyourmatch

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.viewmodel.SessionViewModelFactory
import com.example.findyourmatch.navigation.NavGraph
import com.example.findyourmatch.ui.screens.CustomTopAppBar
import com.example.findyourmatch.ui.screens.Footer
import com.example.findyourmatch.ui.theme.FindYourMatchTheme
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindYourMatchTheme(dynamicColor = false) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val sessionViewModel: SessionViewModel = viewModel(
                        factory = SessionViewModelFactory(context.applicationContext as Application)
                    )
                val notificheViewModel: NotificheViewModel = viewModel(
                    factory = NotificheViewModelFactory(application)
                )
                    Scaffold(
                        topBar = { CustomTopAppBar(navController) },
                        bottomBar = { Footer(navController, sessionViewModel, notificheViewModel) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavGraph(navController, sessionViewModel, Modifier.padding(innerPadding), activity = this, notificheViewModel)
                    }
                }
        }
    }
}

