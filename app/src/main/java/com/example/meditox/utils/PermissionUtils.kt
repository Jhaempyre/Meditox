package com.example.meditox.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object PermissionUtils {

    // Regular runtime permissions
    fun getRegularPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions
    }

    // Check if all regular permissions are granted
    fun allRegularPermissionsGranted(context: Context): Boolean {
        return getRegularPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Check if storage management permission is granted (Android 11+)
    fun isStorageManagementGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // For Android 10 and below, check READ_EXTERNAL_STORAGE
            true // Already checked in regular permissions
        }
    }

    // Check if all permissions (including storage management) are granted
    fun allPermissionsGranted(context: Context): Boolean {
        return allRegularPermissionsGranted(context) && isStorageManagementGranted()
    }

    // Get denied regular permissions
    fun getDeniedRegularPermissions(context: Context): List<String> {
        return getRegularPermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    // Check if we need to request storage management permission
    fun needsStorageManagementPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()
    }

    // Helper function to check specific permission
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    // Get user-friendly permission names
    fun getPermissionDisplayName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "Camera"
            Manifest.permission.RECORD_AUDIO -> "Microphone"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Location (Fine)"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "Location (Coarse)"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Read Storage"
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "Manage All Files"
            Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
            Manifest.permission.CALL_PHONE -> "Phone Calls"
            else -> permission.substringAfterLast(".")
        }
    }
}