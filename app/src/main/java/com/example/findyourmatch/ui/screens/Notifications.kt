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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory
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
    val ctx = localizedContext
    val coloreBordo = when (notifica.tipologia) {
        "accettato" -> Color(0xFF4CAF50)
        "rifiutato" -> Color(0xFFF44336)
        "partita" -> Color(0xFF2E7D32)
        "recensione" -> Color(0xFF1976D2)
        "obiettivo" -> Color(0xFF8976D2)
        "richiesta" -> Color(0xFF0079D2)
        else ->Color(0xFF1976D2)
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

    val coloreSfondo = if (!notifica.stato) MaterialTheme.colorScheme.background else Color(0xFF1976D2)
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
                navController.navigate("${NavigationRoute.Notice}/$notificaJson")
            }
            .padding(16.dp)
    ) {
        Column {
            if (!notifica.stato) {
                Text(ctx.getString(R.string.nuova), color = coloreBordo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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


@Composable
fun Notifiche(navController: NavHostController) {
    val context = LocalContext.current
    val notificheViewModel: NotificheViewModel = viewModel(
        factory = NotificheViewModelFactory(context.applicationContext as Application)
    )

    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val ctx = localizedContext

    val notifiche by remember { derivedStateOf { notificheViewModel.notifiche } }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
            if (navController.previousBackStackEntry != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = ctx.getString(R.string.indietro),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = ctx.getString(R.string.notifiche),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            notifiche.forEach {
                CardNotifica(it, navController)
            }
        }
    }
}
