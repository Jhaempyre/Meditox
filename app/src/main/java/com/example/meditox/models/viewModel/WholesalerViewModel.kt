package com.example.meditox.models.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.database.entity.WholesalerEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WholesalerViewModel(context: Context) : ViewModel() {

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(WholesalerViewModel::class.java)) {
                        return WholesalerViewModel(context) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }

    private val wholesalerDao = MeditoxDatabase.getDatabase(context).wholesalerDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val wholesalers: StateFlow<List<WholesalerEntity>> = _searchQuery
        .combine(wholesalerDao.getAllWholesalers()) { query, list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter { wholesaler ->
                    wholesaler.wholesalerName.contains(query, ignoreCase = true) ||
                            (wholesaler.contactPerson?.contains(query, ignoreCase = true) == true) ||
                            (wholesaler.city?.contains(query, ignoreCase = true) == true) ||
                            (wholesaler.phoneNumber?.contains(query, ignoreCase = true) == true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _isSearchActive.value = !_isSearchActive.value
        if (!_isSearchActive.value) {
            _searchQuery.value = ""
        }
    }

    private val apiService = com.example.meditox.services.ApiClient.createWholesalerApiService(context)

    fun deleteWholesaler(wholesalerId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteWholesaler(wholesalerId)
                if (response.isSuccessful && response.body()?.success == true) {
                    wholesalerDao.deleteById(wholesalerId)
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Failed to delete wholesaler")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error deleting wholesaler")
            }
        }
    }
}
