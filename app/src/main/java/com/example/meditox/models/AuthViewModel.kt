package com.example.meditox.models

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _loginResult = mutableStateOf<ApiResult<AuthResponse>?>(null)
    val loginResult: State<ApiResult<AuthResponse>?> = _loginResult


    fun sendOtp(phone: String){
        viewModelScope.launch{
            try {
                _loginResult.value = ApiResult.Loading
                val response = ApiClient.userApiService.sendOtp(AuthRequest(phone))
                if(response.isSuccessful){
                    _loginResult.value = ApiResult.Success(response.body()!!)
                }else{
                    _loginResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Login failed")
                }


            }catch(e:Exception){
                _loginResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            }

        }
    }

    fun resetLoginState() {
        _loginResult.value = null
    }
}