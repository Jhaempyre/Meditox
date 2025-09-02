package com.example.meditox.models.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.userRegistration.UserRegistrationRequest
import com.example.meditox.models.userRegistration.UserRegistrationResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
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

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber


    init {
        viewModelScope.launch {
            DataStoreManager.getPhoneNumber(application).collect {
                _phoneNumber.value = it
            }
        }
    }


    fun registerUser(userDetails: UserRegistrationRequest){
        viewModelScope.launch {
            try{

                registerResult.value = ApiResult.Loading
                userDetails.phone=phoneNumber.value!!

                val response = ApiClient.userApiService.registerUser(userDetails)
                if(response.isSuccessful){
                    val body = response.body()

                    if (body != null && body.success) {
                        registerResult.value = ApiResult.Success(body)
                        _isRegistered.value = true
                    }
                }else{
                    registerResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Registration failed")

                }

            }catch(e:Exception){
                registerResult.value= ApiResult.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun resetRegistrationState() {
        _isRegistered.value = null
        registerResult.value = null

    }

}