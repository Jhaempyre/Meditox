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
import com.example.meditox.models.subscription.SubscriptionApiResponse


class OtpViewModel(application: Application) : AndroidViewModel(application) {

    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber

    //get userid from datastore
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val apiService = ApiClient.createUserApiService(application)


    init {
        viewModelScope.launch {
            DataStoreManager.getPhoneNumber(application).collect {
                _phoneNumber.value = it
            }
        }
        viewModelScope.launch {
            DataStoreManager.getUserData(application).collect {
                _userId.value = it?.id.toString()
            }
        }
    }

    private val _otpResult = MutableStateFlow<ApiResult<Response<VerifyOtpResponse>>?>(null)
    val otpResult: StateFlow<ApiResult<Response<VerifyOtpResponse>>?> = _otpResult.asStateFlow()

    private val _shopDetailsResult =
        MutableStateFlow<ApiResult<Response<ApiResponse<ShopDetails>>>?>(null)
    val shopDetailsResult: StateFlow<ApiResult<Response<ApiResponse<ShopDetails>>>?> =
        _shopDetailsResult.asStateFlow()


    private val _subscriptionDetailResult =
        MutableStateFlow<ApiResult<Response<ApiResponse<SubscriptionApiResponse>>>?>(null)
    val subscriptionDetailResult: StateFlow<ApiResult<Response<ApiResponse<SubscriptionApiResponse>>>?> =
        _subscriptionDetailResult.asStateFlow()


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
            } catch (e: Exception) {
                _otpResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            }

        }
    }

    fun resetOtpVerificationState() {
        _otpResult.value = null
    }

    fun fetchShopDetails(userId: String) {
        viewModelScope.launch {
            try {
                _shopDetailsResult.value = ApiResult.Loading
                Log.d("OtpViewModel", "Fetching shop details for user: $userId")
                val response = apiService.getShopDetails(userId)
                Log.d("OtpViewModel", "API response may be succedded")
                if (response.isSuccessful) {
                    Log.d("OtpViewModel", "Tried this one ")
                    _shopDetailsResult.value = ApiResult.Success(response)
                    Log.d("OtpViewModel", "Shop details fetched successfully: ${response.body()}")
                } else {
                    _shopDetailsResult.value = ApiResult.Error(
                        response.errorBody()?.string() ?: "Failed to fetch shop details"
                    )
                    Log.e(
                        "OtpViewModel",
                        "Failed to fetch shop details: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                _shopDetailsResult.value =
                    ApiResult.Error(e.message ?: "Unexpected error while fetching shop details")
                Log.e("OtpViewModel", "Exception while fetching shop details: ${e.message}", e)
            }
        }
    }

    fun resetShopDetailsState() {
        _shopDetailsResult.value = null
    }

    //do good logging after each line
    fun getSubscriptionDetails(userId: String) {
        viewModelScope.launch {
            try {
                _subscriptionDetailResult.value = ApiResult.Loading
                Log.d("OtpViewModel", "Fetching subscription details for businessid: $userId")
                val response = apiService.getUserSubscription(userId)
                Log.d("OtpViewModel", "API response may be succedded")
                if (response.isSuccessful) {
                    _subscriptionDetailResult.value = ApiResult.Success(response)
                    Log.d(
                        "OtpViewModel",
                        "Subscription details fetched stringifying successfully: ${
                            response.body()?.toString()
                        }"
                    )

                    Log.d(
                        "OtpViewModel",
                        "Subscription details fetched successfully: ${response.body()}"
                    )

                } else {
                    _subscriptionDetailResult.value = ApiResult.Error(
                        response.errorBody()?.string() ?: "Failed to fetch subscription details"
                    )
                    Log.e(
                        "OtpViewModel",
                        "Failed to fetch subscription details: ${response.errorBody()?.string()}"
                    )
                }

            } catch (e: Exception) {
                _subscriptionDetailResult.value = ApiResult.Error(
                    e.message ?: "Unexpected error while fetching subscription details"
                )
                Log.e(
                    "OtpViewModel",
                    "Exception while fetching subscription details: ${e.message}",
                    e
                )
            }
        }


    }

    fun resetgetSubscriptionDetails() {
        _subscriptionDetailResult.value = null
    }

}