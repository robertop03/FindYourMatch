package com.example.findyourmatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.match.AutoreAutogol
import com.example.findyourmatch.data.match.GiocatoreWrapper
import com.example.findyourmatch.data.match.Marcatore
import com.example.findyourmatch.data.match.PartitaMostrata
import com.example.findyourmatch.data.match.deleteMatch
import com.example.findyourmatch.data.match.getMatch
import com.example.findyourmatch.data.match.getOwnGoalsScorers
import com.example.findyourmatch.data.match.getScorers
import com.example.findyourmatch.data.match.getTeamPlayers
import com.example.findyourmatch.data.match.isUserInRequestState
import com.example.findyourmatch.data.match.unsubscribePlayerFromMatch
import com.example.findyourmatch.data.notifications.aggiungiNotificaEliminazioneDaAdmin
import com.example.findyourmatch.data.notifications.aggiungiNotificaRichiesta
import com.example.findyourmatch.data.notifications.inviaNotificaPush
import com.example.findyourmatch.data.notifications.prendiTokenFCMDaEmail
import com.example.findyourmatch.data.user.getLoggedUserEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val _match = MutableStateFlow<PartitaMostrata?>(null)
    private val _giocatoriSquadra1 = MutableStateFlow<List<GiocatoreWrapper>?>(null)
    private val _giocatoriSquadra2 = MutableStateFlow<List<GiocatoreWrapper>?>(null)
    private val _currentUser = MutableStateFlow<String?>(null)
    private val _inRequestState = MutableStateFlow<Boolean?>(null)
    private val _scorers = MutableStateFlow<List<Marcatore>?>(null)
    private val _ownGoalsScorers = MutableStateFlow<List<AutoreAutogol>?>(null)
    val match = _match
    val giocatoriSquadra1 = _giocatoriSquadra1
    val giocatoriSquadra2 = _giocatoriSquadra2
    val currentUser = _currentUser
    val inRequestState = _inRequestState
    val scorers = _scorers
    val ownGoalsScorers = _ownGoalsScorers
    var id: Int? = null

    fun loadMatch(idMatch: Int) {
        viewModelScope.launch {
            _currentUser.value = getLoggedUserEmail(application)
            _match.value = getMatch(application, idMatch)
            _giocatoriSquadra1.value = _match.value?.let { getTeamPlayers(application, it.squadra1, idMatch) }
            _giocatoriSquadra2.value = _match.value?.let { getTeamPlayers(application, it.squadra2, idMatch) }
            _inRequestState.value = isUserInRequestState(application, _currentUser.value!!, _match.value!!.creatore, idMatch)
            _scorers.value = getScorers(application, idMatch)
            _ownGoalsScorers.value = getOwnGoalsScorers(application, idMatch)
            id = idMatch
        }
    }

    suspend fun unsubscribePlayer(team: String, idMatch: Int) : Boolean{
        val result = unsubscribePlayerFromMatch(application, _currentUser.value!!, team, idMatch)
        return result != null
    }

    fun sendParticipationRequest(idMatch: Int) {
        viewModelScope.launch {
            aggiungiNotificaRichiesta(
                context = application,
                titolo = "Richiesta ricevuta",
                testo = "Hai ricevuto una nuova richiesta di partecipazione",
                destinatario = _match.value!!.creatore,
                titoloEn = "Request received",
                testoEn = "You have received a request to participate",
                idPartita = idMatch,
                richiedente = _currentUser.value!!
            )
            val fcmToken = prendiTokenFCMDaEmail(application, _match.value!!.creatore)
            if (fcmToken != null) {
                inviaNotificaPush(
                    "Richiesta ricevuta",
                    "Hai ricevuto una nuova richiesta di partecipazione",
                    fcmToken
                )
            }
        }
    }

    fun deleteGame(idMatch: Int) {
        viewModelScope.launch {
            deleteMatch(application, idMatch)
            sendEliminationNotification(_giocatoriSquadra1.value!!)
            sendEliminationNotification(_giocatoriSquadra2.value!!)
        }
    }

    private suspend fun sendEliminationNotification(players: List<GiocatoreWrapper>) {
        val data = _match.value!!.dataOra.toLocalDateTime()
        players.forEach {
            if (it.utente.email != _currentUser.value) {
                aggiungiNotificaEliminazioneDaAdmin(
                    context = application,
                    titolo = "Partita annullata",
                    testo = "La partita del ${data.date} alle ${data.hour} presso ${_match.value!!.nomeCampo} " +
                            "(${_match.value!!.citta}) è stata cancellata dall'amministratore.",
                    destinatario = it.utente.email,
                    titoloEn = "Match cancelled",
                    testoEn = "The match on ${data.date} at ${data.hour} at ${_match.value!!.nomeCampo} " +
                            "(${_match.value!!.citta}) has been cancelled by the administrator."
                )
                val fcmToken = prendiTokenFCMDaEmail(application, it.utente.email)
                if (fcmToken != null) {
                    inviaNotificaPush(
                        "Partita annullata",
                        "La partita del ${data.date} alle ${data.hour} presso ${_match.value!!.nomeCampo} " +
                                "(${_match.value!!.citta}) è stata cancellata dall'amministratore.",
                        fcmToken
                    )
                }
            }
        }
    }
}

class MatchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}