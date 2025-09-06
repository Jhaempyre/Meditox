package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    private val _isRegistered = MutableStateFlow<Boolean?>(null)
    val isRegistered: StateFlow<Boolean?> = _isRegistered

    private val _isBusinessRegistered = MutableStateFlow<Boolean?>(null)
    val isBusinessRegistered: StateFlow<Boolean?> = _isBusinessRegistered

    init {
        Log.d("SplashViewModel", "Initializing SplashViewModel")

        viewModelScope.launch {
            Log.d("SplashViewModel", "Starting to collect isLoggedIn")
            DataStoreManager.isLoggedIn(application).collect { value ->
                Log.d("SplashViewModel", "isLoggedIn collected: $value")
                _isLoggedIn.value = value
            }
        }

        viewModelScope.launch {
            Log.d("SplashViewModel", "Starting to collect isRegistered")
            DataStoreManager.isRegistered(application).collect { value ->
                Log.d("SplashViewModel", "isRegistered collected: $value")
                _isRegistered.value = value
            }
        }

        viewModelScope.launch {
            Log.d("SplashViewModel", "Starting to collect isBusinessRegistered")
            DataStoreManager.isBusinessRegistered(application).collect { value ->
                Log.d("SplashViewModel", "isBusinessRegistered collected: $value")
                _isBusinessRegistered.value = value
            }
        }
    }
}