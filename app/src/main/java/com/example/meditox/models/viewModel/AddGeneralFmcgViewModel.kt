package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalGeneralFmcgEntity
import com.example.meditox.models.api.CreateGlobalGeneralFmcgRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime

class AddGeneralFmcgViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddGeneralFmcgVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddGeneralFmcgViewModel::class.java)) {
                        return AddGeneralFmcgViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createGlobalDataSyncApiService()
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalGeneralFmcgDao = database.globalGeneralFmcgDao()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<GlobalGeneralFmcgEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                globalGeneralFmcgDao.searchFmcg(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form State
    private val _formState = MutableStateFlow(FmcgFormState())
    val formState: StateFlow<FmcgFormState> = _formState.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(AddFmcgUiState())
    val uiState: StateFlow<AddFmcgUiState> = _uiState.asStateFlow()

    // Create Result
    private val _createFmcgResult = MutableStateFlow<ApiResult<GlobalGeneralFmcgEntity>?>(null)
    val createFmcgResult: StateFlow<ApiResult<GlobalGeneralFmcgEntity>?> = _createFmcgResult.asStateFlow()

    // Selected FMCG for Add to Catalog
    private val _selectedFmcg = MutableStateFlow<GlobalGeneralFmcgEntity?>(null)
    val selectedFmcg: StateFlow<GlobalGeneralFmcgEntity?> = _selectedFmcg.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Clean create result when searching again
        if (_createFmcgResult.value != null) {
            _createFmcgResult.value = null
        }
    }

    fun updateFormField(update: (FmcgFormState) -> FmcgFormState) {
        _formState.value = update(_formState.value)
    }

    fun onFmcgSelected(fmcg: GlobalGeneralFmcgEntity) {
        Log.d(TAG, "Selected FMCG: ${fmcg.productName} (ID: ${fmcg.globalFmcgId})")
        _selectedFmcg.value = fmcg
    }

    fun clearSelectedFmcg() {
        _selectedFmcg.value = null
    }

    fun createFmcg() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _createFmcgResult.value = ApiResult.Loading

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L // Fallback or handle error

                val form = _formState.value
                val request = CreateGlobalGeneralFmcgRequest(
                    productName = form.productName,
                    brand = form.brand,
                    manufacturer = form.manufacturer,
                    categoryLabel = form.categoryLabel,
                    baseUnit = form.baseUnit,
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 1,
                    hsnCode = form.hsnCode,
                    gstRate = form.gstRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    createdByChemistId = chemistId
                )

                Log.d(TAG, "Creating FMCG: $request")
                val response = apiService.createGlobalGeneralFmcg(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    apiData?.let { data ->
                        val entity = GlobalGeneralFmcgEntity(
                            globalFmcgId = data.globalFmcgId,
                            productName = data.productName,
                            brand = data.brand,
                            manufacturer = data.manufacturer,
                            categoryLabel = data.categoryLabel,
                            baseUnit = data.baseUnit,
                            unitsPerPack = data.unitsPerPack,
                            hsnCode = data.hsnCode,
                            gstRate = data.gstRate.toString(),
                            verified = data.verified,
                            createdByChemistId = data.createdByChemistId,
                            createdAt = data.createdAt,
                            updatedAt = null,
                            isActive = data.isActive
                        )

                        // Insert into local DB
                        globalGeneralFmcgDao.insert(entity)
                        Log.d(TAG, "FMCG saved to local DB")

                        // Update sync stats
                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)

                        _createFmcgResult.value = ApiResult.Success(entity)
                        _selectedFmcg.value = entity // Auto-select created fmcg for Catalog flow
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create FMCG product"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createFmcgResult.value = ApiResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating FMCG", e)
                _createFmcgResult.value = ApiResult.Error(e.message ?: "Unknown error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetCreateResult() {
        _createFmcgResult.value = null
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.productName.isNotBlank() &&
                form.brand.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.categoryLabel.isNotBlank() &&
                form.baseUnit.isNotBlank() &&
                form.unitsPerPack.toIntOrNull() != null &&
                form.hsnCode.isNotBlank() &&
                form.gstRate.toBigDecimalOrNull() != null
    }
}

data class AddFmcgUiState(
    val isLoading: Boolean = false
)

data class FmcgFormState(
    val productName: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val categoryLabel: String = "",
    val baseUnit: String = "",
    val unitsPerPack: String = "1",
    val hsnCode: String = "",
    val gstRate: String = ""
)
