package com.example.meditox.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meditox.database.entity.GlobalMedicalDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlobalMedicalDeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<GlobalMedicalDeviceEntity>)

    @Query("DELETE FROM global_medical_device")
    suspend fun deleteAll()

    @Query("SELECT * FROM global_medical_device")
    fun getAllDevices(): Flow<List<GlobalMedicalDeviceEntity>>

    @Query("SELECT COUNT(*) FROM global_medical_device")
    suspend fun getCount(): Int
}
