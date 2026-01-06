package com.example.meditox.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.SplashViewModel
import com.example.meditox.utils.PermissionUtils

@Composable
fun PermissionsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity) ?: return // Ensure we have ComponentActivity
    val viewModel: SplashViewModel = viewModel()
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value
    val isBusinessRegistered = viewModel.isBusinessRegistered.collectAsState().value

    // Get regular permissions
    val regularPermissions = remember { PermissionUtils.getRegularPermissions() }

    // States
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var permanentlyDeniedPermissions by remember { mutableStateOf(emptyList<String>()) }
    var deniedPermissions by remember { mutableStateOf(emptyList<String>()) }

    // Navigation function
    fun navigateToNext() {
        when {

            isLoggedIn == true && isRegistered == false -> {
                navController.navigate(Routes.REGISTER_USER) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            }

            isLoggedIn==true && isRegistered == true && isBusinessRegistered == false -> {
                navController.navigate(Routes.REGISTER_SHOP) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            }

            isLoggedIn==true && isBusinessRegistered == true && isRegistered == true -> {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            }
            else -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            }
        }
    }

    // Regular permissions launcher
    val regularPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filterValues { !it }.keys.toList()
        deniedPermissions = denied

        if (denied.isEmpty()) {
            // All regular permissions granted
            if (PermissionUtils.needsStorageManagementPermission()) {
                showStoragePermissionDialog = true
            } else {
                navController.popBackStack()
            }
        } else {
            // Check for permanently denied permissions
            permanentlyDeniedPermissions = denied.filter {
                !activity.shouldShowRequestPermissionRationale(it)
            }
            showPermissionDeniedDialog = true
        }
    }

    // Storage permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (PermissionUtils.isStorageManagementGranted()) {
            navController.popBackStack()
        } else {
            showStoragePermissionDialog = true
        }
    }

    // Settings launcher for permanently denied permissions
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (PermissionUtils.allPermissionsGranted(context)) {
            navController.popBackStack()
        } else {
            // Re-check permissions
            deniedPermissions = PermissionUtils.getDeniedRegularPermissions(context)
            if (deniedPermissions.isNotEmpty()) {
                permanentlyDeniedPermissions = deniedPermissions.filter {
                    !activity.shouldShowRequestPermissionRationale(it)
                }
                showPermissionDeniedDialog = true
            } else if (PermissionUtils.needsStorageManagementPermission()) {
                showStoragePermissionDialog = true
            }
        }
    }

    // Check permissions on screen load
    LaunchedEffect(Unit) {
        if (PermissionUtils.allPermissionsGranted(context)) {
            navController.popBackStack()
        } else {
            regularPermissionsLauncher.launch(regularPermissions.toTypedArray())
        }
    }

    // UI
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("We need your permissions", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("To provide full functionality, please grant the following permissions:")
            Spacer(modifier = Modifier.height(16.dp))

            // Show permissions list
            Column {
                regularPermissions.forEach { permission ->
                    Text(
                        "• ${PermissionUtils.getPermissionDisplayName(permission)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (PermissionUtils.needsStorageManagementPermission()) {
                    Text(
                        "• Complete Storage Access",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    regularPermissionsLauncher.launch(regularPermissions.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
    }

    // Regular permissions denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            confirmButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                    if (permanentlyDeniedPermissions.isNotEmpty()) {
                        // Redirect to app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        settingsLauncher.launch(intent)
                    } else {
                        // Retry regular permissions
                        regularPermissionsLauncher.launch(regularPermissions.toTypedArray())
                    }
                }) {
                    Text(if (permanentlyDeniedPermissions.isNotEmpty()) "Open Settings" else "Try Again")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                    // Allow skipping for non-critical permissions
                    if (!PermissionUtils.needsStorageManagementPermission()) {
                        navController.popBackStack()
                    }
                }) {
                    Text("Skip")
                }
            },
            title = { Text("Permissions Required") },
            text = {
                Column {
                    Text("The following permissions are needed:")
                    Spacer(modifier = Modifier.height(8.dp))
                    deniedPermissions.forEach { permission ->
                        Text("• ${PermissionUtils.getPermissionDisplayName(permission)}")
                    }
                    if (permanentlyDeniedPermissions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Some permissions are permanently denied. Please enable them in app settings.")
                    }
                }
            }
        )
    }

    // Storage permission dialog
    if (showStoragePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showStoragePermissionDialog = false },
            confirmButton = {
                Button(onClick = {
                    showStoragePermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        storagePermissionLauncher.launch(intent)
                    }
                }) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showStoragePermissionDialog = false
                    navController.popBackStack() // Allow skipping storage permission
                }) {
                    Text("Skip")
                }
            },
            title = { Text("Storage Access Needed") },
            text = {
                Column {
                    Text("The app needs complete storage access for full functionality.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You'll be redirected to system settings to grant this permission.")
                }
            }
        )
    }
}