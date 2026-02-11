package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.WholesalerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WholesalerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wholesalers: List<WholesalerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wholesaler: WholesalerEntity)

    @androidx.room.Update
    suspend fun update(wholesaler: WholesalerEntity)

    @Query("DELETE FROM wholesaler WHERE wholesaler_id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wholesaler")
    suspend fun deleteAll()

    @Query("SELECT * FROM wholesaler")
    fun getAllWholesalers(): Flow<List<WholesalerEntity>>

    @Query("SELECT * FROM wholesaler WHERE added_by_chemist_id = :chemistId")
    fun getWholesalersByChemistId(chemistId: Long): Flow<List<WholesalerEntity>>

    @Query("SELECT COUNT(*) FROM wholesaler")
    suspend fun getCount(): Int
}
