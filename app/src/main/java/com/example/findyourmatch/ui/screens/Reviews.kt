package com.example.findyourmatch.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.Recensione
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.viewmodel.ProfileViewModel
import com.example.findyourmatch.viewmodel.ReviewsViewModel
import kotlinx.datetime.toLocalDateTime

@Composable
fun Recensioni(navController: NavHostController, email: String?, reviewsViewModel: ReviewsViewModel, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }

    val reviews by if (email != null) reviewsViewModel.reviews.collectAsState() else profileViewModel.reviews.collectAsState()

    LaunchedEffect(Unit) {
        if (email != null)
            reviewsViewModel.loadReviews(email)
        else
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
                title = localizedContext.getString(R.string.recensioni),
                showBackButton = showBackButton
            )

            reviews?.let {
                if (it.isNotEmpty()) {
                    ReviewsTable(it, navController, localizedContext)
                } else {
                    Text(localizedContext.getString(R.string.no_recensioni),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp))
                }
            }
        }
    }
}

@Composable
fun HeadText(headString: String) {
    Text(text = headString,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        modifier = Modifier.padding(0.dp, 12.dp))
}

@Composable
fun ReviewsTable(reviews: List<Recensione>, navController: NavHostController, locContext: Context) {
    Column (
        modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(10.dp))
    ){
        Row {
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                HeadText(locContext.getString(R.string.autore))
            }
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                HeadText(locContext.getString(R.string.punteggio))
            }
            Column (
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                HeadText(locContext.getString(R.string.partita))
            }
        }
        Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        reviews.forEachIndexed { index, review ->
            Row(modifier = Modifier
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = review.nomeAutore + "\n" + review.cognomeAutore,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(0.dp, 8.dp),
                        textAlign = TextAlign.Center)
                }
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    PrintStars(review.punteggio.toDouble())
                }
                Column (
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(text = locContext.getString(R.string.partita_del) + "\n" + review.dataOraPartita.toLocalDateTime().date,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .padding(3.dp, 8.dp)
                            .clickable { navController.navigate("partita/${review.partita}") },
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline)
                }
            }
            if (index != reviews.lastIndex) Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}