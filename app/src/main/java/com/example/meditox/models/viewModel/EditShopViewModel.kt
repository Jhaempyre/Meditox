package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.ApiResponse
import com.example.meditox.models.ShopDetails
import com.example.meditox.models.ShopDetailsEdit.UpdateShopDetailsRequest
import com.example.meditox.models.ShopDetailsEdit.UpdateShopDetailsResponse
import com.example.meditox.models.User
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDateTime

class EditShopViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _updateResult = MutableStateFlow<ApiResult<Response<ApiResponse<UpdateShopDetailsResponse>>>?>(null)
    val updateResult: StateFlow<ApiResult<Response<ApiResponse<UpdateShopDetailsResponse>>>?> = _updateResult.asStateFlow()
    
    private val _shopDetails = MutableStateFlow<ShopDetails?>(null)
    val shopDetails: StateFlow<ShopDetails?> = _shopDetails.asStateFlow()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<LocationUtils.LocationResult?>(null)
    val currentLocation: StateFlow<LocationUtils.LocationResult?> = _currentLocation.asStateFlow()
    
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()
    
    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()
    
    private val _useCurrentLocation = MutableStateFlow(true)
    val useCurrentLocation: StateFlow<Boolean> = _useCurrentLocation.asStateFlow()
    
    private val apiService = ApiClient.createUserApiService(application)
    
    init {
        viewModelScope.launch {
            // Load user data
            DataStoreManager.getUserData(application).collect {
                _user.value = it
                Log.d("EditShopViewModel", "User data loaded: $it")
            }
        }
        
        viewModelScope.launch {
            // Load shop details
            DataStoreManager.getShopDetails(application).collect {
                _shopDetails.value = it
                Log.d("EditShopViewModel", "Shop details loaded: $it")
                // Set current location from shop details
                if (it?.latitude != null && it.longitude != null) {
                    try {
                        val lat = it.latitude.toDouble()
                        val lng = it.longitude.toDouble()
                        _currentLocation.value = LocationUtils.LocationResult(lat, lng)
                    } catch (e: Exception) {
                        Log.e("EditShopViewModel", "Error parsing saved location", e)
                    }
                }
            }
        }
    }
    
    fun toggleLocationMode() {
        _useCurrentLocation.value = !_useCurrentLocation.value
        if (!_useCurrentLocation.value) {
            // If switching to keep current location, reset location states
            resetLocationState()
            // Set location back to shop's saved location
            val shop = _shopDetails.value
            if (shop?.latitude != null && shop.longitude != null) {
                try {
                    val lat = shop.latitude.toDouble()
                    val lng = shop.longitude.toDouble()
                    _currentLocation.value = LocationUtils.LocationResult(lat, lng)
                } catch (e: Exception) {
                    Log.e("EditShopViewModel", "Error restoring saved location", e)
                }
            }
        }
    }
    
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _isLocationLoading.value = true
            _locationError.value = null
            
            try {
                when (val locationResponse = LocationUtils.getCurrentLocation(getApplication())) {
                    is LocationUtils.LocationResponse.Success -> {
                        _currentLocation.value = locationResponse.location
                        _locationError.value = null
                        Log.d("EditShopViewModel", "Location fetched: ${LocationUtils.formatLocation(locationResponse.location)}")
                    }
                    is LocationUtils.LocationResponse.Error -> {
                        _locationError.value = locationResponse.message
                        Log.e("EditShopViewModel", "Location error: ${locationResponse.message}")
                    }
                    is LocationUtils.LocationResponse.PermissionDenied -> {
                        _locationError.value = "Location permission denied. Please grant location permission to update coordinates."
                        Log.w("EditShopViewModel", "Location permission denied")
                    }
                    is LocationUtils.LocationResponse.LocationDisabled -> {
                        _locationError.value = "Location services disabled. Please enable location services."
                        Log.w("EditShopViewModel", "Location services disabled")
                    }
                }
            } catch (e: Exception) {
                _locationError.value = "Failed to get location: ${e.message}"
                Log.e("EditShopViewModel", "Exception fetching location", e)
            } finally {
                _isLocationLoading.value = false
            }
        }
    }
    
    fun updateShopDetails(updateRequest: UpdateShopDetailsRequest) {
        viewModelScope.launch {
            try {
                _updateResult.value = ApiResult.Loading
                
                val currentUser = _user.value
                val theShop = _shopDetails.value
                if (currentUser?.id == null) {
                    _updateResult.value = ApiResult.Error("User not found. Please login again.")
                    return@launch
                }
                
                val updatedRequest = updateRequest.copy(
                    user_id = currentUser.id,
                    )
                
                val response = apiService.updateShopDetails(theShop?.id.toString(), updatedRequest)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {

                        _updateResult.value = ApiResult.Success(response)
                        // Update shop details in DataStore
                        // Convert UpdateShopDetailsResponse to ShopDetails and save to DataStore
                        body.data?.let { updateResponse ->
                            Log.d("EditShopViewModel", "Received update response: $updateResponse")
                            
                            // Convert UpdateShopDetailsResponse to ShopDetails for DataStore
                            val updatedShop = ShopDetails(
                                id = updateResponse.id,
                                shopName = updateResponse.shopName,
                                licenseNumber = updateResponse.licenseNumber,
                                gstNumber = updateResponse.gstNumber,
                                address = updateResponse.address,
                                city = updateResponse.city,
                                state = updateResponse.state,
                                pinCode = updateResponse.pinCode,
                                contactPhone = updateResponse.contactPhone,
                                contactEmail = updateResponse.contactEmail,
                                latitude = updateResponse.latitude,
                                longitude = updateResponse.longitude,
                                shopStatus = updateResponse.shopStatus,
                                // Map new fields
                                chemistId = updateResponse.chemistId,
                                ownerName = updateResponse.ownerName,
                                stateCode = updateResponse.stateCode,
                                addressLine2 = updateResponse.addressLine2,
                                bankAccountNumber = updateResponse.bankAccountNumber,
                                bankIfscCode = updateResponse.bankIfscCode,
                                bankName = updateResponse.bankName,
                                upiId = updateResponse.upiId,
                                isActive = updateResponse.isActive,
                                createdAt = updateResponse.createdAt ?: _shopDetails.value?.createdAt,
                                meditoxUpdatedAt = updateResponse.meditoxUpdatedAt
                            )
                            
                            DataStoreManager.saveShopDetails(getApplication(), updatedShop)
                            Log.d("EditShopViewModel", "Shop details successfully saved to DataStore: $updatedShop")
                        } ?: Log.e("EditShopViewModel", "Response data is null")
                        Log.d("EditShopViewModel", "Shop details update successful")
                    } else {
                        _updateResult.value = ApiResult.Error(body?.message ?: "Shop details update failed")
                    }
                } else {
                    _updateResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Shop details update failed")
                }
                
            } catch (e: Exception) {
                _updateResult.value = ApiResult.Error(e.message ?: "Unexpected error occurred")
                Log.e("EditShopViewModel", "Exception during shop details update", e)
            }
        }
    }
    
    fun resetUpdateState() {
        _updateResult.value = null
    }
    
    fun resetLocationState() {
        _locationError.value = null
        _isLocationLoading.value = false
    }
}