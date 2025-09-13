package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.subscription.SubscriptionRequest
import com.example.meditox.models.subscription.SubscriptionResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val _subscriptionResult = MutableStateFlow<ApiResult<SubscriptionResponse>?>(null)
    val subscriptionResult: StateFlow<ApiResult<SubscriptionResponse>?> = _subscriptionResult.asStateFlow()
    
    private val _selectedPlanId = MutableStateFlow<String?>(null)
    val selectedPlanId: StateFlow<String?> = _selectedPlanId.asStateFlow()
    
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
    
    fun setPlanId(planId: String) {
        _selectedPlanId.value = planId
    }
    
    fun createSubscription(planId: String) {
        viewModelScope.launch {
            try {
                Log.d("SubscriptionViewModel", "Starting subscription creation for planId: $planId")
                _subscriptionResult.value = ApiResult.Loading
                
                val subscriptionRequest = SubscriptionRequest(planId = planId)
                Log.d("SubscriptionViewModel", "Subscription request: $subscriptionRequest")
                
                Log.d("SubscriptionViewModel", "Making API call...")
                val response = apiService.createSubscription(subscriptionRequest)
                Log.d("SubscriptionViewModel", "API call completed")
                
                Log.d("SubscriptionViewModel", "Response code: ${response.code()}")
                Log.d("SubscriptionViewModel", "Response headers: ${response.headers()}")
                Log.d("SubscriptionViewModel", "Response success: ${response.isSuccessful}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("SubscriptionViewModel", "Response body: $body")
                    
                    if (body != null) {
                        Log.d("SubscriptionViewModel", "Success - Subscription ID: ${body.id}")
                        Log.d("SubscriptionViewModel", "Success - Status: ${body.status}")
                        Log.d("SubscriptionViewModel", "Success - Short URL: ${body.shortUrl}")
                        _subscriptionResult.value = ApiResult.Success(body)
                    } else {
                        Log.e("SubscriptionViewModel", "Response body is null")
                        _subscriptionResult.value = ApiResult.Error("Empty response from server")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("SubscriptionViewModel", "API Error code: ${response.code()}")
                    Log.e("SubscriptionViewModel", "API Error body: $errorBody")
                    _subscriptionResult.value = ApiResult.Error(errorBody ?: "Subscription creation failed")
                }
            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Exception during subscription creation", e)
                Log.e("SubscriptionViewModel", "Exception type: ${e.javaClass.simpleName}")
                Log.e("SubscriptionViewModel", "Exception message: ${e.message}")
                _subscriptionResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            }
        }
    }
    
    fun resetSubscriptionState() {
        _subscriptionResult.value = null
        _selectedPlanId.value = null
    }
}