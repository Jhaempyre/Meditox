package com.example.meditox.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meditox.database.dao.GlobalDrugDao
import com.example.meditox.database.entity.ChemistProductMasterEntity
import com.example.meditox.database.entity.GlobalDrugEntity
import com.example.meditox.database.entity.GlobalCosmeticEntity
import com.example.meditox.database.entity.GlobalGeneralFmcgEntity
import com.example.meditox.database.entity.GlobalMedicalDeviceEntity
import com.example.meditox.database.entity.GlobalSupplementEntity
import com.example.meditox.database.entity.GlobalSurgicalConsumableEntity
import timber.log.Timber

@Database(
    entities = [
        GlobalDrugEntity::class,
        GlobalCosmeticEntity::class,
        GlobalGeneralFmcgEntity::class,
        GlobalMedicalDeviceEntity::class,
        GlobalSupplementEntity::class,
        GlobalSurgicalConsumableEntity::class,
        ChemistProductMasterEntity::class

    ],
    version = 4,
    exportSchema = false
)
abstract class MeditoxDatabase : RoomDatabase() {
    
    abstract fun globalDrugDao(): GlobalDrugDao
    abstract fun globalCosmeticDao(): com.example.meditox.database.dao.GlobalCosmeticDao
    abstract fun globalGeneralFmcgDao(): com.example.meditox.database.dao.GlobalGeneralFmcgDao
    abstract fun globalMedicalDeviceDao(): com.example.meditox.database.dao.GlobalMedicalDeviceDao
    abstract fun globalSupplementDao(): com.example.meditox.database.dao.GlobalSupplementDao
    abstract fun globalSurgicalConsumableDao(): com.example.meditox.database.dao.GlobalSurgicalConsumableDao
    abstract fun chemistProductMasterDao(): com.example.meditox.database.dao.ChemistProductMasterDao
    
    companion object {
        @Volatile
        private var INSTANCE: MeditoxDatabase? = null
        
        fun getDatabase(context: Context): MeditoxDatabase {
            Timber.tag("MeditoxDatabase").d("Getting database instance")
            return INSTANCE ?: synchronized(this) {
                Timber.tag("MeditoxDatabase").d("Creating new database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeditoxDatabase::class.java,
                    "meditox_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                Timber.tag("MeditoxDatabase").d("Database instance created successfully")
                instance
            }
        }
    }
}
