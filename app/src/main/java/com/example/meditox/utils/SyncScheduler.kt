package com.example.meditox.utils

import android.content.Context
import androidx.work.*
import com.example.meditox.workers.DataSyncWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val TAG = "SyncScheduler"
    private const val DAILY_SYNC_WORK_NAME = "daily_data_sync"
    private const val MANUAL_SYNC_WORK_NAME = "manual_data_sync"
    
    /**
     * Schedule daily automatic sync
     * Runs once every 24 hours when device has network connection
     */
    fun scheduleDailySync(context: Context) {
        Timber.tag(TAG).d("📅 Scheduling daily sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                DAILY_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
        
        Timber.tag(TAG).d("✅ Daily sync scheduled")
    }
    
    /**
     * Trigger manual sync immediately
     * @return WorkRequest ID for tracking progress
     */
    fun triggerManualSync(context: Context): String {
        Timber.tag(TAG).d("🔄 Triggering manual sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                MANUAL_SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        
        Timber.tag(TAG).d("✅ Manual sync triggered with ID: ${syncRequest.id}")
        return syncRequest.id.toString()
    }
    
    /**
     * Cancel daily sync
     */
    fun cancelDailySync(context: Context) {
        Timber.tag(TAG).d("🛑 Cancelling daily sync")
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_SYNC_WORK_NAME)
    }
    
    /**
     * Get sync work info for observing progress
     */
    fun getSyncWorkInfo(context: Context) = 
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(MANUAL_SYNC_WORK_NAME)
}
