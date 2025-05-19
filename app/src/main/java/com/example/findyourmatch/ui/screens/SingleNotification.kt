package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.ui.theme.LightGreen
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun Notifica(notifica: Notifica, navController: NavHostController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .border(2.dp, LightGreen, RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = { navController.navigateUp() }) {
                        Text("X", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
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


                Spacer(Modifier.height(16.dp))

                Icon(imageVector = icona, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Black)

                Spacer(Modifier.height(16.dp))

                Text(
                    text = notifica.titolo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = notifica.testo,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.height(250.dp))

                Text(
                    text = formattaDataOra(notifica.dataOraInvio),
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

private fun formattaDataOra(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d/%02d/%d %02d:%02d".format(
        local.dayOfMonth,
        local.monthNumber,
        local.year,
        local.hour,
        local.minute
    )
}