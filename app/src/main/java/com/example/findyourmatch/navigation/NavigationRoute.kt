package com.example.findyourmatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable
    data object Home : NavigationRoute
    @Serializable
    data object Settings : NavigationRoute
    @Serializable
    data object Profile : NavigationRoute
    @Serializable
    data object Notifiche : NavigationRoute
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
        composable<NavigationRoute.Notifiche> {
            Notifiche(navController)
        }
    }
}
