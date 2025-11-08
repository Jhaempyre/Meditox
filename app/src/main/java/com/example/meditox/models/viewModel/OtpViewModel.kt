package com.example.meditox.models.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.otpVerication.VerifyOtpRequest
import com.example.meditox.models.otpVerication.VerifyOtpResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response


class OtpViewModel(application: Application) : AndroidViewModel(application) {

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber
    private val apiService = ApiClient.createUserApiService(application)


    init {
        viewModelScope.launch {
            DataStoreManager.getPhoneNumber(application).collect {
                _phoneNumber.value = it
            }
        }
    }

    private val _otpResult = MutableStateFlow<ApiResult<Response<VerifyOtpResponse>>?>(null)
    val otpResult: StateFlow<ApiResult<Response<VerifyOtpResponse>>?> = _otpResult.asStateFlow()



    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            try {
                _otpResult.value = ApiResult.Loading
                val response =
                    apiService.verifyOtp(VerifyOtpRequest(_phoneNumber.value!!, otp))
                if (response.isSuccessful) {
                    _otpResult.value = ApiResult.Success(response)
                } else {
                    _otpResult.value =
                        ApiResult.Error(response.errorBody()?.string() ?: "Login failed")
                }
            }catch (e:Exception){
                _otpResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            }

        }
    }

    fun resetOtpVerificationState(){
        _otpResult.value = null
    }

}