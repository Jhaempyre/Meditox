package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalGeneralFmcgEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalGeneralFmcgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fmcgProducts: List<GlobalGeneralFmcgEntity>)

    @Query("DELETE FROM global_general_fmcg")
    suspend fun deleteAll()

    @Query("SELECT * FROM global_general_fmcg")
    fun getAllFmcgProducts(): Flow<List<GlobalGeneralFmcgEntity>>

    @Query("SELECT COUNT(*) FROM global_general_fmcg")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fmcg: GlobalGeneralFmcgEntity)

    @Query("SELECT * FROM global_general_fmcg WHERE product_name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY product_name ASC")
    fun searchFmcg(query: String): Flow<List<GlobalGeneralFmcgEntity>>
}
