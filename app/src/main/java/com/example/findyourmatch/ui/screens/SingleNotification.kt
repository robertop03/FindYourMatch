package com.example.findyourmatch.ui.screens

import android.app.Application
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.data.notifications.aggiungiGiocatoreAllaSquadra
import com.example.findyourmatch.data.notifications.aggiungiNotificaAccettazione
import com.example.findyourmatch.data.notifications.aggiungiNotificaRifiuto
import com.example.findyourmatch.data.notifications.prendiNomeCognomeDaEmail
import com.example.findyourmatch.data.notifications.prendiNomiSquadreDaPartita
import com.example.findyourmatch.data.notifications.prendiNumeroMassimoPartecipanti
import com.example.findyourmatch.data.notifications.prendiNumeroPartecipantiInSquadra
import com.example.findyourmatch.data.notifications.prendiPunteggioRecensione
import com.example.findyourmatch.data.notifications.segnaNotificaComeGestita
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.Bronze
import com.example.findyourmatch.ui.theme.Gold
import com.example.findyourmatch.ui.theme.LightGreen
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.viewmodel.NotificheViewModel
import com.example.findyourmatch.viewmodel.NotificheViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun Notifica(notifica: Notifica, navController: NavHostController) {
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
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var squadra1 by remember { mutableStateOf("") }
    var squadra2 by remember { mutableStateOf("") }
    var partecipantiSquadra1 by remember { mutableIntStateOf(0) }
    var partecipantiSquadra2 by remember { mutableIntStateOf(0) }
    var maxPartecipantiPerSquadra by remember { mutableIntStateOf(0) }
    var voto by remember { mutableIntStateOf(0) }
    var showDialogAccetta by remember { mutableStateOf(false) }
    var showDialogRifiuta by remember { mutableStateOf(false) }


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

                Spacer(Modifier.height(32.dp))

                when (notifica.tipologia) {
                    "accettato" -> {
                        // Nome e cognome tuo + compliemnti sei stato accetto per partecipare al calcetto

                        // link per andare alla partita relativa

                    }
                    "rifiutato" -> {
                        // Nome e cognome tuo + compliemnti sei stato accetto per partecipare al calcetto

                        // link per andare alla partita relativa


                    }
                    "partita" -> {
                        // Questa notifica viene inviato a tutti i componenti delle squadre iscritti a quella partita, 1 ora prima della partita

                        // Ehy, tra 1 ora inizierà la partita!! Sei pronto?

                        // link per andare alla partita relativa

                    }
                    "recensione" -> {
                        LaunchedEffect(notifica.autoreRecensione) {
                            coroutineScope.launch {
                                val (n, c) = prendiNomeCognomeDaEmail(context, notifica.autoreRecensione!!) ?: ("" to "")
                                nome = n
                                cognome = c
                                val v = prendiPunteggioRecensione(context, notifica.destinatarioRecensione!!, notifica.autoreRecensione, notifica.partitaRiferimentoRecensione!!)
                                voto = v!!
                            }
                        }

                        Text(
                            text = "${localizedContext.getString(R.string.autore)}: $nome $cognome",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${localizedContext.getString(R.string.contatto)}: ${notifica.autoreRecensione}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(voto) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            repeat(5 - voto) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }


                        // Clicca qui per andare alla partita di riferimento (passare l'id)
                        Text(
                            text = localizedContext.getString(R.string.vai_a_partita),
                            fontSize = 14.sp,
                            color = Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    val id = notifica.partita
                                    if (id != null) {
                                        navController.navigate("partita/$id")
                                    }
                                },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )

                    }
                    "obiettivo" -> {

                        val (colore, numero) = when (notifica.coloreMedagliaRaggiunta) {
                            "bronzo" -> "bronzo" to 10
                            "argento" -> "argento" to 15
                            "oro" -> "oro" to 20
                            else -> null to null
                        }

                        val badgeColor: Color? = when (colore) {
                            "bronzo" -> Bronze
                            "argento" -> Silver
                            "oro" -> Gold
                            else -> null
                        }

                        val icon: ImageVector? = when (notifica.titoloMedagliaRaggiunta) {
                            "partite_giocate" -> Icons.AutoMirrored.Filled.DirectionsRun
                            "goal_fatti" -> Icons.Default.SportsSoccer
                            "partite_vinte" -> Icons.Default.EmojiEvents
                            else -> null
                        }

                        val stringaObiettivo: String? = when (notifica.titoloMedagliaRaggiunta) {
                            "partite_giocate" -> "${localizedContext.getString(R.string.hai_partecipato_a)} $numero ${localizedContext.getString(R.string.partite)}!"
                            "partite_vinte" -> "${localizedContext.getString(R.string.hai_vinto)} $numero ${localizedContext.getString(R.string.partite)}!"
                            "goal_fatti" -> "${localizedContext.getString(R.string.hai_segnato)} $numero goal!"
                            else -> null
                        }

                        if (badgeColor != null && icon != null) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Text(
                                text = stringaObiettivo!!,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = localizedContext.getString(R.string.vedi_tuoi_obiettivi),
                                fontSize = 14.sp,
                                color = Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        navController.navigate(NavigationRoute.Rewards)
                                    },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        }

                    }
                    "richiesta" -> {
                        LaunchedEffect(notifica.richiedente) {
                            coroutineScope.launch {
                                val (n, c) = prendiNomeCognomeDaEmail(context, notifica.richiedente!!) ?: ("" to "")
                                nome = n
                                cognome = c
                                val nomiSquadre = prendiNomiSquadreDaPartita(context, notifica.partita!!)
                                squadra1 = nomiSquadre.getOrNull(0) ?: "?"
                                squadra2 = nomiSquadre.getOrNull(1) ?: "?"
                                val maxPartecipanti = prendiNumeroMassimoPartecipanti(context, notifica.partita)
                                maxPartecipantiPerSquadra = maxPartecipanti!!
                                val partecipanti1 = prendiNumeroPartecipantiInSquadra(context, squadra1, notifica.partita)
                                val partecipanti2 = prendiNumeroPartecipantiInSquadra(context, squadra2, notifica.partita)
                                partecipantiSquadra1 = partecipanti1
                                partecipantiSquadra2 = partecipanti2
                            }
                        }

                        Text(
                            text = "${localizedContext.getString(R.string.richiedente)}: $nome $cognome",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(15.dp))

                        if (!notifica.gestita!!) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { showDialogAccetta = true },
                                    modifier = Modifier.width(140.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LightGreen)
                                ) {
                                    Text(localizedContext.getString(R.string.accetta), color = Color.White)
                                }

                                Button(
                                    onClick = { showDialogRifiuta = true },
                                    modifier = Modifier.width(140.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                                ) {
                                    Text(localizedContext.getString(R.string.rifiuta), color = Color.White)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = localizedContext.getString(R.string.richiesta_gestita),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (showDialogAccetta) {
                            AlertDialog(
                                onDismissRequest = { showDialogAccetta = false },
                                title = { Text(localizedContext.getString(R.string.scegli_squadra)) },
                                text = {
                                    Column {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Colonna squadra 1
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$squadra1: $partecipantiSquadra1 / $maxPartecipantiPerSquadra")
                                                if (partecipantiSquadra1 >= maxPartecipantiPerSquadra) {
                                                    Text(localizedContext.getString(R.string.squadra_completa), color = Color.Red)
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                aggiungiGiocatoreAllaSquadra(context, notifica.richiedente!!, squadra1, notifica.partita!!)
                                                                aggiungiNotificaAccettazione(
                                                                    context = context,
                                                                    titolo = "Richiesta accettata",
                                                                    testo = "Sei stato accettato alla partita",
                                                                    destinatario = notifica.richiedente,
                                                                    titoloEn = "Request accepted",
                                                                    testoEn = "You have been accepted to the match",
                                                                    idPartita = notifica.partita
                                                                )
                                                                segnaNotificaComeGestita(context, notifica.idNotifica)
                                                                Toast.makeText(context, "$nome $cognome ${localizedContext.getString(R.string.aggiunto_a)} $squadra1", Toast.LENGTH_SHORT).show()
                                                                showDialogAccetta = false
                                                                navController.navigate(NavigationRoute.Notifications) {
                                                                    popUpTo(NavigationRoute.Notifications) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.width(160.dp)
                                                    ) {
                                                        Text(
                                                            squadra1,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("$squadra2: $partecipantiSquadra2 / $maxPartecipantiPerSquadra")
                                                if (partecipantiSquadra2 >= maxPartecipantiPerSquadra) {
                                                    Text(localizedContext.getString(R.string.squadra_completa), color = Color.Red)
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                aggiungiGiocatoreAllaSquadra(context, notifica.richiedente!!, squadra2, notifica.partita!!)
                                                                aggiungiNotificaAccettazione(
                                                                    context = context,
                                                                    titolo = "Richiesta accettata",
                                                                    testo = "Sei stato accettato alla partita",
                                                                    destinatario = notifica.richiedente,
                                                                    titoloEn = "Request accepted",
                                                                    testoEn = "You have been accepted to the match",
                                                                    idPartita = notifica.partita
                                                                )
                                                                segnaNotificaComeGestita(context, notifica.idNotifica)
                                                                Toast.makeText(context, "$nome $cognome ${localizedContext.getString(R.string.aggiunto_a)} $squadra2", Toast.LENGTH_SHORT).show()
                                                                showDialogAccetta = false
                                                                navController.navigate(NavigationRoute.Notifications) {
                                                                    popUpTo(NavigationRoute.Notifications) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.width(160.dp)
                                                    ) {
                                                        Text(
                                                            squadra2,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (partecipantiSquadra1 >= maxPartecipantiPerSquadra && partecipantiSquadra2 >= maxPartecipantiPerSquadra) {
                                            Spacer(Modifier.height(16.dp))
                                            Text(localizedContext.getString(R.string.squadre_al_completo), color = Color.Red)
                                        }
                                    }
                                },
                                confirmButton = {
                                    Text(
                                        localizedContext.getString(R.string.chiudi),
                                        modifier = Modifier
                                            .clickable { showDialogAccetta = false }
                                            .padding(8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }

                        if (showDialogRifiuta) {
                            AlertDialog(
                                onDismissRequest = { showDialogRifiuta = false },
                                title = { Text(localizedContext.getString(R.string.conferma_rifiuto)) },
                                text = {
                                    Text("${localizedContext.getString(R.string.rifiutare_richiesta_partecipazione)} $nome $cognome?")
                                },
                                confirmButton = {
                                    Text(
                                        localizedContext.getString(R.string.conferma),
                                        modifier = Modifier
                                            .clickable {
                                                coroutineScope.launch {
                                                    aggiungiNotificaRifiuto(
                                                        context = context,
                                                        titolo = "Richiesta rifiutata",
                                                        testo = "L'amministratore ha rifiutato la tua richiesta di partecipazione",
                                                        destinatario = notifica.richiedente!!,
                                                        titoloEn = "Request denied",
                                                        testoEn = "Your partecipation request was denied",
                                                        idPartita = notifica.partita!!
                                                    )
                                                    segnaNotificaComeGestita(context, notifica.idNotifica)
                                                    showDialogRifiuta = false
                                                    navController.navigate(NavigationRoute.Notifications) {
                                                        popUpTo(NavigationRoute.Notifications) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                            .padding(8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                dismissButton = {
                                    Text(
                                        localizedContext.getString(R.string.annulla),
                                        modifier = Modifier
                                            .clickable { showDialogRifiuta = false }
                                            .padding(8.dp)
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.height(15.dp))
                        Text(
                            text = localizedContext.getString(R.string.vedi_recensioni),
                            fontSize = 14.sp,
                            color = Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    if (notifica.richiedente != null) {
                                        navController.navigate("reviews?email=${notifica.richiedente}")
                                    }
                                },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )

                        Spacer(Modifier.height(15.dp))
                        Text(
                            text = localizedContext.getString(R.string.vai_a_partita),
                            fontSize = 14.sp,
                            color = Black,
                            textAlign = TextAlign.Center,
                                    modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    val id = notifica.partitaRiferimentoRecensione?.toIntOrNull()
                                    if (id != null) {
                                        navController.navigate("partita/$id")
                                    }
                                },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

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