package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.ChemistProductMasterEntity
import com.example.meditox.models.chemist.AddProductToCatalogRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class AddToCatalogViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddToCatalogViewModel"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddToCatalogViewModel::class.java)) {
                        return AddToCatalogViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    // Database and API
    private val database = MeditoxDatabase.getDatabase(context)
    private val chemistProductDao = database.chemistProductMasterDao()
    private val apiService = ApiClient.createChemistProductApiService(context)

    // UI State
    private val _uiState = MutableStateFlow(AddToCatalogUiState())
    val uiState: StateFlow<AddToCatalogUiState> = _uiState.asStateFlow()

    // Form state
    private val _formState = MutableStateFlow(CatalogFormState())
    val formState: StateFlow<CatalogFormState> = _formState.asStateFlow()

    // API result
    private val _addResult = MutableStateFlow<ApiResult<ChemistProductMasterEntity>?>(null)
    val addResult: StateFlow<ApiResult<ChemistProductMasterEntity>?> = _addResult.asStateFlow()

    fun updateFormField(update: (CatalogFormState) -> CatalogFormState) {
        _formState.value = update(_formState.value)
    }

    fun addProductToCatalog(globalDrugId: Long, category: String = "DRUG") {
        viewModelScope.launch {
            try {
                print("Adding to catalog: $category")
                _addResult.value = ApiResult.Loading
                _uiState.update { it.copy(isLoading = true) }

                // Get chemist ID from DataStore
                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = AddProductToCatalogRequest(
                    productCategory = category,
                    globalProductId = globalDrugId,
                    minimumStockLevel = form.minimumStockLevel.toIntOrNull() ?: 10,
                    maximumStockLevel = form.maximumStockLevel.toIntOrNull() ?: 100,
                    reorderQuantity = form.reorderQuantity.toIntOrNull() ?: 50
                )

                Log.d(TAG, "Adding product to catalog: globalDrugId=$globalDrugId, chemistId=$chemistId")
                val response = apiService.addProductToCatalog(chemistId, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val productResponse = response.body()?.data
                    Log.d(TAG, "Product added successfully: ${productResponse?.chemistProductId}")

                    // Save to Room DB
                    productResponse?.let { product ->
                        val entity = ChemistProductMasterEntity(
                            chemistProductId = product.chemistProductId,
                            chemistId = product.chemistId,
                            productCategory = product.productCategory,
                            globalProductId = product.globalProductId,
                            minimumStockLevel = product.minimumStockLevel,
                            maximumStockLevel = product.maximumStockLevel,
                            reorderQuantity = product.reorderQuantity,
                            isActive = product.isActive,
                            updatedAt = product.createdAt
                        )
                        chemistProductDao.insert(entity)
                        Log.d(TAG, "Product saved to local DB")

                        // Update total synced records count
                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)
                        Log.d(TAG, "Updated total synced records to: ${currentCount + 1}")

                        _addResult.value = ApiResult.Success(entity)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to add product to catalog"
                    Log.e(TAG, "API Error: $errorMsg")
                    _addResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding product to catalog", e)
                _addResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetResult() {
        _addResult.value = null
    }

    fun resetForm() {
        _formState.value = CatalogFormState()
    }
}

// UI State
data class AddToCatalogUiState(
    val isLoading: Boolean = false
)

// Form state for catalog addition
data class CatalogFormState(
    val minimumStockLevel: String = "10",
    val maximumStockLevel: String = "100",
    val reorderQuantity: String = "50"
)
