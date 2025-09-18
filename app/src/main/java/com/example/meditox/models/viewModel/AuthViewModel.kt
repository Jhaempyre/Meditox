package com.example.meditox.models.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.meditox.models.auth.AuthRequest
import com.example.meditox.models.auth.AuthResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _loginResult = MutableStateFlow<ApiResult<AuthResponse>?>(null)
    val loginResult: StateFlow<ApiResult<AuthResponse>?> = _loginResult.asStateFlow()
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()
    private val apiService = ApiClient.createUserApiService(application)

    init {
        viewModelScope.launch {
            DataStoreManager.getPhoneNumber(application).collect {
                _phoneNumber.value = it
            }
        }
    }

    fun sendOtp(phone: String){
        viewModelScope.launch{
            try {
                _loginResult.value = ApiResult.Loading
                val response = apiService.sendOtp(AuthRequest(phone))
                if(response.isSuccessful){
                    _loginResult.value = ApiResult.Success(response.body()!!)
                }else{
                    _loginResult.value = ApiResult.Error(response.errorBody()?.string() ?: "OTP sending failed")
                }


            }catch(e:Exception){
                _loginResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            }

        }
    }

    fun savePhoneInDataStore( phone: String) {
        viewModelScope.launch {
            DataStoreManager.savePhoneNumber(getApplication(), phone)
        }
    }

    fun resetLoginState() {
        _loginResult.value = null
    }
}