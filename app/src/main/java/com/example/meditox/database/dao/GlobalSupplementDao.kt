package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalSupplementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalSupplementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(supplements: List<GlobalSupplementEntity>)

    @Query("DELETE FROM global_supplement")
    suspend fun deleteAll()

    @Query("SELECT * FROM global_supplement")
    fun getAllSupplements(): Flow<List<GlobalSupplementEntity>>

    @Query("SELECT COUNT(*) FROM global_supplement")
    suspend fun getCount(): Int
}
