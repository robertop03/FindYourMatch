package com.example.findyourmatch.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

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
    val lastGamesStats by profileViewModel.gamesStatsMap.collectAsState()

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
                val gP = if (it.partiteGiocate > 0) it.golFatti.toDouble() / it.partiteGiocate.toDouble() else 0.0
                val auP = if (it.partiteGiocate > 0) it.autogol.toDouble() / it.partiteGiocate.toDouble() else 0.0
                val percW = if (it.partiteGiocate > 0) it.vittorie.toDouble()*100 / it.partiteGiocate.toDouble() else 0.0
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
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = localizedContext.getString(R.string.andamento),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            //GRAFICO
            val axisTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()
            AndroidView(
                factory = { context ->
                    LineChart(context)
                },
                update = { chart ->
                    val reversed = lastGamesStats.entries
                    .toList()
                    .asReversed()
                    .associateTo(LinkedHashMap()) { it.toPair() }

                    val labels = reversed.keys.toList()

                    val entriesGol = reversed.values.mapIndexed { index, stats ->
                        stats?.numeroGol?.let { Entry(index.toFloat(), it.toFloat()) }
                    }

                    val entriesAutogol = reversed.values.mapIndexed { index, stats ->
                        stats?.numeroAutogol?.let { Entry(index.toFloat(), it.toFloat()) }
                    }

                    val dataSetGol = LineDataSet(entriesGol, localizedContext.getString(R.string.goal_fatti)).apply {
                        color = Color.GREEN
                        lineWidth = 2f
                        setDrawValues(false)
                    }

                    val dataSetAutogol = LineDataSet(entriesAutogol, localizedContext.getString(R.string.autogol)).apply {
                        color = Color.RED
                        lineWidth = 2f
                        setDrawValues(false)
                    }

                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < labels.size) labels[index] else ""
                        }
                    }

                    chart.xAxis.granularity = 1f
                    chart.xAxis.isGranularityEnabled = true
                    chart.xAxis.textColor = axisTextColor

//                    chart.axisLeft.granularity = 1f
//                    chart.axisLeft.isGranularityEnabled = true
                    chart.axisLeft.textColor = axisTextColor

                    chart.data = LineData(dataSetGol, dataSetAutogol)
                    chart.axisRight.isEnabled = false
                    chart.description.isEnabled = false
                    chart.legend.textColor = axisTextColor
                    chart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
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