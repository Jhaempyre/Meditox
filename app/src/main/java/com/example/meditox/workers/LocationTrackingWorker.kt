package com.example.meditox.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.meditox.utils.LocationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationTrackingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "LocationTrackingWorker"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ACCURACY = "accuracy"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_ERROR = "error"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting location tracking work...")
            
            when (val locationResponse = LocationUtils.getCurrentLocation(applicationContext)) {
                is LocationUtils.LocationResponse.Success -> {
                    val location = locationResponse.location
                    val timestamp = System.currentTimeMillis()
                    
                    Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                    
                    // Create output data with location information
                    val outputData = workDataOf(
                        KEY_LATITUDE to location.latitude,
                        KEY_LONGITUDE to location.longitude,
                        KEY_ACCURACY to (location.accuracy ?: 0f),
                        KEY_TIMESTAMP to timestamp
                    )
                    
                    Result.success(outputData)
                }
                is LocationUtils.LocationResponse.Error -> {
                    Log.e(TAG, "Location error: ${locationResponse.message}")
                    val outputData = workDataOf(KEY_ERROR to locationResponse.message)
                    Result.failure(outputData)
                }
                is LocationUtils.LocationResponse.PermissionDenied -> {
                    Log.e(TAG, "Location permission denied")
                    val outputData = workDataOf(KEY_ERROR to "Location permission denied")
                    Result.failure(outputData)
                }
                is LocationUtils.LocationResponse.LocationDisabled -> {
                    Log.e(TAG, "Location services disabled")
                    val outputData = workDataOf(KEY_ERROR to "Location services disabled")
                    Result.failure(outputData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in location tracking work", e)
            val outputData = workDataOf(KEY_ERROR to "Exception: ${e.message}")
            Result.failure(outputData)
        }
    }
}