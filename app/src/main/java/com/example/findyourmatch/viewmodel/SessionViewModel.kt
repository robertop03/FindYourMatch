package com.example.findyourmatch.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.user.SessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            _isLoggedIn.value = SessionManager.isLoggedInFlow(context).first()
        }
    }

    fun updateLoginStatus(context: Context, isLogged: Boolean) {
        _isLoggedIn.value = isLogged
        viewModelScope.launch {
            SessionManager.setLoggedIn(context, isLogged)
        }
    }

}

class SessionViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}