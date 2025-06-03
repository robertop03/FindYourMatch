package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.viewmodel.ProfileViewModel

@SuppressLint("DefaultLocale")
@Composable
fun StatistichePersonali(navController: NavHostController, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    val stats by profileViewModel.stats.collectAsState()
    var statsMap: Map<String, Any>? = null

    LaunchedEffect(Unit) {
        profileViewModel.ricaricaUtente()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        val showBackButton = navController.previousBackStackEntry != null

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            TopBarWithBackButton(
                navController = navController,
                title = localizedContext.getString(R.string.stats_personali),
                showBackButton = showBackButton
            )

            stats?.let {
                val gP = it.golFatti.toDouble() / it.partiteGiocate.toDouble()
                val auP = it.autogol.toDouble() / it.partiteGiocate.toDouble()
                val percW = it.vittorie.toDouble()*100 / it.partiteGiocate.toDouble()
                statsMap = mapOf(
                    localizedContext.getString(R.string.partite_giocate) to it.partiteGiocate,
                    localizedContext.getString(R.string.goal_fatti) to it.golFatti,
                    localizedContext.getString(R.string.autogol) to it.autogol,
                    localizedContext.getString(R.string.gol_per_partita) to String.format("%.2f", gP),
                    localizedContext.getString(R.string.autogol_per_partita) to String.format("%.2f", auP),
                    localizedContext.getString(R.string.val_media) to "da fare",
                    localizedContext.getString(R.string.vittorie) to it.vittorie.toString() + " (" + String.format("%.2f", percW) + " %)"
                )
            }

            StatsTable(statsMap)
            
            //GRAFICO
        }
    }
}

@Composable
fun StatsTable(info: Map<String, Any>?) {
    Column (
        modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(10.dp))
    ){
        info?.entries?.forEach {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(it.key,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f))
                Text(it.value.toString(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth())
            }
            if (it.key != "Vittorie" && it.key != "Victories") Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}