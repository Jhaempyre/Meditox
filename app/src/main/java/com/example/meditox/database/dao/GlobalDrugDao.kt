package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalDrugEntity
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface GlobalDrugDao {
    
    @Query("SELECT * FROM global_drugs ORDER BY brand_name ASC")
    fun getAllDrugs(): Flow<List<GlobalDrugEntity>>
    
    @Query("SELECT * FROM global_drugs WHERE global_drug_id = :id")
    suspend fun getDrugById(id: Long): GlobalDrugEntity?
    
    @Query("SELECT * FROM global_drugs WHERE brand_name LIKE '%' || :query || '%' OR generic_name LIKE '%' || :query || '%'")
    fun searchDrugs(query: String): Flow<List<GlobalDrugEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drugs: List<GlobalDrugEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drug: GlobalDrugEntity)
    
    @Query("DELETE FROM global_drugs")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM global_drugs")
    suspend fun getCount(): Int
    
    @Query("SELECT COUNT(*) FROM global_drugs WHERE verified = 1")
    suspend fun getVerifiedCount(): Int
}
