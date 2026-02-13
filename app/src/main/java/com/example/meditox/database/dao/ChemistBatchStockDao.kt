package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.ChemistBatchStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChemistBatchStockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<ChemistBatchStockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stock: ChemistBatchStockEntity)

    @Query("DELETE FROM chemist_batch_stock")
    suspend fun deleteAll()

    @Query("SELECT * FROM chemist_batch_stock")
    fun getAll(): Flow<List<ChemistBatchStockEntity>>

    @Query("SELECT COUNT(*) FROM chemist_batch_stock")
    suspend fun getCount(): Int
}
