package com.example.findyourmatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

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
                Scaffold(
                    topBar = { CustomTopAppBar(navController) },
                    bottomBar = {Footer(navController)},
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavGraph(navController, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

