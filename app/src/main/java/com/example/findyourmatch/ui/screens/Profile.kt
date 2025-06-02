package com.example.findyourmatch.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.CalendarToday
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.findyourmatch.R
import com.example.findyourmatch.data.user.LocaleHelper
import com.example.findyourmatch.data.user.PartiteGiocateUtente
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.navigation.NavigationRoute
import com.example.findyourmatch.ui.theme.Black
import com.example.findyourmatch.ui.theme.Bronze
import com.example.findyourmatch.ui.theme.Gold
import com.example.findyourmatch.ui.theme.Green
import com.example.findyourmatch.ui.theme.Grey
import com.example.findyourmatch.ui.theme.Red
import com.example.findyourmatch.ui.theme.Silver
import com.example.findyourmatch.ui.theme.White
import com.example.findyourmatch.viewmodel.ProfileViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    val profileImageUri by profileViewModel.profileImageUri.collectAsState()
    val numRewardsAchieved by profileViewModel.numRewardsAchieved.collectAsState()
    val rewardsAchieved by profileViewModel.maxRewardsAchieved.collectAsState()
    val playedGames by profileViewModel.playedGames.collectAsState()
    var infoMap: Map<String, String>? = null

    var capturedImageUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { pictureTaken ->
        if (pictureTaken && capturedImageUri != Uri.EMPTY) {
            profileViewModel.saveLocalProfileImageUri(capturedImageUri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileViewModel.saveLocalProfileImageUri(it)
        }
    }

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
                title = localizedContext.getString(R.string.profilo),
                showBackButton = showBackButton
            )

            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                val imageRequest = ImageRequest.Builder(context)
                    .data(profileImageUri)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()

                utente?.let {
                    Image(
                        painter = if (profileImageUri != null && profileImageUri != Uri.EMPTY) rememberAsyncImagePainter(imageRequest) else painterResource(id = R.drawable.no_profile_image),
                        contentDescription = "Foto Profilo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 20.dp)
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { showDecisionOnProfileImage.value = true }
                    )
                }

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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = localizedContext.getString(R.string.vedi_tutte_medaglie),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (numRewardsAchieved == 0) {
                Text(localizedContext.getString(R.string.no_obiettivi_raggiunti),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp))
            } else {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    rewardsAchieved?.forEach {
                        val color = when (it.colore) {
                            "oro" -> Gold
                            "argento" -> Silver
                            "bronzo" -> Bronze
                            else -> White
                        }
                        val icon = when (it.tipologia) {
                            "goal_fatti" -> Icons.Default.SportsSoccer
                            "partite_giocate" -> Icons.AutoMirrored.Filled.DirectionsRun
                            "partite_vinte" -> Icons.Default.EmojiEvents
                            else -> null
                        }
                        Column (
                            modifier = Modifier.width(110.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Column (
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(color = color, shape = MaterialTheme.shapes.medium)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ){
                                Icon(
                                    imageVector = icon!!,
                                    contentDescription = it.tipologia,
                                    tint = White,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            val text = when {
                                color == Bronze && it.tipologia == "goal_fatti" -> localizedContext.getString(R.string.gol_segnati_bronzo)
                                color == Silver && it.tipologia == "goal_fatti" -> localizedContext.getString(R.string.gol_segnati_argento)
                                color == Gold && it.tipologia == "goal_fatti" -> localizedContext.getString(R.string.gol_segnati_oro)
                                color == Bronze && it.tipologia == "partite_giocate" -> localizedContext.getString(R.string.giocate_bronzo)
                                color == Silver && it.tipologia == "partite_giocate" -> localizedContext.getString(R.string.giocate_argento)
                                color == Gold && it.tipologia == "partite_giocate" -> localizedContext.getString(R.string.giocate_oro)
                                color == Bronze && it.tipologia == "partite_vinte" -> localizedContext.getString(R.string.vittorie_bronzo)
                                color == Silver && it.tipologia == "partite_vinte" -> localizedContext.getString(R.string.vittorie_argento)
                                color == Gold && it.tipologia == "partite_vinte" -> localizedContext.getString(R.string.vittorie_oro)
                                else -> ""
                            }
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(NavigationRoute.PlayedGames)
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = localizedContext.getString(R.string.ultime_partite),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = localizedContext.getString(R.string.vedi_partite_giocate),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (playedGames == null || playedGames!!.isEmpty()) {
                Text(localizedContext.getString(R.string.no_partite),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(0.dp, 5.dp))
            } else {
                playedGames!!.take(3).forEach {
                    GameCard(it, navController, localizedContext, utente!!.email)
                }
            }
        }
    }

    if (showDecisionOnProfileImage.value) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            val imageFile = File.createTempFile("profile_image", ".jpg", context.cacheDir)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
                            capturedImageUri = uri
                            context.grantUriPermission(
                                context.packageName,
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                            cameraLauncher.launch(uri)
                            showDecisionOnProfileImage.value = false
                        }
                        .padding(16.dp),
                    fontSize = 18.sp
                )
                Text(
                    text = localizedContext.getString(R.string.scegli_galleria),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            galleryLauncher.launch("image/*")
                            showDecisionOnProfileImage.value = false
                        }
                        .padding(16.dp),
                    fontSize = 18.sp
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
                .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(it.key,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f))
                Text(it.value,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth())
            }
            if (it.key != "Indirizzo" && it.key != "Address") Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun GameCard(game: PartiteGiocateUtente?, navController: NavHostController, localizedContext: Context, currentUser: String) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val dateTime = LocalDateTime.parse(game!!.dataOra, formatter)
    val backgroundColor = when (game.esito) {
        "vittoria" -> Green
        "pareggio" -> Grey
        else -> Red
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(backgroundColor, shape = RoundedCornerShape(15.dp))
            .clickable {
                val id = game.id
                navController.navigate("partita/$id")
            }
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Black, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${dateTime.dayOfMonth} ${dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${dateTime.year}",
                            color = White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${localizedContext.getString(R.string.ore)}: %02d:%02d".format(dateTime.hour, dateTime.minute),
                        color = White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${localizedContext.getString(R.string.calcio_a)} ${game.tipo}",
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(text = game.citta,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp)
            }
            Text(text = game.nomeCampo,
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(0.dp, 3.dp))

            Row (modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                if (game.squadra1 == game.squadraUtente) {
                    ShieldIcon()
                }
                Text(text = game.squadra1,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(if (game.squadra1 == game.squadraUtente) 5.dp else 0.dp, 0.dp, 10.dp, 0.dp))
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Black)
                ) {
                    Text(text = "${game.gol1} - ${game.gol2}",
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(8.dp))
                }
                Text(text = game.squadra2,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(10.dp, 0.dp))
                if (game.squadra2 == game.squadraUtente) {
                    ShieldIcon()
                }
            }
            Text(
                text = "${localizedContext.getString(R.string.organizzatore)}: " +
                        if (game.creatore == currentUser) localizedContext.getString(R.string.tu) else game.creatore,
                color = White,
                fontSize = 14.sp,
                modifier = Modifier.padding(0.dp, 4.dp, 0.dp, 0.dp))
        }
    }
}

@Composable
fun ShieldIcon() {
    Icon(
        imageVector = Icons.Default.Shield,
        contentDescription = null,
        tint = White,
        modifier = Modifier.size(35.dp)
    )
}