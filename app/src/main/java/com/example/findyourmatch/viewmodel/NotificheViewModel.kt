package com.example.findyourmatch.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.notifications.Notifica
import com.example.findyourmatch.data.notifications.caricaNotificheUtente
import com.example.findyourmatch.data.user.UserSettings
import com.example.findyourmatch.data.user.dataStore
import com.example.findyourmatch.data.user.getLoggedUserEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificheViewModel(application: Application) : AndroidViewModel(application) {

    private val _notifiche = MutableStateFlow<List<Notifica>>(emptyList())
    val notifiche = _notifiche

    init {
        viewModelScope.launch {
            val email = getLoggedUserEmail(application)
            if (email != null) {
                val notificheCaricate = caricaNotificheUtente(application, email)

                val lingua = getLanguage(application)
                _notifiche.value = notificheCaricate.map { notifica ->
                    notifica.copy(
                        titolo = if (lingua == "en") notifica.titoloEn else notifica.titolo,
                        testo = if (lingua == "en") notifica.testoEn else notifica.testo
                    )
                }
            }
        }
    }

    fun segnaComeLetta(notificaDaAggiornare: Notifica) {
        _notifiche.value = _notifiche.value.map { notifica ->
            if (notifica.idNotifica == notificaDaAggiornare.idNotifica) {
                notifica.copy(stato = true)
            } else {
                notifica
            }
        }
    }

    fun ricaricaNotifiche() {
        viewModelScope.launch {
            val email = getLoggedUserEmail(getApplication())
            if (email != null) {
                val notificheCaricate = caricaNotificheUtente(getApplication(), email)
                val lingua = getLanguage(getApplication())
                _notifiche.value = notificheCaricate.map { notifica ->
                    notifica.copy(
                        titolo = if (lingua == "en") notifica.titoloEn else notifica.titolo,
                        testo = if (lingua == "en") notifica.testoEn else notifica.testo
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
        if (modelClass.isAssignableFrom(NotificheViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificheViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

suspend fun getLanguage(context: Context): String {
    val prefs = context.dataStore.data.first()
    return prefs[UserSettings.LANGUAGE_KEY] ?: "it"
}
