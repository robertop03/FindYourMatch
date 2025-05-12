package com.example.findyourmatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
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
    @Serializable
    data object Login : NavigationRoute
    @Serializable
    data object CreateAccount : NavigationRoute
    @Serializable
    data object RestorePassword : NavigationRoute
    @Serializable
    data class PasswordResetDeepLink(val token: String) : NavigationRoute
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
        composable<NavigationRoute.Login> {
            Login(navController)
        }
        composable<NavigationRoute.CreateAccount> {
            CreaAccount(navController)
        }
        composable<NavigationRoute.RestorePassword> {
            RecuperaPassword(navController)
        }
        composable(
            route = "password-reset?access_token={access_token}",
            arguments = listOf(
                navArgument("access_token") { nullable = false }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "findyourmatch://password-reset?access_token={access_token}"
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("access_token") ?: ""
            CambiaPasswordDeepLink(navController, token)
        }
    }
}
