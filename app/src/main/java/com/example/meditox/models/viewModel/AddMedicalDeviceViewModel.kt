package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalMedicalDeviceEntity
import com.example.meditox.models.api.CreateGlobalMedicalDeviceRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AddMedicalDeviceViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddMedicalDeviceVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddMedicalDeviceViewModel::class.java)) {
                        return AddMedicalDeviceViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createGlobalDataSyncApiService()
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalMedicalDeviceDao = database.globalMedicalDeviceDao()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<GlobalMedicalDeviceEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                globalMedicalDeviceDao.searchDevices(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form State
    private val _formState = MutableStateFlow(MedicalDeviceFormState())
    val formState: StateFlow<MedicalDeviceFormState> = _formState.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(AddMedicalDeviceUiState())
    val uiState: StateFlow<AddMedicalDeviceUiState> = _uiState.asStateFlow()

    // Create Result
    private val _createDeviceResult = MutableStateFlow<ApiResult<GlobalMedicalDeviceEntity>?>(null)
    val createDeviceResult: StateFlow<ApiResult<GlobalMedicalDeviceEntity>?> = _createDeviceResult.asStateFlow()

    // Selected Device for Add to Catalog
    private val _selectedDevice = MutableStateFlow<GlobalMedicalDeviceEntity?>(null)
    val selectedDevice: StateFlow<GlobalMedicalDeviceEntity?> = _selectedDevice.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (_createDeviceResult.value != null) {
            _createDeviceResult.value = null
        }
    }

    fun updateFormField(update: (MedicalDeviceFormState) -> MedicalDeviceFormState) {
        _formState.value = update(_formState.value)
    }

    fun onDeviceSelected(device: GlobalMedicalDeviceEntity) {
        Log.d(TAG, "Selected Device: ${device.productName} (ID: ${device.globalDeviceId})")
        _selectedDevice.value = device
    }

    fun clearSelectedDevice() {
        _selectedDevice.value = null
    }

    fun createDevice() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _createDeviceResult.value = ApiResult.Loading

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = CreateGlobalMedicalDeviceRequest(
                    productName = form.productName,
                    brand = form.brand,
                    manufacturer = form.manufacturer,
                    modelNumber = form.modelNumber,
                    deviceType = form.deviceType,
                    baseUnit = form.baseUnit,
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 1,
                    reusable = form.reusable,
                    medicalDeviceClass = form.medicalDeviceClass,
                    certification = form.certification.ifBlank { null },
                    warrantyMonths = form.warrantyMonths.toIntOrNull(),
                    hsnCode = form.hsnCode,
                    gstRate = form.gstRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    createdByChemistId = chemistId
                )

                Log.d(TAG, "Creating Medical Device: $request")
                val response = apiService.createGlobalMedicalDevice(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    apiData?.let { data ->
                        val entity = GlobalMedicalDeviceEntity(
                            globalDeviceId = data.globalDeviceId,
                            productName = data.productName,
                            brand = data.brand,
                            manufacturer = data.manufacturer,
                            modelNumber = data.modelNumber,
                            deviceType = data.deviceType,
                            baseUnit = data.baseUnit,
                            unitsPerPack = data.unitsPerPack,
                            reusable = data.reusable,
                            medicalDeviceClass = data.medicalDeviceClass,
                            certification = data.certification ?: "",
                            warrantyMonths = data.warrantyMonths ?: 0,
                            hsnCode = data.hsnCode,
                            gstRate = data.gstRate.toString(),
                            verified = data.verified,
                            createdByChemistId = data.createdByChemistId,
                            createdAt = data.createdAt,
                            updatedAt = null,
                            isActive = data.isActive
                        )

                        globalMedicalDeviceDao.insert(entity)
                        Log.d(TAG, "Medical Device saved to local DB")

                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)

                        _createDeviceResult.value = ApiResult.Success(entity)
                        _selectedDevice.value = entity
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create medical device"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createDeviceResult.value = ApiResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating medical device", e)
                _createDeviceResult.value = ApiResult.Error(e.message ?: "Unknown error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetCreateResult() {
        _createDeviceResult.value = null
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.productName.isNotBlank() &&
                form.brand.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.modelNumber.isNotBlank() &&
                form.deviceType.isNotBlank() &&
                form.baseUnit.isNotBlank() &&
                form.unitsPerPack.toIntOrNull() != null &&
                form.medicalDeviceClass.isNotBlank() &&
                form.hsnCode.isNotBlank() &&
                form.gstRate.toBigDecimalOrNull() != null
    }
}

data class AddMedicalDeviceUiState(
    val isLoading: Boolean = false
)

data class MedicalDeviceFormState(
    val productName: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val modelNumber: String = "",
    val deviceType: String = "",
    val baseUnit: String = "",
    val unitsPerPack: String = "1",
    val reusable: Boolean = false,
    val medicalDeviceClass: String = "",
    val certification: String = "",
    val warrantyMonths: String = "",
    val hsnCode: String = "",
    val gstRate: String = ""
)
