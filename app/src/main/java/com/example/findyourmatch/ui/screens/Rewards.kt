package com.example.findyourmatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings

@Composable
fun Rewards(navController: NavHostController) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
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
            if (showBackButton) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = localizedContext.getString(R.string.indietro),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = localizedContext.getString(R.string.tue_medaglie),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            LegendBox()
        }
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
            modifier = Modifier.size(32.dp)
        )
        Text(text = " → ", fontSize = 30.sp)
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


