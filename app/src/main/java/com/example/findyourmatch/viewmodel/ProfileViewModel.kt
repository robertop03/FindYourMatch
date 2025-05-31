package com.example.findyourmatch.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.findyourmatch.data.user.AnagraficaUtente
import com.example.findyourmatch.data.user.IndirizzoUtente
import com.example.findyourmatch.data.user.getIndirizzoUtente
import com.example.findyourmatch.data.user.getUserInfo
import com.example.findyourmatch.data.user.updateProfileImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _user = MutableStateFlow<AnagraficaUtente?>(null)
    private val _userAddress = MutableStateFlow<IndirizzoUtente?>(null)
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val user = _user
    val userAddress = _userAddress
    val profileImageUri: StateFlow<Uri?> = _profileImageUri

    init {
        viewModelScope.launch {
            _user.value = getUserInfo(application)
            _userAddress.value = getIndirizzoUtente(application)

            _user.value?.pathFotoProfilo?.let { path ->
                // Se il bucket è pubblico
                val url = "https://ugtxgylfzblkvudpnagi.supabase.co/storage/v1/object/public/profilephotos/$path"
                _profileImageUri.value = Uri.parse(url)
            }
        }
    }

    fun saveLocalProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri
        viewModelScope.launch {
            updateProfileImage(application, _user.value!!.email, _profileImageUri.value!!)
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