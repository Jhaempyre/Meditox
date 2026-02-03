package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.ChemistProductMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChemistProductMasterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ChemistProductMasterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ChemistProductMasterEntity)

    @Query("DELETE FROM chemist_product_master")
    suspend fun deleteAll()

    @Query("SELECT * FROM chemist_product_master")
    fun getAllProducts(): Flow<List<ChemistProductMasterEntity>>

    @Query("SELECT * FROM chemist_product_master WHERE chemist_id = :chemistId")
    fun getProductsByChemistId(chemistId: Long): Flow<List<ChemistProductMasterEntity>>

    @Query("SELECT COUNT(*) FROM chemist_product_master")
    suspend fun getCount(): Int
}
