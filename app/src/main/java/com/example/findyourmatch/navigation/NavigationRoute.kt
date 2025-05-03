package com.example.findyourmatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.findyourmatch.ui.screens.*
import kotlinx.serialization.Serializable

sealed interface NavigationRoute {

    @Serializable
    data object Home : NavigationRoute
    @Serializable
    data object Settings : NavigationRoute
    @Serializable
    data object Profile : NavigationRoute
    @Serializable
    data object Notifications : NavigationRoute
    @Serializable
    data object CreateMatch : NavigationRoute
    @Serializable
    data object ChangePassword : NavigationRoute
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Home,
        modifier = modifier
    ) {
        composable<NavigationRoute.Home> {
            Home(navController)
        }
        composable<NavigationRoute.Settings> {
            Settings(navController)
        }
        composable<NavigationRoute.Profile> {
            Profile(navController)
        }
        composable<NavigationRoute.Notifications> {
            Notifiche(navController)
        }
        composable<NavigationRoute.CreateMatch> {
            CreaPartita(navController)
        }
        composable<NavigationRoute.ChangePassword> {
            CambiaPassword(navController)
        }
    }
}
