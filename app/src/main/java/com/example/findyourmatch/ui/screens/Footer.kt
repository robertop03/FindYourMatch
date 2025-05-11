package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.findyourmatch.navigation.NavigationRoute
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.launch

@Composable
fun Footer(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isHomeSelected = currentRoute == NavigationRoute.Home::class.qualifiedName
    val isCreateMatchSelected = currentRoute == NavigationRoute.CreateMatch::class.qualifiedName
    val isNotificationSelected = currentRoute == NavigationRoute.Notifications::class.qualifiedName
    val isProfileSelected = currentRoute == NavigationRoute.Profile::class.qualifiedName
    val isLoginSelected = currentRoute == NavigationRoute.Login::class.qualifiedName
    val isCreateAccountSelected = currentRoute == NavigationRoute.CreateAccount::class.qualifiedName
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigate(NavigationRoute.Home) }) {
                Icon(
                    imageVector = if (isHomeSelected)
                        Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(45.dp)
                )
            }

            IconButton(onClick = { navController.navigate(NavigationRoute.CreateMatch) }) {
                Icon(
                    imageVector = if (isCreateMatchSelected)
                        Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                    contentDescription = "Add",
                    modifier = Modifier.size(45.dp)
                )
            }

            IconButton(onClick = {
                coroutineScope.launch {
                    if (SessionManager.isLoggedIn(context)) {
                        navController.navigate(NavigationRoute.Profile)
                    } else {
                        navController.navigate(NavigationRoute.Login)
                    }
                }
            }) {
                Icon(
                    imageVector = if (isProfileSelected || isLoginSelected || isCreateAccountSelected)
                        Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(45.dp)
                )
            }

            IconButton(onClick = { navController.navigate(NavigationRoute.Notifications) }) {
                Icon(
                    imageVector = if (isNotificationSelected)
                        Icons.Filled.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    modifier = Modifier.size(45.dp)
                )
            }
        }
    }
}