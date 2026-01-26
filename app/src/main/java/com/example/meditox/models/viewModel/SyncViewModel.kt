package com.example.meditox.models.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.meditox.repository.SyncRepository
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.SyncPreferences
import com.example.meditox.utils.SyncScheduler
import com.example.meditox.workers.DataSyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class SyncViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        private const val TAG = "SyncViewModel"
    }
    
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val repository = SyncRepository(
        context,
        ApiClient.createGlobalDataSyncApiService()
    )
    
    init {
        Timber.tag(TAG).d("SyncViewModel initialized")
        loadSyncMetadata()
        observeSyncProgress()
    }
    
    private fun loadSyncMetadata() {
        viewModelScope.launch {
            Timber.tag(TAG).d("Loading sync metadata")
            
            combine(
                SyncPreferences.getLastSyncTime(context),
                SyncPreferences.getLastSyncStatus(context),
                SyncPreferences.getTotalSyncedRecords(context)
            ) { lastTime, status, totalRecords ->
                Triple(lastTime, status, totalRecords)
            }.collect { (lastTime, status, totalRecords) ->
                _syncState.update { currentState ->
                    currentState.copy(
                        lastSyncTime = lastTime,
                        lastSyncStatus = status ?: "Never synced",
                        totalRecordsSynced = totalRecords?.toInt() ?: 0
                    )
                }
            }
        }
        
        // Load local drug count
        viewModelScope.launch {
            val count = repository.getLocalDrugCount()
            val cosmeticCount = repository.getLocalCosmeticCount()
            val fmcgCount = repository.getLocalFmcgCount()
            val deviceCount = repository.getLocalDeviceCount()
            val supplementCount = repository.getLocalSupplementCount()
            val surgicalCount = repository.getLocalSurgicalCount()
            
            Timber.tag(TAG).d("Local counts: Drugs=$count, Cosmetic=$cosmeticCount, FMCG=$fmcgCount, Device=$deviceCount, Supp=$supplementCount, Surg=$surgicalCount")
            
            _syncState.update { 
                it.copy(
                    localDrugCount = count,
                    localCosmeticCount = cosmeticCount,
                    localFmcgCount = fmcgCount,
                    localDeviceCount = deviceCount,
                    localSupplementCount = supplementCount,
                    localSurgicalCount = surgicalCount
                ) 
            }
        }
    }
    
    private fun observeSyncProgress() {
        SyncScheduler.getSyncWorkInfo(context).observeForever { workInfos ->
            workInfos?.firstOrNull()?.let { workInfo ->
                Timber.tag(TAG).d("Work state: ${workInfo.state}")
                
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress
                        val current = progress.getInt(DataSyncWorker.PROGRESS_CURRENT, 0)
                        val total = progress.getInt(DataSyncWorker.PROGRESS_TOTAL, 0)
                        val page = progress.getInt(DataSyncWorker.PROGRESS_PAGE, 0)
                        val totalPages = progress.getInt(DataSyncWorker.PROGRESS_TOTAL_PAGES, 0)
                        val status = progress.getString(DataSyncWorker.PROGRESS_STATUS) ?: "Syncing..."
                        
                        Timber.tag(TAG).d("Progress: $current/$total, page $page/$totalPages")
                        
                        _syncState.update {
                            it.copy(
                                isSyncing = true,
                                currentProgress = current,
                                totalProgress = total,
                                currentPage = page,
                                totalPages = totalPages,
                                syncStatus = status
                            )
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Timber.tag(TAG).d("Sync succeeded")
                        _syncState.update {
                            it.copy(
                                isSyncing = false,
                                syncStatus = "Completed successfully"
                            )
                        }
                        loadSyncMetadata() // Reload metadata
                    }
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.outputData.getString("error") ?: "Unknown error"
                        Timber.tag(TAG).e("Sync failed: $error")
                        _syncState.update {
                            it.copy(
                                isSyncing = false,
                                syncStatus = "Failed: $error"
                            )
                        }
                    }
                    WorkInfo.State.CANCELLED -> {
                        Timber.tag(TAG).d("Sync cancelled")
                        _syncState.update {
                            it.copy(
                                isSyncing = false,
                                syncStatus = "Cancelled"
                            )
                        }
                    }
                    else -> {
                        // ENQUEUED, BLOCKED
                        _syncState.update {
                            it.copy(
                                isSyncing = false,
                                syncStatus = "Waiting..."
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun triggerManualSync() {
        Timber.tag(TAG).d("Manual sync triggered by user")
        _syncState.update {
            it.copy(
                isSyncing = true,
                syncStatus = "Starting...",
                currentProgress = 0,
                totalProgress = 0
            )
        }
        SyncScheduler.triggerManualSync(context)
    }
    
    fun getFormattedLastSyncTime(): String {
        val lastTime = _syncState.value.lastSyncTime ?: return "Never"
        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(lastTime))
    }
}

data class SyncState(
    val isSyncing: Boolean = false,
    val currentProgress: Int = 0,
    val totalProgress: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val syncStatus: String = "Idle",
    val lastSyncTime: Long? = null,
    val lastSyncStatus: String = "Never synced",
    val totalRecordsSynced: Int = 0,
    val localDrugCount: Int = 0,
    val localCosmeticCount: Int = 0,
    val localFmcgCount: Int = 0,
    val localDeviceCount: Int = 0,
    val localSupplementCount: Int = 0,
    val localSurgicalCount: Int = 0
)
