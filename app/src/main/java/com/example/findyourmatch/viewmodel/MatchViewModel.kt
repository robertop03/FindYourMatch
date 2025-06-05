package com.example.findyourmatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.match.GiocatoreWrapper
import com.example.findyourmatch.data.match.PartitaMostrata
import com.example.findyourmatch.data.match.getMatch
import com.example.findyourmatch.data.match.getTeamPlayers
import com.example.findyourmatch.data.user.getLoggedUserEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MatchViewModel(application: Application) : AndroidViewModel(application) {
    private val _match = MutableStateFlow<PartitaMostrata?>(null)
    private val _giocatoriSquadra1 = MutableStateFlow<List<GiocatoreWrapper>?>(null)
    private val _giocatoriSquadra2 = MutableStateFlow<List<GiocatoreWrapper>?>(null)
    private val _currentUser = MutableStateFlow<String?>(null)
    val match = _match
    val giocatoriSquadra1 = _giocatoriSquadra1
    val giocatoriSquadra2 = _giocatoriSquadra2
    val currentUser = _currentUser

    fun loadMatch(idMatch: Int) {
        viewModelScope.launch {
            _currentUser.value = getLoggedUserEmail(application)
            _match.value = getMatch(application, idMatch)
            _giocatoriSquadra1.value = _match.value?.let { getTeamPlayers(application, it.squadra1, idMatch) }
            _giocatoriSquadra2.value = _match.value?.let { getTeamPlayers(application, it.squadra2, idMatch) }
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