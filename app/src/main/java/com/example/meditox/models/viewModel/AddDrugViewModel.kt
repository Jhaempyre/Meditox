package com.example.meditox.models.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.GlobalDrugEntity
import com.example.meditox.enums.DosageForm
import com.example.meditox.enums.DrugSchedule
import com.example.meditox.enums.RouteOfAdministration
import com.example.meditox.enums.SystemOfMedicine
import com.example.meditox.models.GlobalDrug.CreateGlobalDrugRequest
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import kotlinx.coroutines.FlowPreview
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddDrugViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "AddDrugViewModel"

        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddDrugViewModel::class.java)) {
                        return AddDrugViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    // Database and API
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalDrugDao = database.globalDrugDao()
    private val apiService = ApiClient.createGlobalDataSyncApiService()

    // UI State
    private val _uiState = MutableStateFlow(AddDrugUiState())
    val uiState: StateFlow<AddDrugUiState> = _uiState.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<GlobalDrugEntity>>(emptyList())
    val searchResults: StateFlow<List<GlobalDrugEntity>> = _searchResults.asStateFlow()

    // Form fields
    private val _formState = MutableStateFlow(DrugFormState())
    val formState: StateFlow<DrugFormState> = _formState.asStateFlow()

    // API result
    private val _createDrugResult = MutableStateFlow<ApiResult<GlobalDrugEntity>?>(null)
    val createDrugResult: StateFlow<ApiResult<GlobalDrugEntity>?> = _createDrugResult.asStateFlow()

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        globalDrugDao.searchDrugs(query).collect { results ->
                            _searchResults.value = results
                            Log.d(TAG, "Search results: ${results.size} drugs found for '$query'")
                        }
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // Auto-fill brand name if form is shown
        if (_formState.value.brandName.isEmpty()) {
            updateFormField { it.copy(brandName = query) }
        }
    }

    fun onDrugSelected(drug: GlobalDrugEntity) {
        Log.d(TAG, "Selected drug: ${drug.brandName} (ID: ${drug.globalDrugId})")
        println("MATCH FOUND: ${drug.brandName} - ${drug.strengthValue}${drug.strengthUnit}")
        // TODO: Future implementation - navigate to drug detail or add to inventory
    }

    fun updateFormField(update: (DrugFormState) -> DrugFormState) {
        _formState.value = update(_formState.value)
    }

    fun createDrug() {
        viewModelScope.launch {
            try {
                _createDrugResult.value = ApiResult.Loading
                _uiState.update { it.copy(isLoading = true) }

                // Get chemist ID from DataStore
                val shopDetails = DataStoreManager.getShopDetails(context).first()
                val chemistId = shopDetails?.chemistId ?: 1L

                val form = _formState.value
                val request = CreateGlobalDrugRequest(
                    brandName = form.brandName.trim(),
                    genericName = form.genericName.trim(),
                    manufacturer = form.manufacturer.trim(),
                    systemOfMedicine = form.systemOfMedicine.name,
                    dosageForm = form.dosageForm.name,
                    strengthValue = form.strengthValue.trim(),
                    strengthUnit = form.strengthUnit.trim(),
                    routeOfAdministration = form.routeOfAdministration.name,
                    baseUnit = form.baseUnit.trim(),
                    unitsPerPack = form.unitsPerPack.toIntOrNull() ?: 10,
                    looseSaleAllowed = form.looseSaleAllowed,
                    drugSchedule = form.drugSchedule.name,
                    prescriptionRequired = form.prescriptionRequired,
                    narcoticDrug = form.narcoticDrug,
                    regulatoryAuthority = form.regulatoryAuthority.trim(),
                    hsnCode = form.hsnCode.trim(),
                    gst = form.gst.toDoubleOrNull() ?: 12.0, // Default to 12.0 if parsing fails, but validation should prevent this
                    createdByChemistId = chemistId
                )

                Log.d(TAG, "Creating drug: ${form.brandName}")
                val response = apiService.createGlobalDrug(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val drugResponse = response.body()?.data
                    Log.d(TAG, "Drug created successfully: ${drugResponse?.globalDrugId}")

                    // Insert into local database
                    drugResponse?.let { drug ->
                        val entity = GlobalDrugEntity(
                            globalDrugId = drug.globalDrugId,
                            brandName = drug.brandName,
                            genericName = drug.genericName,
                            manufacturer = drug.manufacturer,
                            systemOfMedicine = drug.systemOfMedicine,
                            dosageForm = drug.dosageForm,
                            strengthValue = drug.strengthValue,
                            strengthUnit = drug.strengthUnit,
                            routeOfAdministration = drug.routeOfAdministration,
                            baseUnit = drug.baseUnit,
                            unitsPerPack = drug.unitsPerPack,
                            isLooseSaleAllowed = drug.looseSaleAllowed,
                            drugSchedule = drug.drugSchedule,
                            prescriptionRequired = drug.prescriptionRequired,
                            narcoticDrug = drug.narcoticDrug,
                            regulatoryAuthority = drug.regulatoryAuthority,
                            hsnCode = drug.hsnCode,
                            gst = drug.gst,
                            verified = drug.verified,
                            createdByChemistId = drug.createdByChemistId,
                            createdAt = drug.createdAt,
                            isActive = drug.isActive
                        )
                        globalDrugDao.insert(entity)
                        Log.d(TAG, "Drug saved to local DB")
                        
                        // Update total synced records count
                        val currentCount = SyncPreferences.getTotalSyncedRecords(context).first() ?: 0L
                        SyncPreferences.setTotalSyncedRecords(context, currentCount + 1)
                        Log.d(TAG, "Updated total synced records to: ${currentCount + 1}")

                        _createDrugResult.value = ApiResult.Success(entity)
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to create drug"
                    Log.e(TAG, "API Error: $errorMsg")
                    _createDrugResult.value = ApiResult.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating drug", e)
                _createDrugResult.value = ApiResult.Error(e.message ?: "Unexpected error")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun isFormValid(): Boolean {
        val form = _formState.value
        return form.brandName.isNotBlank() &&
                form.genericName.isNotBlank() &&
                form.manufacturer.isNotBlank() &&
                form.strengthValue.isNotBlank() &&
                form.gst.isNotBlank()
    }

    fun resetCreateResult() {
        _createDrugResult.value = null
    }

    fun resetForm() {
        _formState.value = DrugFormState()
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}

// UI State
data class AddDrugUiState(
    val isLoading: Boolean = false
)

// Form state for drug creation
data class DrugFormState(
    val brandName: String = "",
    val genericName: String = "",
    val manufacturer: String = "",
    val strengthValue: String = "",
    val strengthUnit: String = "mg",
    val baseUnit: String = "TABLET",
    val unitsPerPack: String = "10",
    val hsnCode: String = "3004",
    val regulatoryAuthority: String = "CDSCO",
    val systemOfMedicine: SystemOfMedicine = SystemOfMedicine.ALLOPATHY,
    val dosageForm: DosageForm = DosageForm.TABLET,
    val routeOfAdministration: RouteOfAdministration = RouteOfAdministration.ORAL,
    val drugSchedule: DrugSchedule = DrugSchedule.NONE,
    val looseSaleAllowed: Boolean = false,
    val prescriptionRequired: Boolean = false,
    val narcoticDrug: Boolean = false,
    val gst: String = ""
)
