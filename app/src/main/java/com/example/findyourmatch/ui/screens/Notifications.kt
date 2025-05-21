package com.example.findyourmatch.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.data.notifications.segnaNotificaComeLetta
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Blue
import com.example.findyourmatch.ui.theme.LightBlue
import com.example.findyourmatch.ui.theme.LightGreen
import com.example.findyourmatch.ui.theme.LightLightGreen
import com.example.findyourmatch.ui.theme.LightRed
import com.example.findyourmatch.ui.theme.Purple
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun CardNotifica(notifica: Notifica, navController: NavHostController) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val coroutineScope = rememberCoroutineScope()
    val notificheViewModel: NotificheViewModel = viewModel(
        factory = NotificheViewModelFactory(context.applicationContext as Application)
    )

    val coloreBordo = when (notifica.tipologia) {
        "accettato" -> LightLightGreen
        "rifiutato" -> LightRed
        "partita" -> LightGreen
        "recensione" -> Blue
        "obiettivo" -> Purple
        "richiesta" -> LightBlue
        else -> Silver
    }

    val icona = when (notifica.tipologia) {
        "accettato" -> Icons.Default.Check
        "rifiutato" -> Icons.Default.Close
        "partita" -> Icons.Default.SportsSoccer
        "recensione" -> Icons.Default.Star
        "obiettivo" -> Icons.Default.EmojiEvents
        "richiesta" -> Icons.AutoMirrored.Filled.Help
        else -> Icons.Default.Notifications
    }

    val coloreSfondo = if (!notifica.stato) MaterialTheme.colorScheme.background else Color(0xFFF0F0F0)
    val cornerRadius = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(cornerRadius)
            .border(2.dp, coloreBordo, MaterialTheme.shapes.medium)
            .background(coloreSfondo)
            .clickable {
                val notificaJson = URLEncoder.encode(Json.encodeToString(notifica), StandardCharsets.UTF_8.toString())
                coroutineScope.launch {
                    segnaNotificaComeLetta(context, notifica)
                }
                notificheViewModel.segnaComeLetta(notifica)
                navController.navigate("${NavigationRoute.Notice}/$notificaJson")
            }
            .padding(16.dp)
    ) {
        Column {
            if (!notifica.stato) {
                Text(
                    localizedContext.getString(R.string.nuova),
                    color = coloreBordo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icona, contentDescription = null, tint = coloreBordo)
                Spacer(Modifier.width(8.dp))
                Text(notifica.titolo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text(notifica.testo, fontSize = 14.sp)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Notifiche(navController: NavHostController, notificheViewModel: NotificheViewModel) {
    val context = LocalContext.current
    val showBackButton = navController.previousBackStackEntry != null
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            notificheViewModel.ricaricaNotifiche()
            isRefreshing = false
        }
    )

    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }

    val notifiche by notificheViewModel.notifiche.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.notifiche),
                showBackButton = showBackButton
            )

            Spacer(Modifier.height(16.dp))

            notifiche.forEach {
                CardNotifica(it, navController)
            }

        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    }
}
