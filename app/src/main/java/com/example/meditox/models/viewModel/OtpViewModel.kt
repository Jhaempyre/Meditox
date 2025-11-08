package com.example.meditox.models.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.otpVerication.VerifyOtpRequest
import com.example.meditox.models.otpVerication.VerifyOtpResponse
import com.example.meditox.models.ShopDetails
import com.example.meditox.models.ApiResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import android.util.Log


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

    private val _shopDetailsResult = MutableStateFlow<ApiResult<Response<ApiResponse<ShopDetails>>>?>(null)
    val shopDetailsResult: StateFlow<ApiResult<Response<ApiResponse<ShopDetails>>>?> = _shopDetailsResult.asStateFlow()



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

    fun fetchShopDetails(userId: String) {
        viewModelScope.launch {
            try {
                _shopDetailsResult.value = ApiResult.Loading
                Log.d("OtpViewModel", "Fetching shop details for user: $userId")
                val response = apiService.getShopDetails(userId)
                if (response.isSuccessful) {
                    _shopDetailsResult.value = ApiResult.Success(response)
                    Log.d("OtpViewModel", "Shop details fetched successfully: ${response.body()}")
                } else {
                    _shopDetailsResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Failed to fetch shop details")
                    Log.e("OtpViewModel", "Failed to fetch shop details: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _shopDetailsResult.value = ApiResult.Error(e.message ?: "Unexpected error while fetching shop details")
                Log.e("OtpViewModel", "Exception while fetching shop details: ${e.message}", e)
            }
        }
    }

    fun resetShopDetailsState() {
        _shopDetailsResult.value = null
    }

}