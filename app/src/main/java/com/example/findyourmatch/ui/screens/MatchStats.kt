package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.match.AutoreAutogol
import com.example.findyourmatch.data.match.GiocatoreWrapper
import com.example.findyourmatch.data.match.Marcatore
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.ui.theme.LightRed
import com.example.findyourmatch.viewmodel.MatchViewModel

@Composable
fun StatistichePartita(navController: NavHostController, matchViewModel: MatchViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val showBackButton = navController.previousBackStackEntry != null

    val match by matchViewModel.match.collectAsState()
    val playersTeam1 by matchViewModel.giocatoriSquadra1.collectAsState()
    val playersTeam2 by matchViewModel.giocatoriSquadra2.collectAsState()
    val scorers by matchViewModel.scorers.collectAsState()
    val ownGoalsScorers by matchViewModel.ownGoalsScorers.collectAsState()

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
                title = localizedContext.getString(R.string.statistiche_partita),
                showBackButton = showBackButton
            )

            match?.let {
                if (match!!.golSquadra1 == null && match!!.golSquadra2 == null) {
                    Text(localizedContext.getString(R.string.no_stats),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp))
                } else {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(7.dp, 0.dp)
                            .clip(RoundedCornerShape(35))
                            .background(MaterialTheme.colorScheme.onSecondaryContainer)
                            .height(60.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = match!!.squadra1,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${match!!.golSquadra1} - ${match!!.golSquadra2}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = match!!.squadra2,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match!!.squadra1,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TeamRows(playersTeam1, scorers, ownGoalsScorers)
                    Spacer(modifier = Modifier.height(30.dp))
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match!!.squadra2,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TeamRows(playersTeam2, scorers, ownGoalsScorers)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Column (
                modifier = Modifier.padding(7.dp, 0.dp)
            ) {
                Text(
                    text = localizedContext.getString(R.string.legenda),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1 " + localizedContext.getString(R.string.gol),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = LightRed,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "1 " + localizedContext.getString(R.string.autogol_s),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun TeamRows(teamPlayers: List<GiocatoreWrapper>?, scorers: List<Marcatore>?, ownGoalsScorers: List<AutoreAutogol>?) {
    teamPlayers?.let {
        teamPlayers.forEach { p ->
            Row (
                modifier = Modifier.padding(7.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                if (scorers!!.any { it.utente == p.utente.email } || ownGoalsScorers!!.any { it.utente == p.utente.email })
                {
                    Text(
                        p.utente.nome + " " + p.utente.cognome,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                }
                scorers.forEach { s ->
                    if (p.utente.email == s.utente) {
                        for (i in 0 until s.numeroGol) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(25.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }

                ownGoalsScorers?.let {
                    ownGoalsScorers.forEach { s ->
                        if (p.utente.email == s.utente) {
                            for (i in 0 until s.numeroAutogol) {
                                Icon(
                                    imageVector = Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    tint = LightRed,
                                    modifier = Modifier.size(25.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}