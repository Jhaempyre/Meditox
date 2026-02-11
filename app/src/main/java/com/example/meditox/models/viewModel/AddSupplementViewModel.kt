package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalSupplementEntity
import com.example.meditox.models.api.CreateGlobalSupplementRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AddSupplementViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddSupplementVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddSupplementViewModel::class.java)) {
                        return AddSupplementViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createGlobalDataSyncApiService()
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalSupplementDao = database.globalSupplementDao()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<GlobalSupplementEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                globalSupplementDao.searchSupplements(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form State
    private val _formState = MutableStateFlow(SupplementFormState())
    val formState: StateFlow<SupplementFormState> = _formState.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(AddSupplementUiState())
    val uiState: StateFlow<AddSupplementUiState> = _uiState.asStateFlow()

    // Create Result
    private val _createSupplementResult = MutableStateFlow<ApiResult<GlobalSupplementEntity>?>(null)
    val createSupplementResult: StateFlow<ApiResult<GlobalSupplementEntity>?> = _createSupplementResult.asStateFlow()

    // Selected Supplement for Add to Catalog
    private val _selectedSupplement = MutableStateFlow<GlobalSupplementEntity?>(null)
    val selectedSupplement: StateFlow<GlobalSupplementEntity?> = _selectedSupplement.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (_createSupplementResult.value != null) {
            _createSupplementResult.value = null
        }
    }

    fun updateFormField(update: (SupplementFormState) -> SupplementFormState) {
        _formState.value = update(_formState.value)
    }

    fun onSupplementSelected(supplement: GlobalSupplementEntity) {
        Log.d(TAG, "Selected Supplement: ${supplement.productName} (ID: ${supplement.globalSupplementId})")
        _selectedSupplement.value = supplement
    }

    fun clearSelectedSupplement() {
        _selectedSupplement.value = null
    }

    fun createSupplement() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _createSupplementResult.value = ApiResult.Loading

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = CreateGlobalSupplementRequest(
                    productName = form.productName,
                    brand = form.brand,
                    manufacturer = form.manufacturer,
                    supplementType = form.supplementType,
                    compositionSummary = form.compositionSummary,
                    ageGroup = form.ageGroup,
                    baseUnit = form.baseUnit,
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 1,
                    isLooseSaleAllowed = form.isLooseSaleAllowed,
                    fssaiLicense = form.fssaiLicense.ifBlank { null },
                    hsnCode = form.hsnCode,
                    gstRate = form.gstRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    createdByChemistId = chemistId
                )

                Log.d(TAG, "Creating Supplement: $request")
                val response = apiService.createGlobalSupplement(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    apiData?.let { data ->
                        val entity = GlobalSupplementEntity(
                            globalSupplementId = data.globalSupplementId,
                            productName = data.productName,
                            brand = data.brand,
                            manufacturer = data.manufacturer,
                            supplementType = data.supplementType,
                            compositionSummary = data.compositionSummary,
                            ageGroup = data.ageGroup,
                            baseUnit = data.baseUnit,
                            unitsPerPack = data.unitsPerPack,
                            isLooseSaleAllowed = data.isLooseSaleAllowed,
                            fssaiLicense = data.fssaiLicense ?: "",
                            hsnCode = data.hsnCode,
                            gstRate = data.gstRate.toString(),
                            verified = data.verified,
                            createdByChemistId = data.createdByChemistId,
                            createdAt = data.createdAt,
                            updatedAt = null,
                            isActive = data.isActive
                        )

                        globalSupplementDao.insert(entity)
                        Log.d(TAG, "Supplement saved to local DB")

                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)

                        _createSupplementResult.value = ApiResult.Success(entity)
                        _selectedSupplement.value = entity
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create supplement"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createSupplementResult.value = ApiResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating supplement", e)
                _createSupplementResult.value = ApiResult.Error(e.message ?: "Unknown error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetCreateResult() {
        _createSupplementResult.value = null
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.productName.isNotBlank() &&
                form.brand.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.supplementType.isNotBlank() &&
                form.compositionSummary.isNotBlank() &&
                form.ageGroup.isNotBlank() &&
                form.baseUnit.isNotBlank() &&
                form.unitsPerPack.toIntOrNull() != null &&
                form.hsnCode.isNotBlank() &&
                form.gstRate.toBigDecimalOrNull() != null
    }
}

data class AddSupplementUiState(
    val isLoading: Boolean = false
)

data class SupplementFormState(
    val productName: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val supplementType: String = "",
    val compositionSummary: String = "",
    val ageGroup: String = "",
    val baseUnit: String = "",
    val unitsPerPack: String = "1",
    val isLooseSaleAllowed: Boolean = false,
    val fssaiLicense: String = "",
    val hsnCode: String = "",
    val gstRate: String = ""
)
