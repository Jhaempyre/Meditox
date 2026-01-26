package com.example.meditox.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.meditox.repository.SyncRepository
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.SyncPreferences
import timber.log.Timber

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "DataSyncWorker"
        const val PROGRESS_CURRENT = "progress_current"
        const val PROGRESS_TOTAL = "progress_total"
        const val PROGRESS_PAGE = "progress_page"
        const val PROGRESS_TOTAL_PAGES = "progress_total_pages"
        const val PROGRESS_TABLE = "progress_table"
        const val PROGRESS_STATUS = "progress_status"
    }
    
    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("🚀 DataSyncWorker started")
        
        return try {
            // Create API service and repository
            val apiService = ApiClient.createGlobalDataSyncApiService()
            val repository = SyncRepository(applicationContext, apiService)
            
            // Update progress: Starting
            setProgress(workDataOf(
                PROGRESS_TABLE to "Global Drugs",
                PROGRESS_STATUS to "Starting sync...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0,
                PROGRESS_PAGE to 0,
                PROGRESS_TOTAL_PAGES to 0
            ))
            
            var totalSyncedRecords = 0
            
            // 1. Sync Global Drugs
            Timber.tag(TAG).d("📊 Starting Global Drugs sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Global Drugs",
                PROGRESS_STATUS to "Syncing Drugs...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val drugsResult = repository.syncGlobalDrugs { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Global Drugs",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }

            if (drugsResult.isSuccess) {
                totalSyncedRecords += drugsResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ Drugs sync successful")
            } else {
                Timber.tag(TAG).e(drugsResult.exceptionOrNull(), "❌ Drugs sync failed")
                // Optional: Return failure here if strict, or continue
            }

            // 2. Sync Global Cosmetics
            Timber.tag(TAG).d("📊 Starting Global Cosmetics sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Global Cosmetics",
                PROGRESS_STATUS to "Syncing Cosmetics...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val cosmeticsResult = repository.syncGlobalCosmetics { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Global Cosmetics",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }
            
            if (cosmeticsResult.isSuccess) {
                totalSyncedRecords += cosmeticsResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ Cosmetics sync successful")
            } else {
                Timber.tag(TAG).e(cosmeticsResult.exceptionOrNull(), "❌ Cosmetics sync failed")
            }
            
            // Check overall success (if at least one succeeded or both, depending on requirements)
            // For now, if both fail, we consider it a failure. If one succeeds, we consider success/partial.
            if (drugsResult.isSuccess || cosmeticsResult.isSuccess) {
                
                // Save sync metadata
                try {
                    SyncPreferences.setLastSyncTime(applicationContext, System.currentTimeMillis())
                    SyncPreferences.setLastSyncStatus(applicationContext, "Success") // Or "Partial Success"
                    SyncPreferences.setTotalSyncedRecords(applicationContext, totalSyncedRecords.toLong())
                    Timber.tag(TAG).d("📝 Sync metadata saved")
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to save sync metadata")
                }
                
                setProgress(workDataOf(
                    PROGRESS_TABLE to "All",
                    PROGRESS_STATUS to "Completed",
                    PROGRESS_CURRENT to totalSyncedRecords,
                    PROGRESS_TOTAL to totalSyncedRecords
                ))
                
                Result.success(workDataOf(
                    "total_records" to totalSyncedRecords
                ))
            } else {
                val error = drugsResult.exceptionOrNull() ?: cosmeticsResult.exceptionOrNull()
                Timber.tag(TAG).e(error, "❌ All syncs failed")
                Result.retry()
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "💥 Worker exception")
            
            try {
                SyncPreferences.setLastSyncStatus(applicationContext, "Error: ${e.message}")
            } catch (prefError: Exception) {
                Timber.tag(TAG).e(prefError, "Failed to save exception status")
            }
            
            setProgress(workDataOf(
                PROGRESS_TABLE to "Global Drugs",
                PROGRESS_STATUS to "Error: ${e.message}",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            Result.failure(workDataOf("error" to (e.message ?: "Unknown error")))
        }
    }
}
