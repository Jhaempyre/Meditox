package com.example.meditox.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

// Create DataStore extension (same pattern as DataStoreManager)
private val Context.syncDataStore by preferencesDataStore(name = "sync_preferences")

object SyncPreferences {
    private const val TAG = "SyncPreferences"
    
    private val LAST_SYNC_TIME_KEY = longPreferencesKey("last_sync_time")
    private val LAST_SYNC_STATUS_KEY = stringPreferencesKey("last_sync_status")
    private val TOTAL_SYNCED_RECORDS_KEY = longPreferencesKey("total_synced_records")
    
    suspend fun setLastSyncTime(context: Context, timestamp: Long) {
        Timber.tag(TAG).d("Setting last sync time: $timestamp")
        context.syncDataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME_KEY] = timestamp
        }
    }
    
    fun getLastSyncTime(context: Context): Flow<Long?> {
        return context.syncDataStore.data.map { preferences ->
            preferences[LAST_SYNC_TIME_KEY]
        }
    }
    
    suspend fun setLastSyncStatus(context: Context, status: String) {
        Timber.tag(TAG).d("Setting last sync status: $status")
        context.syncDataStore.edit { preferences ->
            preferences[LAST_SYNC_STATUS_KEY] = status
        }
    }
    
    fun getLastSyncStatus(context: Context): Flow<String?> {
        return context.syncDataStore.data.map { preferences ->
            preferences[LAST_SYNC_STATUS_KEY]
        }
    }
    
    suspend fun setTotalSyncedRecords(context: Context, count: Long) {
        Timber.tag(TAG).d("Setting total synced records: $count")
        context.syncDataStore.edit { preferences ->
            preferences[TOTAL_SYNCED_RECORDS_KEY] = count
        }
    }
    
    fun getTotalSyncedRecords(context: Context): Flow<Long?> {
        return context.syncDataStore.data.map { preferences ->
            preferences[TOTAL_SYNCED_RECORDS_KEY]
        }
    }
}
