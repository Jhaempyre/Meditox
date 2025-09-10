package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.User
import com.example.meditox.models.businessRegistration.BusinessRegistrationRequest
import com.example.meditox.models.businessRegistration.BusinessRegistrationResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessRegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationResult = MutableStateFlow<ApiResult<BusinessRegistrationResponse>?>(null)
    val registrationResult: MutableStateFlow<ApiResult<BusinessRegistrationResponse>?> = _registrationResult

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _currentLocation = MutableStateFlow<LocationUtils.LocationResult?>(null)
    val currentLocation: StateFlow<LocationUtils.LocationResult?> = _currentLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _isLocationLoading = MutableStateFlow(false)
    val isLocationLoading: StateFlow<Boolean> = _isLocationLoading.asStateFlow()

    private val apiService = ApiClient.createUserApiService(application)

    init {
        viewModelScope.launch {
            DataStoreManager.getUserData(application).collect {
                _user.value= it
                Log.d("BusinessRegistrationModel", "User data loaded: $it")
            }
        }
    }


    fun registerBusiness(businessDetails: BusinessRegistrationRequest) {
        viewModelScope.launch {
            try {
                _registrationResult.value = ApiResult.Loading
                
                // Set the user_id from the current user
                val currentUser = _user.value
                if (currentUser?.id == null) {
                    _registrationResult.value = ApiResult.Error("User not found. Please login again.")
                    return@launch
                }
                
                val updatedBusinessDetails = businessDetails.copy(user_id = currentUser.id)
                
                val response = apiService.registerBusiness(updatedBusinessDetails)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("yaha hu","aa gya")

                    if (body != null && body.success) {
                        _registrationResult.value = ApiResult.Success(body)
                        // Update business registration status in DataStore
                        DataStoreManager.setIsBusinessRegistered(getApplication(), true)
                        Log.d("BusinessRegistrationModel", "Business registration successful")
                    } else {
                        _registrationResult.value = ApiResult.Error(body?.message ?: "Business registration failed")
                    }
                } else {
                    _registrationResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Business registration failed")
                }
                
            } catch (e: Exception) {
                _registrationResult.value = ApiResult.Error(e.message ?: "Unexpected error occurred")
                Log.e("BusinessRegistrationModel", "Exception during business registration", e)
            }
        }
    }

    /**
     * Fetch current location of the user
     */
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _isLocationLoading.value = true
            _locationError.value = null
            
            try {
                when (val locationResponse = LocationUtils.getCurrentLocation(getApplication())) {
                    is LocationUtils.LocationResponse.Success -> {
                        _currentLocation.value = locationResponse.location
                        _locationError.value = null
                        Log.d("BusinessRegistrationModel", "Location fetched: ${LocationUtils.formatLocation(locationResponse.location)}")
                    }
                    is LocationUtils.LocationResponse.Error -> {
                        _locationError.value = locationResponse.message
                        Log.e("BusinessRegistrationModel", "Location error: ${locationResponse.message}")
                    }
                    is LocationUtils.LocationResponse.PermissionDenied -> {
                        _locationError.value = "Location permission denied. Please grant location permission to auto-fill coordinates."
                        Log.w("BusinessRegistrationModel", "Location permission denied")
                    }
                    is LocationUtils.LocationResponse.LocationDisabled -> {
                        _locationError.value = "Location services disabled. Please enable location services."
                        Log.w("BusinessRegistrationModel", "Location services disabled")
                    }
                }
            } catch (e: Exception) {
                _locationError.value = "Failed to get location: ${e.message}"
                Log.e("BusinessRegistrationModel", "Exception fetching location", e)
            } finally {
                _isLocationLoading.value = false
            }
        }
    }

    /**
     * Register business with automatic location fetching
     */
    fun registerBusinessWithLocation(businessDetails: BusinessRegistrationRequest) {
        viewModelScope.launch {
            try {
                _registrationResult.value = ApiResult.Loading
                
                // Set the user_id from the current user
                val currentUser = _user.value
                if (currentUser?.id == null) {
                    _registrationResult.value = ApiResult.Error("User not found. Please login again.")
                    return@launch
                }
                
                // Fetch location if not already available and coordinates are empty
                var updatedBusinessDetails = businessDetails.copy(user_id = currentUser.id)
                
                if ((businessDetails.latitude.isNullOrEmpty() || businessDetails.longitude.isNullOrEmpty()) && 
                    LocationUtils.hasLocationPermissions(getApplication())) {
                    
                    Log.d("BusinessRegistrationModel", "Fetching location for business registration")
                    
                    when (val locationResponse = LocationUtils.getCurrentLocationHighAccuracy(getApplication())) {
                        is LocationUtils.LocationResponse.Success -> {
                            val (lat, lng) = LocationUtils.locationToString(
                                locationResponse.location.latitude,
                                locationResponse.location.longitude
                            )
                            updatedBusinessDetails = updatedBusinessDetails.copy(
                                latitude = lat,
                                longitude = lng
                            )
                            _currentLocation.value = locationResponse.location
                            Log.d("BusinessRegistrationModel", "Location added to business registration: $lat, $lng")
                        }
                        is LocationUtils.LocationResponse.Error -> {
                            Log.w("BusinessRegistrationModel", "Could not fetch location for registration: ${locationResponse.message}")
                            // Continue with registration without location
                        }
                        is LocationUtils.LocationResponse.PermissionDenied -> {
                            Log.w("BusinessRegistrationModel", "Location permission denied during registration")
                            // Continue with registration without location
                        }
                        is LocationUtils.LocationResponse.LocationDisabled -> {
                            Log.w("BusinessRegistrationModel", "Location services disabled during registration")
                            // Continue with registration without location
                        }
                    }
                }
                
                val response = apiService.registerBusiness(updatedBusinessDetails)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        _registrationResult.value = ApiResult.Success(body)
                        // Update business registration status in DataStore
                        DataStoreManager.setIsBusinessRegistered(getApplication(), true)
                        Log.d("BusinessRegistrationModel", "Business registration successful")
                    } else {
                        _registrationResult.value = ApiResult.Error(body?.message ?: "Business registration failed")
                    }
                } else {
                    _registrationResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Business registration failed")
                }
                
            } catch (e: Exception) {
                _registrationResult.value = ApiResult.Error(e.message ?: "Unexpected error occurred")
                Log.e("BusinessRegistrationModel", "Exception during business registration", e)
            }
        }
    }

    fun resetRegistrationState() {
        _registrationResult.value = null
    }

    fun resetLocationState() {
        _currentLocation.value = null
        _locationError.value = null
        _isLocationLoading.value = false
    }

}