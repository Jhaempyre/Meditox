package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalSurgicalConsumableEntity
import com.example.meditox.models.api.CreateGlobalSurgicalConsumableRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class AddSurgicalConsumableViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddSurgicalVM"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddSurgicalConsumableViewModel::class.java)) {
                        return AddSurgicalConsumableViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createGlobalDataSyncApiService()
    private val database = MeditoxDatabase.getDatabase(context)
    private val surgicalConsumableDao = database.globalSurgicalConsumableDao()

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<GlobalSurgicalConsumableEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                surgicalConsumableDao.searchConsumables(query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Form State
    private val _formState = MutableStateFlow(SurgicalConsumableFormState())
    val formState: StateFlow<SurgicalConsumableFormState> = _formState.asStateFlow()

    // UI State
    private val _uiState = MutableStateFlow(AddSurgicalConsumableUiState())
    val uiState: StateFlow<AddSurgicalConsumableUiState> = _uiState.asStateFlow()

    // Create Result
    private val _createResult = MutableStateFlow<ApiResult<GlobalSurgicalConsumableEntity>?>(null)
    val createResult: StateFlow<ApiResult<GlobalSurgicalConsumableEntity>?> = _createResult.asStateFlow()

    // Selected Item for Add to Catalog
    private val _selectedConsumable = MutableStateFlow<GlobalSurgicalConsumableEntity?>(null)
    val selectedConsumable: StateFlow<GlobalSurgicalConsumableEntity?> = _selectedConsumable.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (_createResult.value != null) {
            _createResult.value = null
        }
    }

    fun updateFormField(update: (SurgicalConsumableFormState) -> SurgicalConsumableFormState) {
        _formState.value = update(_formState.value)
    }

    fun onConsumableSelected(consumable: GlobalSurgicalConsumableEntity) {
        Log.d(TAG, "Selected Consumable: ${consumable.productName} (ID: ${consumable.globalSurgicalId})")
        _selectedConsumable.value = consumable
    }

    fun clearSelectedConsumable() {
        _selectedConsumable.value = null
    }

    fun createConsumable() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _createResult.value = ApiResult.Loading

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = CreateGlobalSurgicalConsumableRequest(
                    productName = form.productName,
                    brand = form.brand,
                    manufacturer = form.manufacturer,
                    material = form.material,
                    baseUnit = form.baseUnit,
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 1,
                    sterile = form.sterile,
                    disposable = form.disposable,
                    intendedUse = form.intendedUse,
                    sizeSpecification = form.sizeSpecification,
                    hsnCode = form.hsnCode,
                    gstRate = form.gstRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    createdByChemistId = chemistId
                )

                Log.d(TAG, "Creating Surgical Consumable: $request")
                val response = apiService.createGlobalSurgicalConsumable(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val apiData = response.body()?.data
                    apiData?.let { data ->
                        val entity = GlobalSurgicalConsumableEntity(
                            globalSurgicalId = data.globalSurgicalId,
                            productName = data.productName,
                            brand = data.brand,
                            manufacturer = data.manufacturer,
                            material = data.material,
                            baseUnit = data.baseUnit,
                            unitsPerPack = data.unitsPerPack,
                            sterile = data.sterile,
                            disposable = data.disposable,
                            intendedUse = data.intendedUse,
                            sizeSpecification = data.sizeSpecification,
                            hsnCode = data.hsnCode,
                            gstRate = data.gstRate.toString(),
                            verified = data.verified,
                            createdByChemistId = data.createdByChemistId,
                            createdAt = data.createdAt,
                            updatedAt = null,
                            isActive = data.isActive
                        )

                        surgicalConsumableDao.insert(entity)
                        Log.d(TAG, "Surgical Consumable saved to local DB")

                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)

                        _createResult.value = ApiResult.Success(entity)
                        _selectedConsumable.value = entity
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create surgical consumable"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createResult.value = ApiResult.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception creating surgical consumable", e)
                _createResult.value = ApiResult.Error(e.message ?: "Unknown error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetCreateResult() {
        _createResult.value = null
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.productName.isNotBlank() &&
                form.brand.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.material.isNotBlank() &&
                form.baseUnit.isNotBlank() &&
                form.unitsPerPack.toIntOrNull() != null &&
                form.intendedUse.isNotBlank() &&
                form.sizeSpecification.isNotBlank() &&
                form.hsnCode.isNotBlank() &&
                form.gstRate.toBigDecimalOrNull() != null
    }
}

data class AddSurgicalConsumableUiState(
    val isLoading: Boolean = false
)

data class SurgicalConsumableFormState(
    val productName: String = "",
    val brand: String = "",
    val manufacturer: String = "",
    val material: String = "",
    val baseUnit: String = "",
    val unitsPerPack: String = "1",
    val sterile: Boolean = false,
    val disposable: Boolean = true,
    val intendedUse: String = "",
    val sizeSpecification: String = "",
    val hsnCode: String = "",
    val gstRate: String = ""
)
