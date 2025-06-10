package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.MatchViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InserisciDettagli(navController: NavHostController, matchViewModel: MatchViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val showBackButton = navController.previousBackStackEntry != null
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val match by matchViewModel.match.collectAsState()
    var team1Goals by remember { mutableStateOf("0") }
    var team2Goals by remember { mutableStateOf("0") }
    val playersTeam1 by matchViewModel.giocatoriSquadra1.collectAsState()
    val playersTeam2 by matchViewModel.giocatoriSquadra2.collectAsState()

    val playersTeam1Stats = remember {
        mutableStateListOf<InserimentoStatsGiocatore>().apply {
            playersTeam1?.forEach {
                add(InserimentoStatsGiocatore(email = it.utente.email, nomeCognome = it.utente.nome + " " + it.utente.cognome))
            }
        }
    }
    val playersTeam2Stats = remember {
        mutableStateListOf<InserimentoStatsGiocatore>().apply {
            playersTeam2?.forEach {
                add(InserimentoStatsGiocatore(email = it.utente.email, nomeCognome = it.utente.nome + " " + it.utente.cognome))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                HeadRow(localizedContext)
                Spacer(modifier = Modifier.height(15.dp))
                playersTeam1Stats.forEach {
                    GenerateRow(it, match!!.creatore)
                }
                Spacer(modifier = Modifier.height(50.dp))
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Text(
                        text = match!!.squadra2,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                HeadRow(localizedContext)
                Spacer(modifier = Modifier.height(15.dp))
                playersTeam2Stats.forEach {
                    GenerateRow(it, match!!.creatore)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
//                            coroutineScope.launch {
//                                snackbarHostState.showSnackbar("stocazzo")
//                            }
                            validation(playersTeam1Stats, playersTeam2Stats) {}
                        },
                        modifier = Modifier.width(150.dp).height(42.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            localizedContext.getString(R.string.salva),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Button(
                        onClick = {
                            navController.navigateUp()
                        },
                        modifier = Modifier.width(150.dp).height(42.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red,
                            contentColor = White
                        )
                    ) {
                        Text(
                            localizedContext.getString(R.string.annulla),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeadRow(localizedContext: Context) {
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
}

@Composable
fun GenerateRow(player: InserimentoStatsGiocatore, matchOrganizer: String) {
    Row (
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = player.nomeCognome,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        Column (
            modifier = Modifier.weight(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = player.gol.value,
                onValueChange = { v ->
                    player.gol.value = v
                },
                singleLine = true,
                modifier = Modifier
                    .width(55.dp)
                    .height(55.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Column (
            modifier = Modifier.weight(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = player.autogol.value,
                onValueChange = { v ->
                    player.autogol.value = v
                },
                singleLine = true,
                modifier = Modifier
                    .width(55.dp)
                    .height(55.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row (modifier = Modifier.weight(1.2f))
        {
            if (player.email != matchOrganizer) {
                for (i in 0 until 5) {
                    Icon(
                        imageVector = if (i < player.rating.value) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Value: ${i+1}",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .clickable {
                                player.rating.value = i + 1
                            }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

fun validation(players1: List<InserimentoStatsGiocatore>, players2: List<InserimentoStatsGiocatore>, onSuccess: () -> Unit) {
    
}