package com.example.meditox.models.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditox.models.User
import com.example.meditox.models.businessRegistration.BusinessRegistrationRequest
import com.example.meditox.models.businessRegistration.BusinessRegistrationResponse
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessRegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val _registrationResult = MutableStateFlow<ApiResult<BusinessRegistrationResponse>?>(null)
    val registrationResult: MutableStateFlow<ApiResult<BusinessRegistrationResponse>?> = _registrationResult

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            DataStoreManager.getUserData(application).collect {
                _user.value= it
                Log.d("BusinessRegistrationModel", "User data loaded: $it")
            }
        }
    }


    fun registerBusiness(businessDetails: BusinessRegistrationRequest) {

    }

}