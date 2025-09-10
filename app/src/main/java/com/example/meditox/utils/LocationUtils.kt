package com.example.meditox.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Utility class for handling location operations
 * Provides methods to get current location with proper error handling
 */
object LocationUtils {
    
    private const val TAG = "LocationUtils"
    
    /**
     * Data class to hold location result
     */
    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float? = null,
        val address: String? = null
    )
    
    /**
     * Sealed class for location operation results
     */
    sealed class LocationResponse {
        data class Success(val location: LocationResult) : LocationResponse()
        data class Error(val message: String) : LocationResponse()
        object PermissionDenied : LocationResponse()
        object LocationDisabled : LocationResponse()
    }
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if fine location permission is granted
     */
    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get current location using FusedLocationProviderClient
     * This method uses the most recent location first, then requests a fresh location if needed
     */
    suspend fun getCurrentLocation(context: Context): LocationResponse {
        if (!hasLocationPermissions(context)) {
            Log.w(TAG, "Location permissions not granted")
            return LocationResponse.PermissionDenied
        }
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        return try {
            // First try to get the last known location (faster)
            val lastLocation = getLastKnownLocation(context, fusedLocationClient)
            if (lastLocation != null) {
                Log.d(TAG, "Using last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                return LocationResponse.Success(
                    LocationResult(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        accuracy = lastLocation.accuracy
                    )
                )
            }
            
            // If no last known location, request a fresh one
            Log.d(TAG, "No last known location, requesting fresh location")
            getFreshLocation(context, fusedLocationClient)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            LocationResponse.Error("Failed to get location: ${e.message}")
        }
    }
    
    /**
     * Get the last known location
     */
    private suspend fun getLastKnownLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { continuation ->
        
        if (!hasLocationPermissions(context)) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get last known location", exception)
                    continuation.resume(null)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting last location", e)
            continuation.resume(null)
        }
    }
    
    /**
     * Request a fresh location update
     */
    private suspend fun getFreshLocation(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient
    ): LocationResponse = suspendCancellableCoroutine { continuation ->
        
        if (!hasLocationPermissions(context)) {
            continuation.resume(LocationResponse.PermissionDenied)
            return@suspendCancellableCoroutine
        }
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateDistanceMeters(0f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                super.onLocationResult(locationResult)
                
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d(TAG, "Fresh location received: ${location.latitude}, ${location.longitude}")
                    
                    val result = LocationResponse.Success(
                        LocationResult(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                    )
                    
                    // Stop location updates
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(result)
                } else {
                    Log.w(TAG, "Received null location in callback")
                }
            }
            
            override fun onLocationAvailability(availability: LocationAvailability) {
                super.onLocationAvailability(availability)
                if (!availability.isLocationAvailable) {
                    Log.w(TAG, "Location not available")
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(LocationResponse.LocationDisabled)
                }
            }
        }
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            
            // Set up cancellation
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting location updates", e)
            continuation.resume(LocationResponse.PermissionDenied)
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting location updates", e)
            continuation.resume(LocationResponse.Error("Failed to request location: ${e.message}"))
        }
    }
    
    /**
     * Get current location with high accuracy using getCurrentLocation API
     * This is an alternative method that uses the newer getCurrentLocation API
     */
    suspend fun getCurrentLocationHighAccuracy(context: Context): LocationResponse {
        if (!hasLocationPermissions(context)) {
            return LocationResponse.PermissionDenied
        }
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()
        
        return suspendCancellableCoroutine { continuation ->
            
            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
            
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "High accuracy location: ${location.latitude}, ${location.longitude}")
                        continuation.resume(
                            LocationResponse.Success(
                                LocationResult(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    accuracy = location.accuracy
                                )
                            )
                        )
                    } else {
                        Log.w(TAG, "High accuracy location returned null")
                        continuation.resume(LocationResponse.Error("Unable to get current location"))
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get high accuracy location", exception)
                    continuation.resume(LocationResponse.Error("Location request failed: ${exception.message}"))
                }
                
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception getting high accuracy location", e)
                continuation.resume(LocationResponse.PermissionDenied)
            } catch (e: Exception) {
                Log.e(TAG, "Exception getting high accuracy location", e)
                continuation.resume(LocationResponse.Error("Failed to get location: ${e.message}"))
            }
        }
    }
    
    /**
     * Format location for display
     */
    fun formatLocation(location: LocationResult): String {
        return "Lat: ${"%.6f".format(location.latitude)}, Lng: ${"%.6f".format(location.longitude)}" +
                if (location.accuracy != null) " (±${location.accuracy.toInt()}m)" else ""
    }
    
    /**
     * Convert location to string format for API
     */
    fun locationToString(latitude: Double, longitude: Double): Pair<String, String> {
        return Pair(latitude.toString(), longitude.toString())
    }
}