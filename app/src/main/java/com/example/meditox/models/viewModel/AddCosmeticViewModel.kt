package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalCosmeticEntity
import com.example.meditox.models.GlobalChemist.CreateGlobalCosmeticRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AddCosmeticViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddCosmeticViewModel"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddCosmeticViewModel::class.java)) {
                        return AddCosmeticViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createGlobalDataSyncApiService()
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalCosmeticDao = database.globalCosmeticDao()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<GlobalCosmeticEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                globalCosmeticDao.searchCosmetics(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form State
    private val _formState = MutableStateFlow(CosmeticFormState())
    val formState: StateFlow<CosmeticFormState> = _formState.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(AddCosmeticUiState())
    val uiState: StateFlow<AddCosmeticUiState> = _uiState.asStateFlow()

    // Create Result
    private val _createCosmeticResult = MutableStateFlow<ApiResult<GlobalCosmeticEntity>?>(null)
    val createCosmeticResult: StateFlow<ApiResult<GlobalCosmeticEntity>?> = _createCosmeticResult.asStateFlow()
    
    // Selected Cosmetic for Add to Catalog
    private val _selectedCosmetic = MutableStateFlow<GlobalCosmeticEntity?>(null)
    val selectedCosmetic: StateFlow<GlobalCosmeticEntity?> = _selectedCosmetic.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Clean create result when searching again
        if (_createCosmeticResult.value != null) {
            _createCosmeticResult.value = null
        }
    }

    fun updateFormField(update: (CosmeticFormState) -> CosmeticFormState) {
        _formState.value = update(_formState.value)
    }
    
    fun onCosmeticSelected(cosmetic: GlobalCosmeticEntity) {
        Log.d(TAG, "Selected cosmetic: ${cosmetic.productName} (ID: ${cosmetic.globalCosmeticId})")
        _selectedCosmetic.value = cosmetic
    }
    
    fun clearSelectedCosmetic() {
        _selectedCosmetic.value = null
    }

    fun createCosmetic() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _createCosmeticResult.value = ApiResult.Loading

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L // Fallback or handle error

                val form = _formState.value
                val request = CreateGlobalCosmeticRequest(
                    productName = form.productName,
                    brand = form.brand,
                    manufacturer = form.manufacturer,
                    form = form.form,
                    variant = form.variant,
                    baseUnit = form.baseUnit,
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 1,
                    intendedUse = form.intendedUse,
                    skinType = form.skinType,
                    cosmeticLicenseNumber = form.cosmeticLicenseNumber,
                    hsnCode = form.hsnCode,
                    gstRate = form.gstRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    createdByChemistId = chemistId
                )
                
                Log.d(TAG, "Creating cosmetic: $request")
                val response = apiService.createGlobalCosmetic(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    apiData?.let { data ->
                        val entity = GlobalCosmeticEntity(
                            globalCosmeticId = data.globalCosmeticId,
                            productName = data.productName,
                            brand = data.brand,
                            manufacturer = data.manufacturer,
                            form = data.form,
                            variant = data.variant,
                            baseUnit = data.baseUnit,
                            unitsPerPack = data.unitsPerPack,
                            intendedUse = data.intendedUse,
                            skinType = data.skinType,
                            cosmeticLicenseNumber = data.cosmeticLicenseNumber,
                            hsnCode = data.hsnCode,
                            gstRate = data.gstRate.toString(),
                            verified = data.verified,
                            createdByChemistId = data.createdByChemistId,
                            createdAt = data.createdAt,
                            updatedAt = null,
                            isActive = data.isActive
                        )
                        
                        // Insert into local DB
                        globalCosmeticDao.insert(entity)
                        Log.d(TAG, "Cosmetic saved to local DB")
                        
                        // Update sync stats
                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)

                        _createCosmeticResult.value = ApiResult.Success(entity)
                        _selectedCosmetic.value = entity // Auto-select created cosmetic for Catalog flow
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create cosmetic"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createCosmeticResult.value = ApiResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating cosmetic", e)
                _createCosmeticResult.value = ApiResult.Error(e.message ?: "Unknown error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetCreateResult() {
        _createCosmeticResult.value = null
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.productName.isNotBlank() &&
                form.brand.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.form.isNotBlank() &&
                form.variant.isNotBlank() &&
                form.baseUnit.isNotBlank() &&
                form.unitsPerPack.toIntOrNull() != null &&
                form.intendedUse.isNotBlank() &&
                form.skinType.isNotBlank() &&
                form.cosmeticLicenseNumber.isNotBlank() &&
                form.hsnCode.isNotBlank() &&
                form.gstRate.toBigDecimalOrNull() != null
    }
}

data class AddCosmeticUiState(
    val isLoading: Boolean = false
)

data class CosmeticFormState(
    val productName: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val form: String = "",
    val variant: String = "",
    val baseUnit: String = "",
    val unitsPerPack: String = "1",
    val intendedUse: String = "",
    val skinType: String = "",
    val cosmeticLicenseNumber: String = "",
    val hsnCode: String = "",
    val gstRate: String = ""
)
