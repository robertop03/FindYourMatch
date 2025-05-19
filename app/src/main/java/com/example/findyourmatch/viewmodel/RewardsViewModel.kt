package com.example.findyourmatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.rewards.RewardAchievement
import com.example.findyourmatch.data.rewards.caricaRaggiungimenti
import com.example.findyourmatch.utils.getLoggedUserEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val _achievements = MutableStateFlow<List<RewardAchievement>>(emptyList())
    val achievements: StateFlow<List<RewardAchievement>> = _achievements

    fun loadAchievements() {
        viewModelScope.launch {
            val email = getLoggedUserEmail(getApplication()) ?: return@launch
            _achievements.value = caricaRaggiungimenti(getApplication(), email)
        }
    }
}