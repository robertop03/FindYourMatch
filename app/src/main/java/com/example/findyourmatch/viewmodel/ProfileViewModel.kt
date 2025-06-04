package com.example.findyourmatch.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.user.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _user = MutableStateFlow<AnagraficaUtente?>(null)
    private val _userAddress = MutableStateFlow<IndirizzoUtente?>(null)
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    private val _numRewardsAchieved = MutableStateFlow<Int?>(null)
    private val _maxRewardsAchieved = MutableStateFlow<List<MaxObiettivoRaggiunto>?>(null)
    private val _playedGames = MutableStateFlow<List<PartiteGiocateUtente>?>(null)
    private val _stats = MutableStateFlow<StatsUtente?>(null)
    private val _gamesStats = MutableStateFlow<LinkedHashMap<String, StatsUtentePartita?>>(LinkedHashMap())
    val user = _user
    val userAddress = _userAddress
    val profileImageUri: StateFlow<Uri?> = _profileImageUri
    val numRewardsAchieved = _numRewardsAchieved
    val maxRewardsAchieved = _maxRewardsAchieved
    val playedGames = _playedGames
    val stats = _stats
    val gamesStatsMap = _gamesStats

    init {
        ricaricaUtente()
    }

    fun ricaricaUtente() {
        viewModelScope.launch {
            _user.value = getUserInfo(application)
            _userAddress.value = getIndirizzoUtente(application)
            _user.value?.let { u ->
                val url = Uri.parse("https://ugtxgylfzblkvudpnagi.supabase.co/storage/v1/object/public/profilephotos/${u.email}.jpg")
                _profileImageUri.value = if (checkIfImageExists(url.toString())) url else null
                _numRewardsAchieved.value = calculateNumOfRewardsAchieved(application, _user.value?.email!!)
                if (_numRewardsAchieved.value != null) {
                    _maxRewardsAchieved.value = getMaxRewards(application, _user.value?.email!!)
                }
                _playedGames.value = getPlayedGames(application, _user.value?.email!!)
                _stats.value = getStats(application, _user.value?.email!!)
                _playedGames.value?.take(5)?.forEach {
                    val dateString = LocalDateTime.parse(it.dataOra).date.toString()
                    _gamesStats.value[dateString] =
                        getUserStatsInMatch(application, _user.value?.email!!, it.id.toLong())
                            ?: StatsUtentePartita(0, 0)
                }
            }
        }
    }

    fun saveLocalProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri
        viewModelScope.launch {
            updateProfileImage(application, _user.value!!.email, _profileImageUri.value!!)
        }
    }

    fun editProfile(newName: String, newLastName: String, newNation: String, newProvince: String,
        newCity: String, newStreet: String, newHouseNumber: String) {
        val toUpdateMap = mutableMapOf<String, String>()
        if (newName != _user.value!!.nome) toUpdateMap["nome"] = newName
        if (newLastName != _user.value!!.cognome) toUpdateMap["cognome"] = newLastName
        if (newNation != _userAddress.value!!.stato) toUpdateMap["stato"] = newNation
        if (newProvince != _userAddress.value!!.provincia) toUpdateMap["provincia"] = newProvince
        if (newCity != _userAddress.value!!.citta) toUpdateMap["citta"] = newCity
        if (newStreet != _userAddress.value!!.via) toUpdateMap["via"] = newStreet
        if (newHouseNumber != _userAddress.value!!.civico) toUpdateMap["civico"] = newHouseNumber
        viewModelScope.launch {
            updateProfile(application, toUpdateMap, _user.value!!.email)
            ricaricaUtente()
        }
    }
}

class ProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}