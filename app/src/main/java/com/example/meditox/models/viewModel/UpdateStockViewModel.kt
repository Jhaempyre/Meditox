package com.example.meditox.models.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductUiModel(
    val chemistProductId: Long,
    val name: String,
    val strength: String,   // e.g. "500 mg" for drugs, "..." for others
    val genericName: String, // e.g. "Paracetamol" for drugs, "..." for others
    val category: String,
    val dosageForm: String? = null // e.g. "TABLET", "CAPSULE" — only for Drug products
)

class UpdateStockViewModel(context: Context) : ViewModel() {

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(UpdateStockViewModel::class.java)) {
                        return UpdateStockViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val db = MeditoxDatabase.getDatabase(context)
    private val chemistProductDao = db.chemistProductMasterDao()
    private val drugDao = db.globalDrugDao()
    private val cosmeticDao = db.globalCosmeticDao()
    private val fmcgDao = db.globalGeneralFmcgDao()
    private val deviceDao = db.globalMedicalDeviceDao()
    private val supplementDao = db.globalSupplementDao()
    private val surgicalDao = db.globalSurgicalConsumableDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductUiModel>>(emptyList())

    val filteredProducts: StateFlow<List<ProductUiModel>> = combine(
        _searchQuery,
        _selectedCategory,
        _allProducts
    ) { query, category, products ->
        var result = products
        
        // Filter by Category if selected
        if (category != null) {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Filter by Search Query
        if (query.isNotBlank()) {
            result = result.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.genericName.contains(query, ignoreCase = true) ||
                        (category == null && product.category.contains(query, ignoreCase = true)) // Only search category if not already filtered
            }
        }
        result
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true

            // Collect all chemist products
            chemistProductDao.getAllProducts().collect { chemistProducts ->
                // Build lookup maps from global tables
                val drugsMap = mutableMapOf<Long, GlobalDrugEntity>()
                val cosmeticsMap = mutableMapOf<Long, GlobalCosmeticEntity>()
                val fmcgMap = mutableMapOf<Long, GlobalGeneralFmcgEntity>()
                val devicesMap = mutableMapOf<Long, GlobalMedicalDeviceEntity>()
                val supplementsMap = mutableMapOf<Long, GlobalSupplementEntity>()
                val surgicalsMap = mutableMapOf<Long, GlobalSurgicalConsumableEntity>()

                // Collect each global table once
                drugDao.getAllDrugs().first().forEach { drugsMap[it.globalDrugId] = it }
                cosmeticDao.getAllCosmetics().first().forEach { cosmeticsMap[it.globalCosmeticId] = it }
                fmcgDao.getAllFmcgProducts().first().forEach { fmcgMap[it.globalFmcgId] = it }
                deviceDao.getAllDevices().first().forEach { devicesMap[it.globalDeviceId] = it }
                supplementDao.getAllSupplements().first().forEach { supplementsMap[it.globalSupplementId] = it }
                surgicalDao.getAllConsumables().first().forEach { surgicalsMap[it.globalSurgicalId] = it }

                val uiModels = chemistProducts.mapNotNull { product ->
                    when (product.productCategory.uppercase()) {
                        "DRUG" -> {
                            val drug = drugsMap[product.globalProductId]
                            drug?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.brandName,
                                    strength = "${it.strengthValue} ${it.strengthUnit}",
                                    genericName = it.genericName.ifBlank { "..." },
                                    category = "Drug",
                                    dosageForm = it.dosageForm
                                )
                            }
                        }
                        "COSMETIC" -> {
                            val cosmetic = cosmeticsMap[product.globalProductId]
                            cosmetic?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.productName,
                                    strength = "...",
                                    genericName = it.brand.ifBlank { "..." },
                                    category = "Cosmetic"
                                )
                            }
                        }
                        "FMCG", "GENERAL_FMCG" -> {
                            val fmcg = fmcgMap[product.globalProductId]
                            fmcg?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.productName,
                                    strength = "...",
                                    genericName = it.brand.ifBlank { "..." },
                                    category = "FMCG"
                                )
                            }
                        }
                        "MEDICAL_DEVICE" -> {
                            val device = devicesMap[product.globalProductId]
                            device?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.productName,
                                    strength = it.modelNumber.ifBlank { "..." },
                                    genericName = it.brand.ifBlank { "..." },
                                    category = "Device"
                                )
                            }
                        }
                        "SUPPLEMENT" -> {
                            val supplement = supplementsMap[product.globalProductId]
                            supplement?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.productName,
                                    strength = "...",
                                    genericName = it.brand.ifBlank { "..." },
                                    category = "Supplement"
                                )
                            }
                        }
                        "SURGICAL", "SURGICAL_CONSUMABLE" -> {
                            val surgical = surgicalsMap[product.globalProductId]
                            surgical?.let {
                                ProductUiModel(
                                    chemistProductId = product.chemistProductId,
                                    name = it.productName,
                                    strength = it.sizeSpecification.ifBlank { "..." },
                                    genericName = it.brand.ifBlank { "..." },
                                    category = "Surgical"
                                )
                            }
                        }
                        else -> null
                    }
                }

                _allProducts.value = uiModels
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }
}
