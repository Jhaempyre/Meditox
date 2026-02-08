package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalCosmeticEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalCosmeticDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cosmetics: List<GlobalCosmeticEntity>)

    @Query("DELETE FROM global_cosmetic")
    suspend fun deleteAll()

    @Query("SELECT * FROM global_cosmetic")
    fun getAllCosmetics(): Flow<List<GlobalCosmeticEntity>>

    @Query("SELECT COUNT(*) FROM global_cosmetic")
    suspend fun getCount(): Int

    @Query("SELECT * FROM global_cosmetic WHERE product_name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchCosmetics(query: String): Flow<List<GlobalCosmeticEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cosmetic: GlobalCosmeticEntity)
}
