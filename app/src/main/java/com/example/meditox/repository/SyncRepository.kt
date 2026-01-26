package com.example.meditox.repository

import android.content.Context
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.services.GlobalDataSyncApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SyncRepository(
    private val context: Context,
    private val apiService: GlobalDataSyncApiService
) {
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalDrugDao = database.globalDrugDao()
    private val globalCosmeticDao = database.globalCosmeticDao()
    private val globalGeneralFmcgDao = database.globalGeneralFmcgDao()
    private val globalMedicalDeviceDao = database.globalMedicalDeviceDao()
    private val globalSupplementDao = database.globalSupplementDao()
    private val globalSurgicalConsumableDao = database.globalSurgicalConsumableDao()
    
    companion object {
        private const val TAG = "SyncRepository"
        private const val PAGE_SIZE = 50 // Fetch 50 records per page
    }
    
    /**
     * Sync Global Drugs from server to local database
     * @param onProgress Callback with (currentRecords, totalRecords, currentPage, totalPages)
     * @return Result with success/failure
     */
    suspend fun syncGlobalDrugs(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global Drugs sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            // Fetch first page to get pagination info
            Timber.tag(TAG).d("📥 Fetching page $currentPage")
            val firstResponse = apiService.getGlobalDrugs(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) {
                val errorMsg = "API call failed: ${firstResponse.code()} - ${firstResponse.message()}"
                Timber.tag(TAG).e(errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val firstBody = firstResponse.body()
            if (firstBody == null || !firstBody.success) {
                val errorMsg = "Invalid response body or success=false"
                Timber.tag(TAG).e(errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            Timber.tag(TAG).d("📊 Total records: $totalRecords, Total pages: $totalPages")
            
            // Insert first page data
            val firstEntities = firstData.drugs.map { it.toEntity() }
            globalDrugDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            
            Timber.tag(TAG).d("✅ Page $currentPage synced: ${firstEntities.size} records")
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            // Fetch remaining pages
            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching page $currentPage of $totalPages")
                
                val response = apiService.getGlobalDrugs(page = currentPage, limit = PAGE_SIZE)
                
                if (!response.isSuccessful) {
                    val errorMsg = "Page $currentPage failed: ${response.code()}"
                    Timber.tag(TAG).e(errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
                
                val body = response.body()
                if (body == null || !body.success) {
                    val errorMsg = "Invalid response for page $currentPage"
                    Timber.tag(TAG).e(errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
                
                val entities = body.data.drugs.map { it.toEntity() }
                globalDrugDao.insertAll(entities)
                totalSynced += entities.size
                
                Timber.tag(TAG).d("✅ Page $currentPage synced: ${entities.size} records")
                onProgress(totalSynced, totalRecords, currentPage, totalPages)
                
                currentPage++
            }
            
            val finalCount = globalDrugDao.getCount()
            Timber.tag(TAG).d("🎉 Sync completed! Total in DB: $finalCount")
            
            Result.success(SyncResult(
                totalRecords = totalSynced,
                totalPages = totalPages,
                success = true
            ))
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Sync failed with exception")
            Result.failure(e)
        }
    }
    
    /**
     * Sync Global Cosmetics from server to local database
     */
    suspend fun syncGlobalCosmetics(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global Cosmetics sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            // Fetch first page
            Timber.tag(TAG).d("📥 Fetching cosmetics page $currentPage")
            val firstResponse = apiService.getGlobalCosmetics(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) {
                val errorMsg = "API call failed: ${firstResponse.code()} - ${firstResponse.message()}"
                Timber.tag(TAG).e(errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val firstBody = firstResponse.body()
            if (firstBody == null || !firstBody.success) {
                val errorMsg = "Invalid response body or success=false"
                Timber.tag(TAG).e(errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            Timber.tag(TAG).d("📊 Total cosmetics: $totalRecords, Total pages: $totalPages")
            
            // Insert first page
            val firstEntities = firstData.cosmetics.map { it.toEntity() }
            globalCosmeticDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            
            Timber.tag(TAG).d("✅ Page $currentPage synced: ${firstEntities.size} records")
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            // Fetch remaining pages
            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching cosmetics page $currentPage of $totalPages")
                
                val response = apiService.getGlobalCosmetics(page = currentPage, limit = PAGE_SIZE)
                
                if (!response.isSuccessful) {
                    val errorMsg = "Page $currentPage failed: ${response.code()}"
                    Timber.tag(TAG).e(errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
                
                val body = response.body()
                if (body == null || !body.success) {
                    val errorMsg = "Invalid response for page $currentPage"
                    Timber.tag(TAG).e(errorMsg)
                    return@withContext Result.failure(Exception(errorMsg))
                }
                
                val entities = body.data.cosmetics.map { it.toEntity() }
                globalCosmeticDao.insertAll(entities)
                totalSynced += entities.size
                
                Timber.tag(TAG).d("✅ Page $currentPage synced: ${entities.size} records")
                onProgress(totalSynced, totalRecords, currentPage, totalPages)
                
                currentPage++
            }
            
            val finalCount = globalCosmeticDao.getCount()
            Timber.tag(TAG).d("🎉 Sync completed! Total cosmetics in DB: $finalCount")
            
            Result.success(SyncResult(
                totalRecords = totalSynced,
                totalPages = totalPages,
                success = true
            ))
            
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Sync failed with exception")
            Result.failure(e)
        }
    }
    
    /**
     * Get current count of drugs in local database
     */
    suspend fun getLocalDrugCount(): Int = withContext(Dispatchers.IO) {
        val count = globalDrugDao.getCount()
        Timber.tag(TAG).d("Local drug count: $count")
        count
    }
    
    /**
     * Clear all drugs from local database
     */
    suspend fun clearAllDrugs() = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🗑️ Clearing all drugs from database")
        globalDrugDao.deleteAll()
        Timber.tag(TAG).d("✅ All drugs cleared")
    }

    suspend fun getLocalCosmeticCount(): Int = withContext(Dispatchers.IO) {
        val count = globalCosmeticDao.getCount()
        Timber.tag(TAG).d("Local cosmetic count: $count")
        count
    }

    suspend fun getLocalFmcgCount(): Int = withContext(Dispatchers.IO) {
        val count = globalGeneralFmcgDao.getCount()
        Timber.tag(TAG).d("Local FMCG count: $count")
        count
    }

    suspend fun getLocalDeviceCount(): Int = withContext(Dispatchers.IO) {
        val count = globalMedicalDeviceDao.getCount()
        Timber.tag(TAG).d("Local device count: $count")
        count
    }

    suspend fun getLocalSupplementCount(): Int = withContext(Dispatchers.IO) {
        val count = globalSupplementDao.getCount()
        Timber.tag(TAG).d("Local supplement count: $count")
        count
    }

    suspend fun getLocalSurgicalCount(): Int = withContext(Dispatchers.IO) {
        val count = globalSurgicalConsumableDao.getCount()
        Timber.tag(TAG).d("Local surgical count: $count")
        count
    }
}

data class SyncResult(
    val totalRecords: Int,
    val totalPages: Int,
    val success: Boolean
)
