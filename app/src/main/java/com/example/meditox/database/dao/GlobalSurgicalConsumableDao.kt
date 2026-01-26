package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalSurgicalConsumableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalSurgicalConsumableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(consumables: List<GlobalSurgicalConsumableEntity>)

    @Query("DELETE FROM global_surgical_consumable")
    suspend fun deleteAll()

    @Query("SELECT * FROM global_surgical_consumable")
    fun getAllConsumables(): Flow<List<GlobalSurgicalConsumableEntity>>

    @Query("SELECT COUNT(*) FROM global_surgical_consumable")
    suspend fun getCount(): Int
}
