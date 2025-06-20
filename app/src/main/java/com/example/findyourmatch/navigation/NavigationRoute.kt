package com.example.findyourmatch.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.viewmodel.SessionViewModel
import com.example.findyourmatch.ui.screens.*
import com.example.findyourmatch.viewmodel.HomeViewModel
import com.example.findyourmatch.viewmodel.MatchViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.ProfileViewModel
import com.example.findyourmatch.viewmodel.ReviewsViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
    data object Notice : NavigationRoute
    @Serializable
    data object RestorePassword : NavigationRoute
    @Serializable
    data object Rewards : NavigationRoute
    @Serializable
    data object MatchStats : NavigationRoute
    @Serializable
    data object InsertMatchDetails : NavigationRoute
    @Serializable
    data object PlayedGames : NavigationRoute
    @Serializable
    data object GamesToPlay : NavigationRoute
    @Serializable
    data object EditProfile : NavigationRoute
    @Serializable
    data object PersonalStats : NavigationRoute
}

@Composable
fun NavGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    modifier: Modifier = Modifier,
    activity: FragmentActivity,
    notificheViewModel: NotificheViewModel,
    profileViewModel: ProfileViewModel,
    homeViewModel: HomeViewModel,
    reviewsViewModel: ReviewsViewModel,
    matchViewModel: MatchViewModel
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Home,
        modifier = modifier
    ) {
        composable<NavigationRoute.Home> {
            Home(navController, sessionViewModel, homeViewModel)
        }
        composable<NavigationRoute.Settings> {
            Settings(navController, sessionViewModel, homeViewModel)
        }
        composable<NavigationRoute.Profile> {
            Profile(navController, profileViewModel)
        }
        composable<NavigationRoute.Notifications> {
            Notifiche(navController, notificheViewModel)
        }
        composable(
            route = "${NavigationRoute.Notice}/{notificaJson}",
            arguments = listOf(navArgument("notificaJson") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "findyourmatch://notice/{notificaJson}"
                }
            )
        ) { backStackEntry ->
            val notificaJson = backStackEntry.arguments?.getString("notificaJson") ?: ""
            val notifica = Json.decodeFromString<Notifica>(
                URLDecoder.decode(notificaJson, StandardCharsets.UTF_8.toString())
            )
            Notifica(notifica = notifica, navController = navController)
        }

        composable<NavigationRoute.CreateMatch> {
            CreaPartita(navController)
        }
        composable<NavigationRoute.ChangePassword> {
            CambiaPassword(navController, sessionViewModel)
        }
        composable<NavigationRoute.Login> {
            Login(navController, sessionViewModel, activity)
        }
        composable<NavigationRoute.CreateAccount> {
            CreaAccount(navController)
        }
        composable<NavigationRoute.RestorePassword> {
            RecuperaPassword(navController)
        }
        composable<NavigationRoute.Rewards> {
            Rewards(navController)
        }
        composable<NavigationRoute.PlayedGames> {
            ListaPartite(navController, profileViewModel, true)
        }
        composable<NavigationRoute.GamesToPlay> {
            ListaPartite(navController, profileViewModel, false)
        }
        composable(
            route = "partita/{idPartita}",
            arguments = listOf(navArgument("idPartita") { type = NavType.IntType })
        ) { backStackEntry ->
            val idPartita = backStackEntry.arguments?.getInt("idPartita") ?: -1
            Partita(navController = navController, idPartita = idPartita, matchViewModel)
        }

        composable(
            route = "reviews?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            Recensioni(navController, email, reviewsViewModel, profileViewModel)
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
            CambiaPasswordDeepLink(navController, token, sessionViewModel)
        }
        composable<NavigationRoute.EditProfile> {
            ModificaProfilo(navController, profileViewModel)
        }
        composable<NavigationRoute.PersonalStats> {
            StatistichePersonali(navController, profileViewModel)
        }
        composable<NavigationRoute.MatchStats> {
            StatistichePartita(navController, matchViewModel)
        }
        composable<NavigationRoute.InsertMatchDetails> {
            InserisciDettagli(navController, matchViewModel)
        }
    }
}
