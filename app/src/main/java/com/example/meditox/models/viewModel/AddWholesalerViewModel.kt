package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.models.wholesaler.CreateWholesalerRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddWholesalerViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddWholesalerVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddWholesalerViewModel::class.java)) {
                        return AddWholesalerViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createWholesalerApiService(context)
    private val wholesalerDao = MeditoxDatabase.getDatabase(context).wholesalerDao()

    // Form State
    private val _formState = MutableStateFlow(WholesalerFormState())
    val formState: StateFlow<WholesalerFormState> = _formState.asStateFlow()

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultMessage = MutableStateFlow<ResultMessage?>(null)
    val resultMessage: StateFlow<ResultMessage?> = _resultMessage.asStateFlow()

    private var currentWholesalerId: Long? = null
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun loadWholesaler(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val wholesaler = wholesalerDao.getAllWholesalers().first().find { it.wholesalerId == id }
                wholesaler?.let {
                    currentWholesalerId = it.wholesalerId
                    _isEditMode.value = true
                    _formState.value = WholesalerFormState(
                        wholesalerName = it.wholesalerName,
                        contactPerson = it.contactPerson ?: "",
                        phoneNumber = it.phoneNumber ?: "",
                        email = it.email ?: "",
                        gstin = it.gstin ?: "",
                        drugLicenseNumber = it.drugLicenseNumber ?: "",
                        stateCode = it.stateCode ?: "",
                        addressLine1 = it.addressLine1 ?: "",
                        addressLine2 = it.addressLine2 ?: "",
                        city = it.city ?: "",
                        state = it.state ?: "",
                        pincode = it.pincode ?: "",
                        creditDays = it.creditDays?.toString() ?: "",
                        creditLimit = it.creditLimit?.toString() ?: ""
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading wholesaler", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFormField(update: (WholesalerFormState) -> WholesalerFormState) {
        _formState.value = update(_formState.value)
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.wholesalerName.isNotBlank() &&
                form.contactPerson.isNotBlank() &&
                form.phoneNumber.isNotBlank() &&
                form.email.isNotBlank() &&
                form.gstin.isNotBlank() &&
                form.drugLicenseNumber.isNotBlank() &&
                form.stateCode.isNotBlank() && form.stateCode.length == 2 &&
                form.addressLine1.isNotBlank() &&
                form.city.isNotBlank() &&
                form.state.isNotBlank() &&
                form.pincode.isNotBlank() && form.pincode.length == 6 &&
                form.creditDays.toIntOrNull() != null &&
                form.creditLimit.toDoubleOrNull() != null
    }

    fun saveWholesaler() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = CreateWholesalerRequest(
                    wholesalerName = form.wholesalerName,
                    contactPerson = form.contactPerson,
                    phoneNumber = form.phoneNumber,
                    email = form.email,
                    gstin = form.gstin,
                    drugLicenseNumber = form.drugLicenseNumber,
                    stateCode = form.stateCode,
                    addressLine1 = form.addressLine1,
                    addressLine2 = form.addressLine2.ifBlank { null },
                    city = form.city,
                    state = form.state,
                    pincode = form.pincode,
                    creditDays = form.creditDays.toIntOrNull() ?: 0,
                    creditLimit = form.creditLimit.toDoubleOrNull() ?: 0.0
                )

                if (currentWholesalerId != null) {
                    Log.d(TAG, "Updating wholesaler ID: $currentWholesalerId")
                    val response = apiService.updateWholesaler(currentWholesalerId!!, request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let {
                            wholesalerDao.update(it.toEntity())
                            Log.d(TAG, "Wholesaler updated in local DB")
                        }
                        _resultMessage.value = ResultMessage(isSuccess = true, message = "Wholesaler updated successfully!")
                        _formState.value = WholesalerFormState()
                        currentWholesalerId = null
                        _isEditMode.value = false
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to update wholesaler"
                        Log.e(TAG, "API Error: $errorMsg")
                        _resultMessage.value = ResultMessage(isSuccess = false, message = errorMsg)
                    }
                } else {
                    Log.d(TAG, "Creating wholesaler: ${request.wholesalerName}")
                    val response = apiService.createWholesaler(chemistId, request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let {
                            wholesalerDao.insert(it.toEntity())
                            Log.d(TAG, "Wholesaler saved to local DB")
                        }
                        _resultMessage.value = ResultMessage(isSuccess = true, message = "Wholesaler created successfully!")
                        _formState.value = WholesalerFormState()
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to create wholesaler"
                        Log.e(TAG, "API Error: $errorMsg")
                        _resultMessage.value = ResultMessage(isSuccess = false, message = errorMsg)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception saving wholesaler", e)
                _resultMessage.value = ResultMessage(
                    isSuccess = false,
                    message = e.message ?: "Unknown error occurred"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResultMessage() {
        _resultMessage.value = null
    }
}

data class WholesalerFormState(
    val wholesalerName: String = "",
    val contactPerson: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val gstin: String = "",
    val drugLicenseNumber: String = "",
    val stateCode: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val creditDays: String = "",
    val creditLimit: String = ""
)

data class ResultMessage(
    val isSuccess: Boolean,
    val message: String
)
