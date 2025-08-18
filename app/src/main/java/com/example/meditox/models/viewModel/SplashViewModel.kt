package com.example.meditox.models.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application){

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    private val _isRegistered = MutableStateFlow<Boolean?>(null)
    val isRegistered: StateFlow<Boolean?> = _isRegistered


    init{
        viewModelScope.launch {
            DataStoreManager.isLoggedIn(application).collect {
                _isLoggedIn.value = it
            }
        }
        viewModelScope.launch {
            DataStoreManager.isRegistered(application).collect {
                _isRegistered.value = it
            }
        }
    }
}