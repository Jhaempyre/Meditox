package com.example.meditox.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.SplashViewModel
import com.example.meditox.utils.PermissionUtils

@Composable
fun PermissionsScreen(navController: NavController) {
    val viewModel: SplashViewModel = viewModel()
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value

    // Get permissions from centralized utils
    val permissions = remember { PermissionUtils.getRequiredPermissions() }

    // State to show denied dialog
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var deniedPermissions by remember { mutableStateOf(listOf<String>()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }

        if (allGranted) {
            // All permissions granted
            when {
                isLoggedIn == true && isRegistered == false -> {
                    navController.navigate(Routes.REGISTER_USER) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }

                isLoggedIn == true && isRegistered == true -> {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }

                isLoggedIn == false -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            }
        } else {
            // Not all permissions granted
            deniedPermissions = result.filterValues { !it }.keys.toList()
            showPermissionDeniedDialog = true
        }
    }

    // UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text("We need your permissions", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("To provide you with full functionality, please grant these permissions:")
            Spacer(modifier = Modifier.height(16.dp))

            // Show what permissions we're asking for
            Column {
                permissions.forEach { permission ->
                    Text(
                        "• ${getPermissionDisplayName(permission)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    launcher.launch(permissions.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
    }

    // Show dialog if permissions denied
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            confirmButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                    launcher.launch(permissions.toTypedArray()) // Retry
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                    // Optionally navigate to next screen or close app
                }) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Permissions Required")
            },
            text = {
                Column {
                    Text("The following permissions are needed to proceed:")
                    Spacer(modifier = Modifier.height(8.dp))
                    deniedPermissions.forEach {
                        Text("• ${getPermissionDisplayName(it)}")
                    }
                }
            }
        )
    }
}

// Helper function to convert permission names to user-friendly names
private fun getPermissionDisplayName(permission: String): String {
    return when (permission) {
        android.Manifest.permission.CAMERA -> "Camera"
        android.Manifest.permission.RECORD_AUDIO -> "Microphone"
        android.Manifest.permission.ACCESS_FINE_LOCATION -> "Location (Fine)"
        android.Manifest.permission.ACCESS_COARSE_LOCATION -> "Location (Coarse)"
        android.Manifest.permission.READ_EXTERNAL_STORAGE -> "Read Storage"
        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE -> "Manage Storage"
        android.Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
        android.Manifest.permission.CALL_PHONE -> "Phone Calls"
        else -> permission.substringAfterLast(".")
    }
}