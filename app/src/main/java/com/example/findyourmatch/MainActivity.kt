package com.example.findyourmatch

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.findyourmatch.data.notifications.aggiornaTokenFCMUtenteSeDiverso
import com.example.findyourmatch.data.notifications.creaCanaleNotifiche
import com.example.findyourmatch.data.user.SessionManager
import com.example.findyourmatch.data.user.SessionManager.isLoggedInFlow
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.viewmodel.SessionViewModelFactory
import com.example.findyourmatch.navigation.NavGraph
import com.example.findyourmatch.ui.screens.CustomTopAppBar
import com.example.findyourmatch.ui.screens.Footer
import com.example.findyourmatch.ui.theme.FindYourMatchTheme
import com.example.findyourmatch.viewmodel.HomeViewModel
import com.example.findyourmatch.viewmodel.HomeViewModelFactory
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory
import com.example.findyourmatch.viewmodel.ProfileViewModel
import com.example.findyourmatch.viewmodel.ProfileViewModelFactory
import com.example.findyourmatch.viewmodel.ReviewsViewModel
import com.example.findyourmatch.viewmodel.ReviewsViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        creaCanaleNotifiche(this)
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
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(application)
                )
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(context.applicationContext as Application)
                )
                val reviewsViewModel: ReviewsViewModel = viewModel(
                    factory = ReviewsViewModelFactory(application)
                )

                LaunchedEffect(Unit) {
                    val isValid = SessionManager.isTokenStillValid(context) && isLoggedInFlow(context).first()
                    sessionViewModel.updateLoginStatus(context, isValid)

                    if (isValid) {
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
                }
                    Scaffold(
                        topBar = { CustomTopAppBar(navController) },
                        bottomBar = { Footer(navController, sessionViewModel, notificheViewModel) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavGraph(navController, sessionViewModel, Modifier.padding(innerPadding), activity = this, notificheViewModel, profileViewModel, homeViewModel, reviewsViewModel)
                    }
                }
        }
    }
}

