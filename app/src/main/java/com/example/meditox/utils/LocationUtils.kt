package com.example.meditox.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
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
     * Check if location services (GPS) are enabled on the device
     */
    fun isLocationServicesEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    
    /**
     * Check if GPS provider specifically is enabled
     */
    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    
    /**
     * Create intent to open location settings
     */
    fun createLocationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
    
    /**
     * Check if device has all location requirements (permissions + services)
     */
    fun hasAllLocationRequirements(context: Context): Boolean {
        return hasLocationPermissions(context) && isLocationServicesEnabled(context)
    }
    
    /**
     * Get current location using Android's native LocationManager API
     * This method prioritizes GPS for high accuracy business registration
     */
    suspend fun getCurrentLocation(context: Context): LocationResponse {
        if (!hasLocationPermissions(context)) {
            Log.w(TAG, "Location permissions not granted")
            return LocationResponse.PermissionDenied
        }
        
        if (!isLocationServicesEnabled(context)) {
            Log.w(TAG, "Location services are disabled")
            return LocationResponse.LocationDisabled
        }
        
        return try {
            Log.d(TAG, "Requesting high-accuracy GPS location using native Android API")
            getNativeGpsLocation(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting native location", e)
            LocationResponse.Error("Failed to get location: ${e.message}")
        }
    }
    
    /**
     * Get high-accuracy location using native Android LocationManager
     * Prioritizes GPS provider for maximum accuracy
     */
    private suspend fun getNativeGpsLocation(context: Context): LocationResponse = suspendCancellableCoroutine { continuation ->
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (!hasLocationPermissions(context)) {
            continuation.resume(LocationResponse.PermissionDenied)
            return@suspendCancellableCoroutine
        }
        
        // Check if GPS provider is available
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w(TAG, "GPS provider is not enabled")
            continuation.resume(LocationResponse.LocationDisabled)
            return@suspendCancellableCoroutine
        }
        
        var bestLocation: Location? = null
        var locationReceived = false
        val handler = Handler(Looper.getMainLooper())
        
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d(TAG, "Native GPS location received: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")
                
                // Only accept locations with good accuracy (< 20 meters for business registration)
                if (location.accuracy <= 20f) {
                    if (!locationReceived) {
                        locationReceived = true
                        locationManager.removeUpdates(this)
                        
                        val result = LocationResponse.Success(
                            LocationResult(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy
                            )
                        )
                        
                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                } else {
                    Log.d(TAG, "Location accuracy too low: ${location.accuracy}m, waiting for better accuracy...")
                    bestLocation = if (bestLocation == null || location.accuracy < bestLocation!!.accuracy) {
                        location
                    } else {
                        bestLocation
                    }
                }
            }
            
            override fun onProviderEnabled(provider: String) {
                Log.d(TAG, "Location provider enabled: $provider")
            }
            
            override fun onProviderDisabled(provider: String) {
                Log.w(TAG, "Location provider disabled: $provider")
                if (provider == LocationManager.GPS_PROVIDER && !locationReceived) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(LocationResponse.LocationDisabled)
                    }
                }
            }
            
            @Deprecated("Deprecated in API level 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d(TAG, "Location provider status changed: $provider, status: $status")
            }
        }
        
        // Set up timeout (45 seconds for high accuracy GPS)
        val timeoutRunnable = Runnable {
            Log.w(TAG, "GPS location request timed out")
            locationManager.removeUpdates(locationListener)
            
            if (!locationReceived && continuation.isActive) {
                if (bestLocation != null) {
                    Log.d(TAG, "Using best available location: accuracy ${bestLocation!!.accuracy}m")
                    continuation.resume(
                        LocationResponse.Success(
                            LocationResult(
                                latitude = bestLocation!!.latitude,
                                longitude = bestLocation!!.longitude,
                                accuracy = bestLocation!!.accuracy
                            )
                        )
                    )
                } else {
                    continuation.resume(LocationResponse.Error("GPS timeout: Could not get accurate location. Please ensure you're outdoors with clear sky view."))
                }
            }
        }
        
        try {
            // Request location updates from GPS provider
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // 1 second interval
                0f,    // No minimum distance
                locationListener,
                Looper.getMainLooper()
            )
            
            // Start timeout timer
            handler.postDelayed(timeoutRunnable, 45000L) // 45 seconds
            
            // Set up cancellation
            continuation.invokeOnCancellation {
                handler.removeCallbacks(timeoutRunnable)
                locationManager.removeUpdates(locationListener)
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting GPS location", e)
            continuation.resume(LocationResponse.PermissionDenied)
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting GPS location", e)
            continuation.resume(LocationResponse.Error("Failed to request GPS location: ${e.message}"))
        }
    }
    
    /**
     * Get last known location from native LocationManager
     */
    private fun getLastKnownNativeLocation(context: Context): Location? {
        if (!hasLocationPermissions(context)) {
            return null
        }
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        return try {
            var bestLocation: Location? = null
            
            // Try GPS provider first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLocation != null) {
                    bestLocation = gpsLocation
                    Log.d(TAG, "Last known GPS location: ${gpsLocation.latitude}, ${gpsLocation.longitude}, accuracy: ${gpsLocation.accuracy}m")
                }
            }
            
            // Try Network provider as fallback
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (networkLocation != null) {
                    bestLocation = networkLocation
                    Log.d(TAG, "Last known Network location: ${networkLocation.latitude}, ${networkLocation.longitude}, accuracy: ${networkLocation.accuracy}m")
                }
            }
            
            bestLocation
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting last known location", e)
            null
        }
    }
    
    /**
     * Get current location with high accuracy using native Android API
     * This method is optimized for business registration requiring < 10m accuracy
     */
    suspend fun getCurrentLocationHighAccuracy(context: Context): LocationResponse {
        if (!hasLocationPermissions(context)) {
            return LocationResponse.PermissionDenied
        }
        
        if (!isLocationServicesEnabled(context)) {
            Log.w(TAG, "Location services are disabled for high accuracy request")
            return LocationResponse.LocationDisabled
        }
        
        return try {
            Log.d(TAG, "Requesting ultra-high accuracy GPS location (< 10m) using native Android API")
            getNativeUltraHighAccuracyLocation(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting ultra-high accuracy location", e)
            LocationResponse.Error("Failed to get high accuracy location: ${e.message}")
        }
    }
    
    /**
     * Get ultra-high accuracy location (< 10 meters) using native GPS
     */
    private suspend fun getNativeUltraHighAccuracyLocation(context: Context): LocationResponse = suspendCancellableCoroutine { continuation ->
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w(TAG, "GPS provider is not enabled for ultra-high accuracy")
            continuation.resume(LocationResponse.LocationDisabled)
            return@suspendCancellableCoroutine
        }
        
        var bestLocation: Location? = null
        var locationReceived = false
        val handler = Handler(Looper.getMainLooper())
        
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d(TAG, "Ultra-high accuracy GPS location: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")
                
                // Only accept locations with ultra-high accuracy (< 10 meters for critical business registration)
                if (location.accuracy <= 10f) {
                    if (!locationReceived) {
                        locationReceived = true
                        locationManager.removeUpdates(this)
                        
                        val result = LocationResponse.Success(
                            LocationResult(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy
                            )
                        )
                        
                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                } else {
                    Log.d(TAG, "Location accuracy not sufficient: ${location.accuracy}m, need < 10m...")
                    bestLocation = if (bestLocation == null || location.accuracy < bestLocation!!.accuracy) {
                        location
                    } else {
                        bestLocation
                    }
                }
            }
            
            override fun onProviderEnabled(provider: String) {
                Log.d(TAG, "Ultra-high accuracy provider enabled: $provider")
            }
            
            override fun onProviderDisabled(provider: String) {
                Log.w(TAG, "Ultra-high accuracy provider disabled: $provider")
                if (provider == LocationManager.GPS_PROVIDER && !locationReceived) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(LocationResponse.LocationDisabled)
                    }
                }
            }
            
            @Deprecated("Deprecated in API level 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.d(TAG, "Ultra-high accuracy provider status changed: $provider, status: $status")
            }
        }
        
        // Extended timeout for ultra-high accuracy (60 seconds)
        val timeoutRunnable = Runnable {
            Log.w(TAG, "Ultra-high accuracy GPS request timed out")
            locationManager.removeUpdates(locationListener)
            
            if (!locationReceived && continuation.isActive) {
                if (bestLocation != null) {
                    Log.d(TAG, "Using best available ultra-high accuracy location: ${bestLocation!!.accuracy}m")
                    continuation.resume(
                        LocationResponse.Success(
                            LocationResult(
                                latitude = bestLocation!!.latitude,
                                longitude = bestLocation!!.longitude,
                                accuracy = bestLocation!!.accuracy
                            )
                        )
                    )
                } else {
                    continuation.resume(LocationResponse.Error("Ultra-high accuracy GPS timeout: Could not achieve < 10m accuracy. Please ensure you're outdoors with clear sky view and wait for GPS to stabilize."))
                }
            }
        }
        
        try {
            // Request very frequent updates for ultra-high accuracy
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L, // 0.5 second interval for rapid updates
                0f,   // No minimum distance
                locationListener,
                Looper.getMainLooper()
            )
            
            // Extended timeout for ultra-high accuracy
            handler.postDelayed(timeoutRunnable, 60000L) // 60 seconds
            
            // Set up cancellation
            continuation.invokeOnCancellation {
                handler.removeCallbacks(timeoutRunnable)
                locationManager.removeUpdates(locationListener)
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting ultra-high accuracy GPS", e)
            continuation.resume(LocationResponse.PermissionDenied)
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting ultra-high accuracy GPS", e)
            continuation.resume(LocationResponse.Error("Failed to request ultra-high accuracy GPS: ${e.message}"))
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