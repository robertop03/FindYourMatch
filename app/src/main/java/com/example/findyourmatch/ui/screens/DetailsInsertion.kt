package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.InserimentoStatsGiocatore
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.viewmodel.MatchViewModel

@Composable
fun InserisciDettagli(navController: NavHostController, matchViewModel: MatchViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val showBackButton = navController.previousBackStackEntry != null

    val match by matchViewModel.match.collectAsState()
    var team1Goals by remember { mutableStateOf("0") }
    var team2Goals by remember { mutableStateOf("0") }
    val playersTeam1 by matchViewModel.giocatoriSquadra1.collectAsState()
    val playersTeam2 by matchViewModel.giocatoriSquadra2.collectAsState()

    val playersTeam1Stats = remember {
        mutableStateListOf<InserimentoStatsGiocatore>().apply {
            playersTeam1?.forEach {
                add(InserimentoStatsGiocatore(email = it.utente.email, nomeCognome = it.utente.nome + it.utente.cognome))
            }
        }
    }
    val playersTeam2Stats = remember {
        mutableStateListOf<InserimentoStatsGiocatore>().apply {
            playersTeam2?.forEach {
                add(InserimentoStatsGiocatore(email = it.utente.email, nomeCognome = it.utente.nome + it.utente.cognome))
            }
        }
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
                title = localizedContext.getString(R.string.btn_ins_dettagli),
                showBackButton = showBackButton
            )

            match?.let {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = match!!.squadra1,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "  -  ",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(0.2f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = match!!.squadra2,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = localizedContext.getString(R.string.inserisci_risultato),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = team1Goals,
                        onValueChange = { team1Goals = it },
                        singleLine = true,
                        modifier = Modifier
                            .width(60.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center, // centra il testo orizzontalmente
                            fontSize = 20.sp              // puoi regolare anche la dimensione
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "-",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    OutlinedTextField(
                        value = team2Goals,
                        onValueChange = { team2Goals = it },
                        singleLine = true,
                        modifier = Modifier
                            .width(60.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center, // centra il testo orizzontalmente
                            fontSize = 20.sp              // puoi regolare anche la dimensione
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(50.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = match!!.squadra1,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("", modifier = Modifier.weight(1f))
                    Text(
                        text = localizedContext.getString(R.string.gol),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = localizedContext.getString(R.string.autogol),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = localizedContext.getString(R.string.punteggio),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center
                    )
                }
//                playersTeam1?.forEach {
//                    Row (
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            text = it.utente.nome + it.utente.cognome,
//                            color = MaterialTheme.colorScheme.onSecondaryContainer,
//                            modifier = Modifier.weight(1f),
//                        )
//
//                    }
//                }
                playersTeam1Stats.forEach {
                    Row (
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(
                            text = it.nomeCognome,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Column (
                            modifier = Modifier.weight(0.7f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //CONTINUARE
                        }
                    }
                }
            }
        }
    }
}