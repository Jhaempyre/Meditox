package com.example.meditox.repository

import android.content.Context
import com.example.meditox.database.MeditoxDatabase
import com.example.meditox.services.ChemistProductApiService
import com.example.meditox.services.WholesalerApiService
import com.example.meditox.services.GlobalDataSyncApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SyncRepository(
    private val context: Context,
    private val apiService: GlobalDataSyncApiService,
    private val chemistProductApiService: ChemistProductApiService,
    private val wholesalerApiService: WholesalerApiService
) {
    private val database = MeditoxDatabase.getDatabase(context)
    private val globalDrugDao = database.globalDrugDao()
    private val globalCosmeticDao = database.globalCosmeticDao()
    private val globalGeneralFmcgDao = database.globalGeneralFmcgDao()
    private val globalMedicalDeviceDao = database.globalMedicalDeviceDao()
    private val globalSupplementDao = database.globalSupplementDao()
    private val globalSurgicalConsumableDao = database.globalSurgicalConsumableDao()
    private val chemistProductMasterDao = database.chemistProductMasterDao()
    private val wholesalerDao = database.wholesalerDao()
    
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
     * Sync Global FMCG from server to local database
     */
    suspend fun syncGlobalFmcg(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global FMCG sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            Timber.tag(TAG).d("📥 Fetching FMCG page $currentPage")
            val firstResponse = apiService.getGlobalGeneralFmcg(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) {
                return@withContext Result.failure(Exception("API call failed: ${firstResponse.code()}"))
            }
            
            val firstBody = firstResponse.body()
            if (firstBody == null || !firstBody.success) {
                return@withContext Result.failure(Exception("Invalid response body"))
            }
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            val firstEntities = firstData.generalFmcg.map { it.toEntity() }
            globalGeneralFmcgDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching FMCG page $currentPage of $totalPages")
                val response = apiService.getGlobalGeneralFmcg(page = currentPage, limit = PAGE_SIZE)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val entities = response.body()!!.data.generalFmcg.map { it.toEntity() }
                    globalGeneralFmcgDao.insertAll(entities)
                    totalSynced += entities.size
                    onProgress(totalSynced, totalRecords, currentPage, totalPages)
                }
                currentPage++
            }
            
            Result.success(SyncResult(totalSynced, totalPages, true))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ FMCG Sync failed")
            Result.failure(e)
        }
    }

    /**
     * Sync Global Medical Devices from server to local database
     */
    suspend fun syncGlobalMedicalDevices(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global Medical Devices sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            Timber.tag(TAG).d("📥 Fetching Medical Devices page $currentPage")
            val firstResponse = apiService.getGlobalMedicalDevices(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) return@withContext Result.failure(Exception("API call failed"))
            
            val firstBody = firstResponse.body() ?: return@withContext Result.failure(Exception("Empty body"))
            if (!firstBody.success) return@withContext Result.failure(Exception("API Error"))
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            val firstEntities = firstData.medicalDevices.map { it.toEntity() }
            globalMedicalDeviceDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching Devices page $currentPage")
                val response = apiService.getGlobalMedicalDevices(page = currentPage, limit = PAGE_SIZE)
                if (response.isSuccessful && response.body()?.success == true) {
                    val entities = response.body()!!.data.medicalDevices.map { it.toEntity() }
                    globalMedicalDeviceDao.insertAll(entities)
                    totalSynced += entities.size
                    onProgress(totalSynced, totalRecords, currentPage, totalPages)
                }
                currentPage++
            }
            Result.success(SyncResult(totalSynced, totalPages, true))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Medical Devices Sync failed")
            Result.failure(e)
        }
    }

    /**
     * Sync Global Supplements from server to local database
     */
    suspend fun syncGlobalSupplements(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global Supplements sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            Timber.tag(TAG).d("📥 Fetching Supplements page $currentPage")
            val firstResponse = apiService.getGlobalSupplements(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) return@withContext Result.failure(Exception("API call failed"))
            
            val firstBody = firstResponse.body() ?: return@withContext Result.failure(Exception("Empty body"))
            if (!firstBody.success) return@withContext Result.failure(Exception("API Error"))
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            val firstEntities = firstData.supplements.map { it.toEntity() }
            globalSupplementDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            currentPage++
            while (currentPage <= totalPages) {
                val response = apiService.getGlobalSupplements(page = currentPage, limit = PAGE_SIZE)
                if (response.isSuccessful && response.body()?.success == true) {
                    val entities = response.body()!!.data.supplements.map { it.toEntity() }
                    globalSupplementDao.insertAll(entities)
                    totalSynced += entities.size
                    onProgress(totalSynced, totalRecords, currentPage, totalPages)
                }
                currentPage++
            }
            Result.success(SyncResult(totalSynced, totalPages, true))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Supplements Sync failed")
            Result.failure(e)
        }
    }

    /**
     * Sync Global Surgical Consumables from server to local database
     */
    suspend fun syncGlobalSurgicalConsumables(
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Global Surgical Consumables sync")
        
        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0
        
        try {
            Timber.tag(TAG).d("📥 Fetching Surgical Items page $currentPage")
            val firstResponse = apiService.getGlobalSurgicalConsumables(page = currentPage, limit = PAGE_SIZE)
            
            if (!firstResponse.isSuccessful) return@withContext Result.failure(Exception("API call failed"))
            
            val firstBody = firstResponse.body() ?: return@withContext Result.failure(Exception("Empty body"))
            if (!firstBody.success) return@withContext Result.failure(Exception("API Error"))
            
            val firstData = firstBody.data
            totalRecords = firstData.pagination.total_records
            totalPages = firstData.pagination.total_pages
            
            val firstEntities = firstData.surgicalConsumables.map { it.toEntity() }
            globalSurgicalConsumableDao.insertAll(firstEntities)
            totalSynced += firstEntities.size
            onProgress(totalSynced, totalRecords, currentPage, totalPages)
            
            currentPage++
            while (currentPage <= totalPages) {
                val response = apiService.getGlobalSurgicalConsumables(page = currentPage, limit = PAGE_SIZE)
                if (response.isSuccessful && response.body()?.success == true) {
                    val entities = response.body()!!.data.surgicalConsumables.map { it.toEntity() }
                    globalSurgicalConsumableDao.insertAll(entities)
                    totalSynced += entities.size
                    onProgress(totalSynced, totalRecords, currentPage, totalPages)
                }
                currentPage++
            }
            Result.success(SyncResult(totalSynced, totalPages, true))
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Surgical Sync failed")
            Result.failure(e)
        }
    }

    /**
     * Sync Chemist Products from server to local database
     */
    suspend fun syncChemistProducts(
        chemistId: Long,
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Chemist Products sync for chemistId=$chemistId")

        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0

        try {
            Timber.tag(TAG).d("📥 Fetching chemist products page $currentPage")
            val firstResponse = chemistProductApiService.getChemistProducts(
                chemistId = chemistId,
                page = currentPage,
                limit = PAGE_SIZE
            )

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

            Timber.tag(TAG).d("📊 Total chemist products: $totalRecords, Total pages: $totalPages")

            val firstEntities = firstData.products.map { it.toEntity(chemistId) }
            chemistProductMasterDao.insertAll(firstEntities)
            totalSynced += firstEntities.size

            Timber.tag(TAG).d("✅ Page $currentPage synced: ${firstEntities.size} records")
            onProgress(totalSynced, totalRecords, currentPage, totalPages)

            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching chemist products page $currentPage of $totalPages")

                val response = chemistProductApiService.getChemistProducts(
                    chemistId = chemistId,
                    page = currentPage,
                    limit = PAGE_SIZE
                )

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

                val entities = body.data.products.map { it.toEntity(chemistId) }
                chemistProductMasterDao.insertAll(entities)
                totalSynced += entities.size

                Timber.tag(TAG).d("✅ Page $currentPage synced: ${entities.size} records")
                onProgress(totalSynced, totalRecords, currentPage, totalPages)

                currentPage++
            }

            val finalCount = chemistProductMasterDao.getCount()
            Timber.tag(TAG).d("🎉 Chemist Products sync completed! Total in DB: $finalCount")

            Result.success(
                SyncResult(
                    totalRecords = totalSynced,
                    totalPages = totalPages,
                    success = true
                )
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Chemist Products sync failed")
            Result.failure(e)
        }
    }

    /**
     * Sync Wholesalers by Chemist from server to local database
     */
    suspend fun syncWholesalers(
        chemistId: Long,
        onProgress: suspend (current: Int, total: Int, page: Int, totalPages: Int) -> Unit
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        Timber.tag(TAG).d("🔄 Starting Wholesalers sync for chemistId=$chemistId")

        var currentPage = 1
        var totalSynced = 0
        var totalRecords = 0
        var totalPages = 0

        try {
            Timber.tag(TAG).d("📥 Fetching wholesalers page $currentPage")
            val firstResponse = wholesalerApiService.getWholesalersByChemist(
                chemistId = chemistId,
                page = currentPage,
                limit = PAGE_SIZE
            )

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

            Timber.tag(TAG).d("📊 Total wholesalers: $totalRecords, Total pages: $totalPages")

            val firstEntities = firstData.wholesalers.map { it.toEntity() }
            wholesalerDao.insertAll(firstEntities)
            totalSynced += firstEntities.size

            Timber.tag(TAG).d("✅ Page $currentPage synced: ${firstEntities.size} records")
            onProgress(totalSynced, totalRecords, currentPage, totalPages)

            currentPage++
            while (currentPage <= totalPages) {
                Timber.tag(TAG).d("📥 Fetching wholesalers page $currentPage of $totalPages")

                val response = wholesalerApiService.getWholesalersByChemist(
                    chemistId = chemistId,
                    page = currentPage,
                    limit = PAGE_SIZE
                )

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

                val entities = body.data.wholesalers.map { it.toEntity() }
                wholesalerDao.insertAll(entities)
                totalSynced += entities.size

                Timber.tag(TAG).d("✅ Page $currentPage synced: ${entities.size} records")
                onProgress(totalSynced, totalRecords, currentPage, totalPages)

                currentPage++
            }

            val finalCount = wholesalerDao.getCount()
            Timber.tag(TAG).d("🎉 Wholesalers sync completed! Total in DB: $finalCount")

            Result.success(
                SyncResult(
                    totalRecords = totalSynced,
                    totalPages = totalPages,
                    success = true
                )
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ Wholesalers sync failed")
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

    suspend fun getLocalWholesalerCount(): Int = withContext(Dispatchers.IO) {
        val count = wholesalerDao.getCount()
        Timber.tag(TAG).d("Local wholesaler count: $count")
        count
    }

    suspend fun getLocalChemistProductCount(): Int = withContext(Dispatchers.IO) {
        val count = chemistProductMasterDao.getCount()
        Timber.tag(TAG).d("Local chemist product count: $count")
        count
    }
}

data class SyncResult(
    val totalRecords: Int,
    val totalPages: Int,
    val success: Boolean
)
