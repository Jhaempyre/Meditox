package com.example.meditox.services

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.meditox.workers.LocationTrackingWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val formattedTime: String
)

object LocationTrackingService {
    private const val TAG = "LocationTrackingService"
    private const val WORK_NAME = "location_tracking_work"
    
    private val _locationUpdates = MutableStateFlow<List<LocationData>>(emptyList())
    val locationUpdates: StateFlow<List<LocationData>> = _locationUpdates.asStateFlow()
    
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    
    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()
    
    fun startLocationTracking(context: Context) {
        Log.d(TAG, "Starting location tracking service...")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationTrackingWorker>(
            10, TimeUnit.SECONDS,
            5, TimeUnit.SECONDS
        )
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            locationWorkRequest
        )
        
        // Observe work results
        WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(WORK_NAME)
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> {
                            _isTracking.value = true
                            _lastError.value = null
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            val outputData = workInfo.outputData
                            val latitude = outputData.getDouble(LocationTrackingWorker.KEY_LATITUDE, 0.0)
                            val longitude = outputData.getDouble(LocationTrackingWorker.KEY_LONGITUDE, 0.0)
                            val accuracy = outputData.getFloat(LocationTrackingWorker.KEY_ACCURACY, 0f)
                            val timestamp = outputData.getLong(LocationTrackingWorker.KEY_TIMESTAMP, 0L)
                            
                            if (latitude != 0.0 && longitude != 0.0) {
                                val locationData = LocationData(
                                    latitude = latitude,
                                    longitude = longitude,
                                    accuracy = accuracy,
                                    timestamp = timestamp,
                                    formattedTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                        .format(java.util.Date(timestamp))
                                )
                                
                                val currentList = _locationUpdates.value.toMutableList()
                                currentList.add(0, locationData) // Add to beginning
                                if (currentList.size > 50) { // Keep only last 50 locations
                                    currentList.removeAt(currentList.size - 1)
                                }
                                _locationUpdates.value = currentList
                                
                                Log.d(TAG, "Location updated: $latitude, $longitude")
                            }
                        }
                        WorkInfo.State.FAILED -> {
                            val error = workInfo.outputData.getString(LocationTrackingWorker.KEY_ERROR)
                            _lastError.value = error ?: "Unknown error occurred"
                            Log.e(TAG, "Location tracking failed: $error")
                        }
                        else -> {
                            // Handle other states if needed
                        }
                    }
                }
            }
        
        _isTracking.value = true
    }
    
    fun stopLocationTracking(context: Context) {
        Log.d(TAG, "Stopping location tracking service...")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        _isTracking.value = false
    }
    
    fun clearLocationHistory() {
        _locationUpdates.value = emptyList()
        _lastError.value = null
    }
}