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
            if (!value.isNullOrBlank()) {
                details.add(label to value)
            }
        }

        fun addBool(label: String, value: Boolean?) {
            if (value != null) {
                details.add(label to if (value) "Yes" else "No")
            }
        }

        fun addInt(label: String, value: Int?) {
            if (value != null) {
                details.add(label to value.toString())
            }
        }

        fun addLong(label: String, value: Long?) {
            if (value != null) {
                details.add(label to value.toString())
            }
        }

        fun addDouble(label: String, value: Double?) {
            if (value != null) {
                details.add(label to "%.2f".format(value))
            }
        }

        // Stock details (batch-level)
        addLong("Batch Stock ID", stock.batchStockId)
        addLong("Chemist ID", stock.chemistId)
        addLong("Chemist Product ID", stock.chemistProductId)
        add("Product Category", stock.productCategory)
        add("Product Name", stock.productName)
        addLong("Wholesaler ID", stock.wholesalerId)
        add("Wholesaler Name", stock.wholesalerName)
        add("Batch Number", stock.batchNumber)
        add("Serial Number", stock.serialNumber)
        addInt("Quantity In Stock", stock.quantityInStock)
        addInt("Quantity Reserved", stock.quantityReserved)
        addInt("Quantity Damaged", stock.quantityDamaged)
        addDouble("Purchase Rate", stock.purchaseRate)
        addDouble("Selling Rate", stock.sellingRate)
        addDouble("MRP", stock.mrp)
        add("Manufacturing Date", stock.manufacturingDate)
        add("Expiry Date", stock.expiryDate)
        add("Received Date", stock.receivedDate)
        add("Storage Location", stock.storageLocation)
        addBool("Is Expired", stock.isExpired)
        addBool("Is Near Expiry", stock.isNearExpiry)
        add("Created At", stock.createdAt)
        add("Updated At", stock.updatedAt)

        // Master details
        addLong("Global Product ID", master?.globalProductId)
        addInt("Minimum Stock Level", master?.minimumStockLevel)
        addInt("Maximum Stock Level", master?.maximumStockLevel)
        addInt("Reorder Quantity", master?.reorderQuantity)
        addBool("Is Active (Master)", master?.isActive)
        add("Master Updated At", master?.updatedAt)

        // Global details by category
        when (category.uppercase()) {
            "DRUG" -> {
                addLong("Global Drug ID", drug?.globalDrugId)
                add("Brand Name", drug?.brandName)
                add("Generic Name", drug?.genericName)
                add("Manufacturer", drug?.manufacturer)
                add("System of Medicine", drug?.systemOfMedicine)
                add("Dosage Form", drug?.dosageForm)
                add("Strength Value", drug?.strengthValue)
                add("Strength Unit", drug?.strengthUnit)
                add("Route of Administration", drug?.routeOfAdministration)
                add("Base Unit", drug?.baseUnit)
                addInt("Units Per Pack", drug?.unitsPerPack)
                addBool("Loose Sale Allowed", drug?.isLooseSaleAllowed)
                add("Drug Schedule", drug?.drugSchedule)
                addBool("Prescription Required", drug?.prescriptionRequired)
                addBool("Narcotic Drug", drug?.narcoticDrug)
                add("Regulatory Authority", drug?.regulatoryAuthority)
                add("HSN Code", drug?.hsnCode)
                addDouble("GST", drug?.gst)
                addBool("Verified", drug?.verified)
                addLong("Created By Chemist ID", drug?.createdByChemistId)
                add("Created At", drug?.createdAt)
                addBool("Is Active", drug?.isActive)
                add("Image URL", drug?.imageUrl)
                add("Description", drug?.description)
                add("Advice", drug?.advice)
            }
            "COSMETIC" -> {
                addLong("Global Cosmetic ID", cosmetic?.globalCosmeticId)
                add("Product Name", cosmetic?.productName)
                add("Brand", cosmetic?.brand)
                add("Manufacturer", cosmetic?.manufacturer)
                add("Form", cosmetic?.form)
                add("Variant", cosmetic?.variant)
                add("Base Unit", cosmetic?.baseUnit)
                addInt("Units Per Pack", cosmetic?.unitsPerPack)
                add("Intended Use", cosmetic?.intendedUse)
                add("Skin Type", cosmetic?.skinType)
                add("Cosmetic License Number", cosmetic?.cosmeticLicenseNumber)
                add("HSN Code", cosmetic?.hsnCode)
                add("GST Rate", cosmetic?.gstRate)
                addBool("Verified", cosmetic?.verified)
                addLong("Created By Chemist ID", cosmetic?.createdByChemistId)
                add("Created At", cosmetic?.createdAt)
                add("Updated At", cosmetic?.updatedAt)
                addBool("Is Active", cosmetic?.isActive)
                add("Image URL", cosmetic?.imageUrl)
                add("Description", cosmetic?.description)
                add("Advice", cosmetic?.advice)
            }
            "FMCG", "GENERAL_FMCG" -> {
                addLong("Global FMCG ID", fmcg?.globalFmcgId)
                add("Product Name", fmcg?.productName)
                add("Brand", fmcg?.brand)
                add("Manufacturer", fmcg?.manufacturer)
                add("Category Label", fmcg?.categoryLabel)
                add("Base Unit", fmcg?.baseUnit)
                addInt("Units Per Pack", fmcg?.unitsPerPack)
                add("HSN Code", fmcg?.hsnCode)
                add("GST Rate", fmcg?.gstRate)
                addBool("Verified", fmcg?.verified)
                addLong("Created By Chemist ID", fmcg?.createdByChemistId)
                add("Created At", fmcg?.createdAt)
                add("Updated At", fmcg?.updatedAt)
                addBool("Is Active", fmcg?.isActive)
                add("Image URL", fmcg?.imageUrl)
                add("Description", fmcg?.description)
                add("Advice", fmcg?.advice)
            }
            "MEDICAL_DEVICE" -> {
                addLong("Global Device ID", device?.globalDeviceId)
                add("Product Name", device?.productName)
                add("Brand", device?.brand)
                add("Manufacturer", device?.manufacturer)
                add("Model Number", device?.modelNumber)
                add("Device Type", device?.deviceType)
                add("Base Unit", device?.baseUnit)
                addInt("Units Per Pack", device?.unitsPerPack)
                addBool("Reusable", device?.reusable)
                add("Medical Device Class", device?.medicalDeviceClass)
                add("Certification", device?.certification)
                addInt("Warranty Months", device?.warrantyMonths)
                add("HSN Code", device?.hsnCode)
                add("GST Rate", device?.gstRate)
                addBool("Verified", device?.verified)
                addLong("Created By Chemist ID", device?.createdByChemistId)
                add("Created At", device?.createdAt)
                add("Updated At", device?.updatedAt)
                addBool("Is Active", device?.isActive)
                add("Image URL", device?.imageUrl)
                add("Description", device?.description)
                add("Advice", device?.advice)
            }
            "SUPPLEMENT" -> {
                addLong("Global Supplement ID", supplement?.globalSupplementId)
                add("Product Name", supplement?.productName)
                add("Brand", supplement?.brand)
                add("Manufacturer", supplement?.manufacturer)
                add("Supplement Type", supplement?.supplementType)
                add("Composition Summary", supplement?.compositionSummary)
                add("Age Group", supplement?.ageGroup)
                add("Base Unit", supplement?.baseUnit)
                addInt("Units Per Pack", supplement?.unitsPerPack)
                addBool("Loose Sale Allowed", supplement?.isLooseSaleAllowed)
                add("FSSAI License", supplement?.fssaiLicense)
                add("HSN Code", supplement?.hsnCode)
                add("GST Rate", supplement?.gstRate)
                addBool("Verified", supplement?.verified)
                addLong("Created By Chemist ID", supplement?.createdByChemistId)
                add("Created At", supplement?.createdAt)
                add("Updated At", supplement?.updatedAt)
                addBool("Is Active", supplement?.isActive)
                add("Image URL", supplement?.imageUrl)
                add("Description", supplement?.description)
                add("Advice", supplement?.advice)
            }
            "SURGICAL", "SURGICAL_CONSUMABLE" -> {
                addLong("Global Surgical ID", surgical?.globalSurgicalId)
                add("Product Name", surgical?.productName)
                add("Brand", surgical?.brand)
                add("Manufacturer", surgical?.manufacturer)
                add("Material", surgical?.material)
                add("Base Unit", surgical?.baseUnit)
                addInt("Units Per Pack", surgical?.unitsPerPack)
                addBool("Sterile", surgical?.sterile)
                addBool("Disposable", surgical?.disposable)
                add("Intended Use", surgical?.intendedUse)
                add("Size Specification", surgical?.sizeSpecification)
                add("HSN Code", surgical?.hsnCode)
                add("GST Rate", surgical?.gstRate)
                addBool("Verified", surgical?.verified)
                addLong("Created By Chemist ID", surgical?.createdByChemistId)
                add("Created At", surgical?.createdAt)
                add("Updated At", surgical?.updatedAt)
                addBool("Is Active", surgical?.isActive)
                add("Image URL", surgical?.imageUrl)
                add("Description", surgical?.description)
                add("Advice", surgical?.advice)
            }
        }

        val title = stock.productName
        return SellItemDetails(
            title = title,
            category = category,
            details = details
        )
    }
}
