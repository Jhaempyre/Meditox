package com.example.meditox.models.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SellStockUiModel(
    val batchStockId: Long,
    val chemistProductId: Long,
    val name: String,
    val genericName: String,
    val strength: String,
    val batchNumber: String?,
    val category: String,
    val createdAt: String?,
    val isOldestBatch: Boolean,
    val pricePerUnit: Double?,
    val sellingPrice: Double?,
    val mrp: Double?
)

data class SellItemDetails(
    val title: String,
    val category: String,
    val details: List<Pair<String, String>>
)

class SellMedicineViewModel(context: Context) : ViewModel() {

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SellMedicineViewModel::class.java)) {
                        return SellMedicineViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val db = MeditoxDatabase.getDatabase(context)
    private val chemistBatchStockDao = db.chemistBatchStockDao()
    private val chemistProductDao = db.chemistProductMasterDao()
    private val drugDao = db.globalDrugDao()
    private val cosmeticDao = db.globalCosmeticDao()
    private val fmcgDao = db.globalGeneralFmcgDao()
    private val deviceDao = db.globalMedicalDeviceDao()
    private val supplementDao = db.globalSupplementDao()
    private val surgicalDao = db.globalSurgicalConsumableDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allStocks = MutableStateFlow<List<SellStockUiModel>>(emptyList())
    private val _detailsByBatchId = MutableStateFlow<Map<Long, SellItemDetails>>(emptyMap())
    val detailsByBatchId: StateFlow<Map<Long, SellItemDetails>> = _detailsByBatchId.asStateFlow()

    val filteredStocks: StateFlow<List<SellStockUiModel>> = combine(
        _searchQuery,
        _allStocks
    ) { query, stocks ->
        if (query.isBlank()) {
            stocks
        } else {
            val q = query.trim()
            stocks.filter { item ->
                item.name.contains(q, ignoreCase = true) ||
                    item.genericName.contains(q, ignoreCase = true) ||
                    item.strength.contains(q, ignoreCase = true) ||
                    (item.batchNumber?.contains(q, ignoreCase = true) == true) ||
                    item.category.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadStocks()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadStocks() {
        viewModelScope.launch {
            _isLoading.value = true

            chemistBatchStockDao.getAll().collect { batchStocks ->
                val activeStocks = batchStocks.filter { it.quantityInStock > 0 }

                val masterMap = chemistProductDao.getAllProducts().first()
                    .associateBy { it.chemistProductId }

                val drugsMap = mutableMapOf<Long, GlobalDrugEntity>()
                val cosmeticsMap = mutableMapOf<Long, GlobalCosmeticEntity>()
                val fmcgMap = mutableMapOf<Long, GlobalGeneralFmcgEntity>()
                val devicesMap = mutableMapOf<Long, GlobalMedicalDeviceEntity>()
                val supplementsMap = mutableMapOf<Long, GlobalSupplementEntity>()
                val surgicalsMap = mutableMapOf<Long, GlobalSurgicalConsumableEntity>()

                drugDao.getAllDrugs().first().forEach { drugsMap[it.globalDrugId] = it }
                cosmeticDao.getAllCosmetics().first().forEach { cosmeticsMap[it.globalCosmeticId] = it }
                fmcgDao.getAllFmcgProducts().first().forEach { fmcgMap[it.globalFmcgId] = it }
                deviceDao.getAllDevices().first().forEach { devicesMap[it.globalDeviceId] = it }
                supplementDao.getAllSupplements().first().forEach { supplementsMap[it.globalSupplementId] = it }
                surgicalDao.getAllConsumables().first().forEach { surgicalsMap[it.globalSurgicalId] = it }

                val detailsMap = mutableMapOf<Long, SellItemDetails>()

                val uiModels = activeStocks.mapNotNull { stock ->
                    val master = masterMap[stock.chemistProductId]
                    val category = master?.productCategory ?: stock.productCategory
                    val globalId = master?.globalProductId

                    val (name, generic, strength) = when (category.uppercase()) {
                        "DRUG" -> {
                            val drug = globalId?.let { drugsMap[it] }
                            val nameValue = drug?.brandName ?: stock.productName
                            val genericValue = drug?.genericName?.ifBlank { "..." } ?: "..."
                            val strengthValue = drug?.let { "${it.strengthValue} ${it.strengthUnit}".trim() } ?: "..."
                            Triple(nameValue, genericValue, strengthValue)
                        }
                        "COSMETIC" -> {
                            val cosmetic = globalId?.let { cosmeticsMap[it] }
                            val nameValue = cosmetic?.productName ?: stock.productName
                            val genericValue = cosmetic?.brand?.ifBlank { "..." } ?: "..."
                            Triple(nameValue, genericValue, "...")
                        }
                        "FMCG", "GENERAL_FMCG" -> {
                            val fmcg = globalId?.let { fmcgMap[it] }
                            val nameValue = fmcg?.productName ?: stock.productName
                            val genericValue = fmcg?.brand?.ifBlank { "..." } ?: "..."
                            Triple(nameValue, genericValue, "...")
                        }
                        "MEDICAL_DEVICE" -> {
                            val device = globalId?.let { devicesMap[it] }
                            val nameValue = device?.productName ?: stock.productName
                            val genericValue = device?.brand?.ifBlank { "..." } ?: "..."
                            val strengthValue = device?.modelNumber?.ifBlank { "..." } ?: "..."
                            Triple(nameValue, genericValue, strengthValue)
                        }
                        "SUPPLEMENT" -> {
                            val supplement = globalId?.let { supplementsMap[it] }
                            val nameValue = supplement?.productName ?: stock.productName
                            val genericValue = supplement?.brand?.ifBlank { "..." } ?: "..."
                            Triple(nameValue, genericValue, "...")
                        }
                        "SURGICAL", "SURGICAL_CONSUMABLE" -> {
                            val surgical = globalId?.let { surgicalsMap[it] }
                            val nameValue = surgical?.productName ?: stock.productName
                            val genericValue = surgical?.brand?.ifBlank { "..." } ?: "..."
                            val strengthValue = surgical?.sizeSpecification?.ifBlank { "..." } ?: "..."
                            Triple(nameValue, genericValue, strengthValue)
                        }
                        else -> Triple(stock.productName, "...", "...")
                    }

                    val details = buildDetails(
                        stock = stock,
                        category = category,
                        master = master,
                        drug = globalId?.let { drugsMap[it] },
                        cosmetic = globalId?.let { cosmeticsMap[it] },
                        fmcg = globalId?.let { fmcgMap[it] },
                        device = globalId?.let { devicesMap[it] },
                        supplement = globalId?.let { supplementsMap[it] },
                        surgical = globalId?.let { surgicalsMap[it] }
                    )
                    detailsMap[stock.batchStockId] = details

                    SellStockUiModel(
                        batchStockId = stock.batchStockId,
                        chemistProductId = stock.chemistProductId,
                        name = name,
                        genericName = generic,
                        strength = strength,
                        batchNumber = stock.batchNumber,
                        category = category,
                        createdAt = stock.createdAt,
                        isOldestBatch = false,
                        pricePerUnit = stock.sellingRate,
                        sellingPrice = stock.sellingRate,
                        mrp = stock.mrp
                    )
                }

                val oldestBatchIds = uiModels
                    .groupBy { it.chemistProductId }
                    .mapValues { (_, items) ->
                        items.minByOrNull { it.createdAt ?: "9999-12-31T23:59:59Z" }?.batchStockId
                    }

                val sortedModels = uiModels
                    .map { it.copy(isOldestBatch = oldestBatchIds[it.chemistProductId] == it.batchStockId) }
                    .sortedWith(
                        compareByDescending<SellStockUiModel> { it.isOldestBatch }
                            .thenBy { it.createdAt ?: "9999-12-31T23:59:59Z" }
                    )

                _allStocks.value = sortedModels
                _detailsByBatchId.value = detailsMap
                _isLoading.value = false
            }
        }
    }

    private fun buildDetails(
        stock: ChemistBatchStockEntity,
        category: String,
        master: ChemistProductMasterEntity?,
        drug: GlobalDrugEntity?,
        cosmetic: GlobalCosmeticEntity?,
        fmcg: GlobalGeneralFmcgEntity?,
        device: GlobalMedicalDeviceEntity?,
        supplement: GlobalSupplementEntity?,
        surgical: GlobalSurgicalConsumableEntity?
    ): SellItemDetails {
        val details = mutableListOf<Pair<String, String>>()

        fun add(label: String, value: String?) {
            if (!value.isNullOrBlank() && value != "...") {
                details.add(label to value)
            }
        }

        fun addInt(label: String, value: Int?) {
            if (value != null && value > 0) {
                details.add(label to value.toString())
            }
        }

        fun addDouble(label: String, value: Double?) {
            if (value != null) {
                details.add(label to "%.2f".format(value))
            }
        }
        
        fun addPercent(label: String, value: Double?) {
             if (value != null) {
                 details.add(label to "%.2f%%".format(value))
             }
        }

        // --- Core Stock Details ---
        add("Product Name", stock.productName)
        add("Category", stock.productCategory)
        add("Wholesaler", stock.wholesalerName)
        add("Batch No", stock.batchNumber)
        addInt("Stock Quantity", stock.quantityInStock)
        
        // --- Pricing ---
        addDouble("Purchase Rate", stock.purchaseRate)
        addDouble("Selling Rate", stock.sellingRate)
        addDouble("MRP", stock.mrp)
        
        // --- Dates ---
        add("Mfg Date", stock.manufacturingDate)
        add("Expiry Date", stock.expiryDate)

        // --- Global Details by Category ---
        when (category.uppercase()) {
            "DRUG" -> {
                add("Brand Name", drug?.brandName)
                add("Generic Name", drug?.genericName)
                add("Manufacturer", drug?.manufacturer)
                add("System of Med", drug?.systemOfMedicine)
                add("Dosage Form", drug?.dosageForm)
                
                val strength = if (drug?.strengthValue != null && drug.strengthUnit != null) 
                    "${drug.strengthValue} ${drug.strengthUnit}" else null
                add("Strength", strength)
                
                add("Route", drug?.routeOfAdministration)
                addPercent("GST", drug?.gst)
                add("Schedule", drug?.drugSchedule)
            }
            "COSMETIC" -> {
                add("Brand", cosmetic?.brand)
                add("Manufacturer", cosmetic?.manufacturer)
                add("Form", cosmetic?.form)
                add("Variant", cosmetic?.variant)
                add("Intended Use", cosmetic?.intendedUse)
                add("Skin Type", cosmetic?.skinType)
                add("GST Rate", cosmetic?.gstRate)
            }
            "FMCG", "GENERAL_FMCG" -> {
                add("Brand", fmcg?.brand)
                add("Manufacturer", fmcg?.manufacturer)
                add("Category Label", fmcg?.categoryLabel)
                add("GST Rate", fmcg?.gstRate)
            }
            "MEDICAL_DEVICE" -> {
                add("Brand", device?.brand)
                add("Manufacturer", device?.manufacturer)
                add("Model No", device?.modelNumber)
                add("Device Type", device?.deviceType)
                add("Class", device?.medicalDeviceClass)
                add("Usage", if (device?.reusable == true) "Reusable" else "Single Use")
                add("GST Rate", device?.gstRate)
            }
            "SUPPLEMENT" -> {
                add("Brand", supplement?.brand)
                add("Manufacturer", supplement?.manufacturer)
                add("Type", supplement?.supplementType)
                add("Composition", supplement?.compositionSummary)
                add("Age Group", supplement?.ageGroup)
                add("FSSAI", supplement?.fssaiLicense)
                add("GST Rate", supplement?.gstRate)
            }
            "SURGICAL", "SURGICAL_CONSUMABLE" -> {
                add("Brand", surgical?.brand)
                add("Manufacturer", surgical?.manufacturer)
                add("Material", surgical?.material)
                add("Size", surgical?.sizeSpecification)
                add("Usage", if (surgical?.disposable == true) "Disposable" else "Reusable")
                add("Sterile", if (surgical?.sterile == true) "Yes" else "No")
                add("GST Rate", surgical?.gstRate)
            }
        }
        
        // --- Pack Info (Common if available) ---
        // Using "unitsPerPack" from relevant entity
        val packSize = when(category.uppercase()) {
             "DRUG" -> drug?.unitsPerPack
             "COSMETIC" -> cosmetic?.unitsPerPack
             "FMCG", "GENERAL_FMCG" -> fmcg?.unitsPerPack
             "MEDICAL_DEVICE" -> device?.unitsPerPack
             "SUPPLEMENT" -> supplement?.unitsPerPack
             "SURGICAL", "SURGICAL_CONSUMABLE" -> surgical?.unitsPerPack
             else -> null
        }
        if (packSize != null && packSize > 1) {
             add("Pack Size", "$packSize Units")
        }

        return SellItemDetails(
            title = stock.productName,
            category = category,
            details = details
        )
    }
}
