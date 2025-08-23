package com.example.meditox.models.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.userRegistration.UserRegistrationRequest
import com.example.meditox.models.userRegistration.UserRegistrationResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(application: Application) : AndroidViewModel(application)  {
    private val _isRegistered = MutableStateFlow<Boolean?>(null)
    val isRegistered: StateFlow<Boolean?> = _isRegistered

//    private val _registrationResponse = MutableStateFlow<UserRegistrationResponse?>(null)
//    val registrationResponse: StateFlow<UserRegistrationResponse?> = _registrationResponse

    private val registerResult = MutableStateFlow<ApiResult<UserRegistrationResponse>?>(null)
    val registrationResult: StateFlow<ApiResult<UserRegistrationResponse>?> = registerResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun registerUser(userDetails: UserRegistrationRequest){
        viewModelScope.launch {
            try{

                registerResult.value = ApiResult.Loading
                val response = ApiClient.userApiService.registerUser(userDetails)
                if(response.isSuccessful){
                    registerResult.value = ApiResult.Success(response.body()!!)
                    _isRegistered.value = true
                }else{
                    registerResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Registration failed")

                }

            }catch(e:Exception){
                registerResult.value= ApiResult.Error(e.message ?: "Unexpected error")
            }
        }
    }


}