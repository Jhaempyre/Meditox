package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.ApiResponse
import com.example.meditox.models.EmergencyContact
import com.example.meditox.models.User
import com.example.meditox.models.userUpdate.UpdateUserRequest
import com.example.meditox.models.userUpdate.UpdateUserResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class EditUserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiClient.createUserApiService(application)
    
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()
    
    private val _updateResult = MutableStateFlow<ApiResult<Response<ApiResponse<UpdateUserResponse>>>?>(null)
    val updateResult: StateFlow<ApiResult<Response<ApiResponse<UpdateUserResponse>>>?> = _updateResult.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                DataStoreManager.getUserData(getApplication()).collect { user ->
                    _userData.value = user
                    Log.d("EditUserProfileVM", "User data loaded: $user")
                }
            } catch (e: Exception) {
                Log.e("EditUserProfileVM", "Error loading user data: ${e.message}", e)
            }
        }
    }

    fun updateUserProfile(
        name: String,
        abhaId: String,
        gender: String,
        bloodGroup: String,
        allergies: String,
        chronicConditions: String,
        emergencyContactName: String,
        emergencyContactPhone: String
    ) {
        viewModelScope.launch {
            try {
                _updateResult.value = ApiResult.Loading
                Log.d("EditUserProfileVM", "Starting user profile update...")

                val currentUser = _userData.value
                if (currentUser == null) {
                    _updateResult.value = ApiResult.Error("User data not found")
                    return@launch
                }

                val emergencyContact = EmergencyContact(
                    name = emergencyContactName,
                    phone = emergencyContactPhone,
                    relationship = "Emergency Contact"
                )

                val updateRequest = UpdateUserRequest(
                    name = name,
                    abhaId = abhaId,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    allergies = allergies,
                    chronicConditions = chronicConditions,
                    emergencyContact = emergencyContact
                )

                Log.d("EditUserProfileVM", "Update request: $updateRequest")

                val response = apiService.updateUserProfile(currentUser.id.toString(), updateRequest)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("EditUserProfileVM", "Update response: $responseBody")
                    
                    if (responseBody?.success == true && responseBody.data != null) {
                        // Update local user data with new information
                        val updatedUser = currentUser.copy(
                            name = name,
                            abhaId = abhaId,
                            gender = gender,
                            bloodGroup = bloodGroup,
                            allergies = allergies,
                            chronicConditions = chronicConditions,
                            emergencyContact = emergencyContact
                        )
                        
                        // Save updated user data to DataStore
                        DataStoreManager.saveUserData(getApplication(), updatedUser)
                        _userData.value = updatedUser
                        
                        _updateResult.value = ApiResult.Success(response)
                        Log.d("EditUserProfileVM", "User profile updated successfully")
                    } else {
                        _updateResult.value = ApiResult.Error(responseBody?.message ?: "Update failed")
                        Log.e("EditUserProfileVM", "Update failed: ${responseBody?.message}")
                    }
                } else {
                    _updateResult.value = ApiResult.Error(response.errorBody()?.string() ?: "Update failed")
                    Log.e("EditUserProfileVM", "Update failed with status: ${response.code()}")
                }
            } catch (e: Exception) {
                _updateResult.value = ApiResult.Error(e.message ?: "Unexpected error during update")
                Log.e("EditUserProfileVM", "Exception during update: ${e.message}", e)
            }
        }
    }

    fun resetUpdateState() {
        _updateResult.value = null
    }
}