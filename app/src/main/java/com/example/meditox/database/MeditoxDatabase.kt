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
import com.example.meditox.database.entity.WholesalerEntity
import com.example.meditox.database.entity.ChemistBatchStockEntity
import timber.log.Timber
import com.example.meditox.database.dao.*

@Database(
    entities = [
        GlobalDrugEntity::class,
        GlobalCosmeticEntity::class,
        GlobalGeneralFmcgEntity::class,
        GlobalMedicalDeviceEntity::class,
        GlobalSupplementEntity::class,
        GlobalSurgicalConsumableEntity::class,
        ChemistProductMasterEntity::class,
        WholesalerEntity::class,
        ChemistBatchStockEntity::class

    ],
    version = 7,
    exportSchema = false
)
abstract class MeditoxDatabase : RoomDatabase() {
    
    abstract fun globalDrugDao(): GlobalDrugDao
    abstract fun globalCosmeticDao(): GlobalCosmeticDao
    abstract fun globalGeneralFmcgDao(): GlobalGeneralFmcgDao
    abstract fun globalMedicalDeviceDao(): GlobalMedicalDeviceDao
    abstract fun globalSupplementDao(): GlobalSupplementDao
    abstract fun globalSurgicalConsumableDao(): GlobalSurgicalConsumableDao
    abstract fun chemistProductMasterDao(): ChemistProductMasterDao
    abstract fun wholesalerDao(): WholesalerDao
    abstract fun chemistBatchStockDao(): ChemistBatchStockDao
    
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
