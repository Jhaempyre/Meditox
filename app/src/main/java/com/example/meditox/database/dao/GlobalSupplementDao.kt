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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplement: GlobalSupplementEntity)

    @Query("SELECT * FROM global_supplement WHERE product_name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY product_name ASC")
    fun searchSupplements(query: String): Flow<List<GlobalSupplementEntity>>
}
