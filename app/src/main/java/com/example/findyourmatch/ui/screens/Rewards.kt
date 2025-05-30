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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.rewards.Badge
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findyourmatch.data.rewards.RewardAchievement
import com.example.findyourmatch.ui.theme.Bronze
import com.example.findyourmatch.ui.theme.Gold
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.viewmodel.RewardsViewModel


@Composable
fun Rewards(navController: NavHostController) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val viewModel: RewardsViewModel = viewModel()
    val achievements by viewModel.achievements.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadAchievements()
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
                title = localizedContext.getString(R.string.tue_medaglie),
                showBackButton = showBackButton
            )
            BadgeGrid(achievements = achievements)
            Spacer(modifier = Modifier.height(32.dp))
            LegendBox()
        }
    }
}

@Composable
fun BadgeGrid(achievements: List<RewardAchievement>) {
    val badgeSteps = listOf(10, 15, 20)
    val getColorFromColore = { colore: String ->
        when (colore.lowercase()) {
            "oro" -> 20
            "argento" -> 15
            "bronzo" -> 10
            else -> -1
        }
    }

    val goalMedals = achievements.filter { it.tipologia == "goal_fatti" }.map { getColorFromColore(it.colore) }
    val playedMedals = achievements.filter { it.tipologia == "partite_giocate" }.map { getColorFromColore(it.colore) }
    val wonMedals = achievements.filter { it.tipologia == "partite_vinte" }.map { getColorFromColore(it.colore) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        badgeSteps.reversed().forEach { step ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                BadgeItem(
                    Badge(step, wonMedals.contains(step), Icons.Default.EmojiEvents, "$step")
                )
                BadgeItem(
                    Badge(step, goalMedals.contains(step), Icons.Default.SportsSoccer, "$step")
                )
                BadgeItem(
                    Badge(step, playedMedals.contains(step), Icons.AutoMirrored.Filled.DirectionsRun, "$step")
                )
            }
        }
    }
}


@Composable
fun BadgeItem(badge: Badge) {
    val backgroundAlpha = if (badge.reached) 1f else 0.2f
    val badgeColor = when (badge.value) {
        20 -> Gold
        15 -> Silver
        10 -> Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = if (badge.reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(80.dp)
            .background(color = badgeColor.copy(alpha = backgroundAlpha), shape = MaterialTheme.shapes.medium)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = badge.icon,
            contentDescription = badge.description,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = badge.value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = iconTint
        )
    }
}



@Composable
fun LegendBox() {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Text(
            text = localizedContext.getString(R.string.obiettivi),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LegendItem(
            icon = Icons.Default.SportsSoccer,
            label = localizedContext.getString(R.string.goal_fatti)
        )
        LegendItem(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            label = localizedContext.getString(R.string.partite_giocate)
        )
        LegendItem(
            icon = Icons.Default.EmojiEvents,
            label = localizedContext.getString(R.string.partite_vinte)
        )
    }
}


@Composable
fun LegendItem(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(32.dp)
        )
        Text(text = " → ", fontSize = 30.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(
            text = label,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}


