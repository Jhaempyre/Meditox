package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.ChemistBatchStockEntity
import com.example.meditox.models.chemist.AddStockRequest
import com.example.meditox.models.chemist.AddStockResponse
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddStockViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddStockViewModel"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddStockViewModel::class.java)) {
                        return AddStockViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val apiService = ApiClient.createChemistProductApiService(context)
    private val batchStockDao = MeditoxDatabase.getDatabase(context).chemistBatchStockDao()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _resultMessage = MutableStateFlow<AddStockResult?>(null)
    val resultMessage: StateFlow<AddStockResult?> = _resultMessage.asStateFlow()

    fun clearResultMessage() {
        _resultMessage.value = null
    }

    fun addStock(product: ProductUiModel, request: AddStockRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _resultMessage.value = null

                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId

                if (chemistId == null) {
                    _resultMessage.value = AddStockResult(false, "Chemist ID not found")
                    return@launch
                }

                Log.d(TAG, "Posting add stock for chemistId=$chemistId")
                val response = apiService.addStock(chemistId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        batchStockDao.insert(buildEntityFromResponse(chemistId, product, request, data))
                        _resultMessage.value = AddStockResult(true, "Stock added successfully")
                    } else {
                        _resultMessage.value = AddStockResult(false, "Empty response from server")
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to add stock"
                    Log.e(TAG, "API Error: $errorMsg")
                    _resultMessage.value = AddStockResult(false, errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception adding stock", e)
                _resultMessage.value = AddStockResult(false, e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildEntityFromResponse(
        chemistId: Long,
        product: ProductUiModel,
        request: AddStockRequest,
        response: AddStockResponse
    ): ChemistBatchStockEntity {
        return ChemistBatchStockEntity(
            batchStockId = response.batchStockId,
            chemistId = chemistId,
            chemistProductId = response.chemistProductId,
            productName = response.productName,
            productCategory = mapCategory(product.category),
            wholesalerId = response.wholesalerId,
            wholesalerName = response.wholesalerName,
            batchNumber = response.batchNumber,
            serialNumber = null,
            quantityInStock = response.totalStock,
            quantityReserved = 0,
            quantityDamaged = 0,
            purchaseRate = response.purchasePrice?.toDouble(),
            sellingRate = request.sellingPrice?.toDouble(),
            mrp = request.mrp?.toDouble(),
            manufacturingDate = request.mfgDate,
            expiryDate = response.expiryDate,
            receivedDate = null,
            storageLocation = null,
            isExpired = null,
            isNearExpiry = null,
            createdAt = response.addedAt,
            updatedAt = response.addedAt
        )
    }

    private fun mapCategory(category: String): String {
        return when (category.lowercase()) {
            "drug" -> "DRUG"
            "cosmetic" -> "COSMETIC"
            "fmcg" -> "GENERAL_FMCG"
            "device" -> "MEDICAL_DEVICE"
            "supplement" -> "SUPPLEMENT"
            "surgical" -> "SURGICAL_CONSUMABLE"
            else -> category.uppercase()
        }
    }
}

data class AddStockResult(
    val isSuccess: Boolean,
    val message: String
)
