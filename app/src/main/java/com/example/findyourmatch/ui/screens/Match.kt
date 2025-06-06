package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.GiocatoreWrapper
import com.example.findyourmatch.data.match.PartitaMostrata
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.Green
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.MatchViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime
import kotlinx.datetime.toLocalDateTime

@SuppressLint("DefaultLocale")
@Composable
fun Partita(navController: NavHostController, idPartita: Int, matchViewModel: MatchViewModel) {
    val context = LocalContext.current
    val showBackButton = navController.previousBackStackEntry != null

    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) { LocaleHelper.updateLocale(context, language) }
    val scope = rememberCoroutineScope()

    val currentUser by matchViewModel.currentUser.collectAsState()
    val match by matchViewModel.match.collectAsState()
    val playersTeam1 by matchViewModel.giocatoriSquadra1.collectAsState()
    val playersTeam2 by matchViewModel.giocatoriSquadra2.collectAsState()
    val isUserInRequestState by matchViewModel.inRequestState.collectAsState()

    LaunchedEffect(Unit) {
        matchViewModel.loadMatch(idPartita)
    }

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
            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.partita),
                showBackButton = showBackButton
            )

            match?.let {
                playersTeam1?.let {
                    playersTeam2?.let {
                        TeamsTable(match!!, playersTeam1!!, playersTeam2!!)
                    }
                }
                val isCreator = currentUser == match!!.creatore
                val isSubscribed = playersTeam1?.any { it.utente.email == currentUser } == true
                        || playersTeam2?.any { it.utente.email == currentUser } == true
                val subscribingNotExpired = LocalDateTime.now().isBefore(match!!.dataOraScadenzaIscrizione.toLocalDateTime().toJavaLocalDateTime())

                if ((subscribingNotExpired || isCreator) && match!!.visibile) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 15.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                //disiscrizione
                                if (isCreator) {
                                    //elimina partita
                                } else if (isSubscribed) {
                                    //disiscriviti
                                    val currentUserTeam =
                                        if (playersTeam1?.any { it.utente.email == currentUser } == true) match!!.squadra1 else match!!.squadra2
                                    scope.launch {
                                        if (matchViewModel.unsubscribePlayer(currentUserTeam, idPartita)) {
                                            matchViewModel.loadMatch(idPartita)
                                        }
                                    }
                                } else if (isUserInRequestState != null && isUserInRequestState == false) {
                                    //iscriviti
                                    matchViewModel.sendParticipationRequest(idPartita)
                                    scope.launch {
                                        matchViewModel.loadMatch(idPartita)
                                    }
                                }
                            },
                            modifier = Modifier.height(50.dp).width(250.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCreator || isSubscribed) Red else MaterialTheme.colorScheme.onSecondaryContainer,
                                contentColor = if (isCreator || isSubscribed) White else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = when {
                                    isCreator -> localizedContext.getString(R.string.elimina)
                                    isSubscribed -> localizedContext.getString(R.string.disiscrivimi)
                                    isUserInRequestState == true -> "in attesa"
                                    else -> localizedContext.getString(R.string.iscrivimi)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp
                            )
                        }
                    }
                } else if (!subscribingNotExpired && match!!.visibile) {
                    Text(localizedContext.getString(R.string.no_iscrizione),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = localizedContext.getString(R.string.info),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                InfoLine(Icons.Default.SportsSoccer, localizedContext.getString(R.string.calcio_a), match!!.tipo, context)
                InfoLine(Icons.Default.CalendarToday, localizedContext.getString(R.string.data), match!!.dataOra.toLocalDateTime().date.toString(), context)
                InfoLine(Icons.Default.AccessTime, localizedContext.getString(R.string.ore), match!!.dataOra.toLocalDateTime().time.toString(), context)
                InfoLine(Icons.Default.LocationOn, localizedContext.getString(R.string.luogo),
                    match!!.nomeCampo + ", " + match!!.citta + ", " + match!!.nazione, context)
                InfoLine(Icons.Default.EuroSymbol, localizedContext.getString(R.string.importo), String.format("%.2f", match!!.importo) + " €", context)
                InfoLine(Icons.Default.ManageAccounts, localizedContext.getString(R.string.organizzatore),
                    match!!.nomeCreatore + " " + match!!.cognomeCreatore, context)
                InfoLine(Icons.Default.Phone, localizedContext.getString(R.string.cellulare),
                    match!!.telefono.dropLast(10) + " " + match!!.telefono.takeLast(10), context)
                Text(
                    text = localizedContext.getString(R.string.condividi),
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    color = Green,
                    modifier = Modifier.padding(12.dp, 8.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                MatchButton(localizedContext.getString(R.string.btn_vedi_stats), 8.dp)
                if (isCreator && !match!!.visibile) {
                    MatchButton(localizedContext.getString(R.string.btn_ins_dettagli), 0.dp)
                }
            }
        }
    }
}

@Composable
fun TeamsTable(match: PartitaMostrata, squadra1: List<GiocatoreWrapper>, squadra2: List<GiocatoreWrapper>) {
    val numRows = when (match.tipo) {
        "5vs5" -> 5
        "7vs7" -> 7
        "8vs8" -> 8
        else -> 11
    }
    Column (
        modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(10.dp))
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeadText(match.squadra1)
            }
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeadText(match.squadra2)
            }
        }
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        for (i in 0 until numRows) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val player1 = squadra1.getOrNull(i)
                    Text(
                        text = if (player1 != null) player1.utente.nome + " " + player1.utente.cognome else "",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(0.dp, 5.dp)
                    )
                }
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val player2 = squadra2.getOrNull(i)
                    Text(
                        text = if (player2 != null) player2.utente.nome + " " + player2.utente.cognome else "",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(0.dp, 8.dp)
                    )
                }
            }
            if (i != (numRows-1)) Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun InfoLine(icon: ImageVector, description: String, value: String, context: Context) {
    Row (
        verticalAlignment = Alignment.CenterVertically
    ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        val twoPoints = if (!value.contains("vs")) ": " else " "
        val isPhoneNumber = description.contains("Cel") || description.contains("Pho")
        Text(description + twoPoints + value, color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.clickable {
                if (isPhoneNumber) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        val number = value.takeLast(10)
                        data = Uri.parse("tel:$number")
                    }
                    context.startActivity(intent)
                }
            },
            textDecoration = if (isPhoneNumber) TextDecoration.Underline else null)
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun MatchButton(text: String, padding: Dp) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(0.dp, padding),
        horizontalArrangement = Arrangement.Center
    ){
        Button(
            onClick = {},
            modifier = Modifier
                .height(50.dp)
                .width(250.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary)
        ) {
            Text(text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp)
        }
    }
}