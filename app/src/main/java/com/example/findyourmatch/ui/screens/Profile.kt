package com.example.findyourmatch.ui.screens

import android.widget.Button
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.AnagraficaUtente
import com.example.findyourmatch.data.user.IndirizzoUtente
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.getLoggedUserEmail
import com.example.findyourmatch.data.user.getUserInfo
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(navController: NavHostController, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val userSettings = remember { UserSettings(context) }
    val language by userSettings.language.collectAsState(initial = "it")
    val localizedContext = remember(language) {
        LocaleHelper.updateLocale(context, language)
    }
    val showDecisionOnProfileImage = remember { mutableStateOf(false) }

    val utente by profileViewModel.user.collectAsState()
    val indirizzo by profileViewModel.userAddress.collectAsState()
    var infoMap: Map<String, String>? = null

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
                title = localizedContext.getString(R.string.profilo),
                showBackButton = showBackButton
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Image(
                    painter = painterResource(id = R.drawable.no_profile_image),
                    contentDescription = "Foto Profilo",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp)
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { showDecisionOnProfileImage.value = true }
                )

                Column {
                    utente?.let {
                        Text(
                            text = it.nome + " " + it.cognome,
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = it.email,
                            fontWeight = FontWeight.W400,
                            fontSize = 15.sp,
                            color = Silver
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        GenerateButton(localizedContext.getString(R.string.btn_modifica), Icons.Default.Edit)
                        Spacer(modifier = Modifier.height(5.dp))
                        GenerateButton(localizedContext.getString(R.string.btn_vedi_stats), Icons.Default.Insights)

                        indirizzo?.let { i ->
                            infoMap = mapOf(
                                localizedContext.getString(R.string.iscrizione) to it.iscrizione,
                                localizedContext.getString(R.string.data_di_nascita) to it.nascita,
                                localizedContext.getString(R.string.sesso) to if (it.sesso == "Maschio") "M" else "F",
                                localizedContext.getString(R.string.citta_residenza) to i.citta + ", " + i.stato,
                                localizedContext.getString(R.string.indirizzo) to (if (!i.via.contains("via", true)) "Via " else "") + i.via + ", " + i.civico
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
            InfoTable(infoMap)
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(NavigationRoute.Rewards)
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedContext.getString(R.string.tue_medaglie),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = localizedContext.getString(R.string.vedi_tutte_medaglie)
                )
            }
        }
    }

    if (showDecisionOnProfileImage.value) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        ModalBottomSheet (
            onDismissRequest = { showDecisionOnProfileImage.value = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = localizedContext.getString(R.string.scatta_foto),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDecisionOnProfileImage.value = false
//                            launchCamera()
                        }
                        .padding(16.dp)
                )
                Text(
                    text = localizedContext.getString(R.string.scegli_galleria),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDecisionOnProfileImage.value = false
//                            pickFromGallery()
                        }
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun GenerateButton(text: String, icon: ImageVector) {
    Button(
        onClick = {},
        modifier = Modifier
            .height(35.dp)
            .width(200.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimary)
        ){
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(0.dp))
    }
}

@Composable
fun InfoTable(info: Map<String, String>?) {
    Column (
        modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.onSecondaryContainer, RoundedCornerShape(10.dp))
    ){
        info?.entries?.forEach {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
            ) {
                Column (modifier = Modifier.weight(1f)){
                    Text(it.key, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Column (modifier = Modifier.weight(1f)){
                    Text(it.value, color = MaterialTheme.colorScheme.onSecondaryContainer, textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth())
                }
            }
            if (it.key != "Indirizzo" && it.key != "Address") Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}