package com.example.findyourmatch.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.data.notifications.caricaNotificheUtente
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.dataStore
import com.example.findyourmatch.utils.getLoggedUserEmail
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificheViewModel(application: Application) : AndroidViewModel(application) {

    var notifiche by mutableStateOf<List<Notifica>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            val email = getLoggedUserEmail(application)
            if (email != null) {
                val notificheCaricate = caricaNotificheUtente(application, email)

                val lingua = getLanguage(application)
                notifiche = notificheCaricate.map { notifica ->
                    notifica.copy(
                        titolo = if (lingua == "en") notifica.titolo_en else notifica.titolo,
                        testo = if (lingua == "en") notifica.testo_en else notifica.testo
                    )
                }
            }
        }
    }

}

class NotificheViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificheViewModel(application) as T
    }
}

suspend fun getLanguage(context: Context): String {
    val prefs = context.dataStore.data.first()
    return prefs[UserSettings.LANGUAGE_KEY] ?: "it"
}
