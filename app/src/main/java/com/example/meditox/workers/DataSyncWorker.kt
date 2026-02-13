package com.example.meditox.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.meditox.repository.SyncRepository
import com.example.meditox.repository.SyncResult
import com.example.meditox.services.ApiClient
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SyncPreferences
import kotlinx.coroutines.flow.first
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
            val chemistProductApiService = ApiClient.createChemistProductApiService(applicationContext)
            val wholesalerApiService = ApiClient.createWholesalerApiService(applicationContext)
            val repository = SyncRepository(applicationContext, apiService, chemistProductApiService, wholesalerApiService)
            
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
            
            // 3. Sync Global FMCG
            Timber.tag(TAG).d("📊 Starting Global FMCG sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Global FMCG",
                PROGRESS_STATUS to "Syncing FMCG...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val fmcgResult = repository.syncGlobalFmcg { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Global FMCG",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }
            
            if (fmcgResult.isSuccess) {
                totalSyncedRecords += fmcgResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ FMCG sync successful")
            } else {
                Timber.tag(TAG).e(fmcgResult.exceptionOrNull(), "❌ FMCG sync failed")
            }

            // 4. Sync Global Medical Devices
            Timber.tag(TAG).d("📊 Starting Global Medical Devices sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Medical Devices",
                PROGRESS_STATUS to "Syncing Devices...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val devicesResult = repository.syncGlobalMedicalDevices { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Medical Devices",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }
            
            if (devicesResult.isSuccess) {
                totalSyncedRecords += devicesResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ Devices sync successful")
            } else {
                Timber.tag(TAG).e(devicesResult.exceptionOrNull(), "❌ Devices sync failed")
            }

            // 5. Sync Global Supplements
            Timber.tag(TAG).d("📊 Starting Global Supplements sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Supplements",
                PROGRESS_STATUS to "Syncing Supplements...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val supplementsResult = repository.syncGlobalSupplements { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Supplements",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }
            
            if (supplementsResult.isSuccess) {
                totalSyncedRecords += supplementsResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ Supplements sync successful")
            } else {
                Timber.tag(TAG).e(supplementsResult.exceptionOrNull(), "❌ Supplements sync failed")
            }

            // 6. Sync Global Surgical Consumables
            Timber.tag(TAG).d("📊 Starting Global Surgical Consumables sync")
            setProgress(workDataOf(
                PROGRESS_TABLE to "Surgical Items",
                PROGRESS_STATUS to "Syncing Surgical...",
                PROGRESS_CURRENT to 0,
                PROGRESS_TOTAL to 0
            ))
            
            val surgicalResult = repository.syncGlobalSurgicalConsumables { current, total, page, totalPages ->
                setProgress(workDataOf(
                    PROGRESS_TABLE to "Surgical Items",
                    PROGRESS_STATUS to "Syncing...",
                    PROGRESS_CURRENT to current,
                    PROGRESS_TOTAL to total,
                    PROGRESS_PAGE to page,
                    PROGRESS_TOTAL_PAGES to totalPages
                ))
            }
            
            if (surgicalResult.isSuccess) {
                totalSyncedRecords += surgicalResult.getOrNull()!!.totalRecords
                Timber.tag(TAG).d("✅ Surgical sync successful")
            } else {
                Timber.tag(TAG).e(surgicalResult.exceptionOrNull(), "❌ Surgical sync failed")
            }

            // 7. Sync Chemist Products
            var chemistProductsResult: kotlin.Result<SyncResult>? = null
            val shopDetails = DataStoreManager.getShopDetails(applicationContext).first()
            val chemistId = shopDetails?.chemistId

            if (chemistId != null) {
                Timber.tag(TAG).d("📊 Starting Chemist Products sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Chemist Products",
                        PROGRESS_STATUS to "Syncing Chemist Products...",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )

                chemistProductsResult = repository.syncChemistProducts(chemistId) { current, total, page, totalPages ->
                    setProgress(
                        workDataOf(
                            PROGRESS_TABLE to "Chemist Products",
                            PROGRESS_STATUS to "Syncing...",
                            PROGRESS_CURRENT to current,
                            PROGRESS_TOTAL to total,
                            PROGRESS_PAGE to page,
                            PROGRESS_TOTAL_PAGES to totalPages
                        )
                    )
                }

                if (chemistProductsResult!!.isSuccess) {
                    totalSyncedRecords += chemistProductsResult!!.getOrNull()!!.totalRecords
                    Timber.tag(TAG).d("✅ Chemist products sync successful")
                } else {
                    Timber.tag(TAG).e(chemistProductsResult!!.exceptionOrNull(), "❌ Chemist products sync failed")
                }
            } else {
                Timber.tag(TAG).w("⚠️ Chemist ID missing; skipping chemist products sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Chemist Products",
                        PROGRESS_STATUS to "Skipped: missing chemist ID",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )
            }

            // 8. Sync Batch Stock
            var batchStockResult: kotlin.Result<SyncResult>? = null
            if (chemistId != null) {
                Timber.tag(TAG).d("📊 Starting Batch Stock sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Batch Stock",
                        PROGRESS_STATUS to "Syncing Batch Stock...",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )

                batchStockResult = repository.syncChemistBatchStock(chemistId) { current, total, page, totalPages ->
                    setProgress(
                        workDataOf(
                            PROGRESS_TABLE to "Batch Stock",
                            PROGRESS_STATUS to "Syncing...",
                            PROGRESS_CURRENT to current,
                            PROGRESS_TOTAL to total,
                            PROGRESS_PAGE to page,
                            PROGRESS_TOTAL_PAGES to totalPages
                        )
                    )
                }

                if (batchStockResult!!.isSuccess) {
                    totalSyncedRecords += batchStockResult!!.getOrNull()!!.totalRecords
                    Timber.tag(TAG).d("✅ Batch Stock sync successful")
                } else {
                    Timber.tag(TAG).e(batchStockResult!!.exceptionOrNull(), "❌ Batch Stock sync failed")
                }
            } else {
                Timber.tag(TAG).w("⚠️ Chemist ID missing; skipping batch stock sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Batch Stock",
                        PROGRESS_STATUS to "Skipped: missing chemist ID",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )
            }

            // 9. Sync Wholesalers
            var wholesalersResult: kotlin.Result<SyncResult>? = null
            if (chemistId != null) {
                Timber.tag(TAG).d("📊 Starting Wholesalers sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Wholesalers",
                        PROGRESS_STATUS to "Syncing Wholesalers...",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )

                wholesalersResult = repository.syncWholesalers(chemistId) { current, total, page, totalPages ->
                    setProgress(
                        workDataOf(
                            PROGRESS_TABLE to "Wholesalers",
                            PROGRESS_STATUS to "Syncing...",
                            PROGRESS_CURRENT to current,
                            PROGRESS_TOTAL to total,
                            PROGRESS_PAGE to page,
                            PROGRESS_TOTAL_PAGES to totalPages
                        )
                    )
                }

                if (wholesalersResult!!.isSuccess) {
                    totalSyncedRecords += wholesalersResult!!.getOrNull()!!.totalRecords
                    Timber.tag(TAG).d("✅ Wholesalers sync successful")
                } else {
                    Timber.tag(TAG).e(wholesalersResult!!.exceptionOrNull(), "❌ Wholesalers sync failed")
                }
            } else {
                Timber.tag(TAG).w("⚠️ Chemist ID missing; skipping wholesalers sync")
                setProgress(
                    workDataOf(
                        PROGRESS_TABLE to "Wholesalers",
                        PROGRESS_STATUS to "Skipped: missing chemist ID",
                        PROGRESS_CURRENT to 0,
                        PROGRESS_TOTAL to 0
                    )
                )
            }
            
            // Check overall success (if at least one succeeded)
            if (drugsResult.isSuccess || cosmeticsResult.isSuccess || fmcgResult.isSuccess ||
                devicesResult.isSuccess || supplementsResult.isSuccess || surgicalResult.isSuccess ||
                (chemistProductsResult?.isSuccess == true) || (batchStockResult?.isSuccess == true) ||
                (wholesalersResult?.isSuccess == true)) {
                
                // Save sync metadata
                try {
                    SyncPreferences.setLastSyncTime(applicationContext, System.currentTimeMillis())
                    SyncPreferences.setLastSyncStatus(applicationContext, "Success")
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
                    ?: fmcgResult.exceptionOrNull() ?: devicesResult.exceptionOrNull()
                    ?: supplementsResult.exceptionOrNull() ?: surgicalResult.exceptionOrNull()
                    ?: chemistProductsResult?.exceptionOrNull()
                    ?: batchStockResult?.exceptionOrNull()
                    ?: wholesalersResult?.exceptionOrNull()
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
