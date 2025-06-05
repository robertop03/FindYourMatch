package com.example.findyourmatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.findyourmatch.data.user.Recensione
import com.example.findyourmatch.data.user.getLoggedUserEmail
import com.example.findyourmatch.data.user.getReviews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ReviewsViewModel(application: Application) : AndroidViewModel(application) {
    private val _reviews = MutableStateFlow<List<Recensione>?>(mutableListOf())
    val reviews = _reviews

    init {
        loadReviews()
    }

    fun loadReviews(userEmail: String? = null) {
        viewModelScope.launch {
            _reviews.value = getReviews(application, userEmail ?: getLoggedUserEmail(application)!!)
        }
    }
}

class ReviewsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}