package com.example.meditox.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
    val viewModel: SplashViewModel = viewModel()
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value

    val regularPermissions = remember { PermissionUtils.getRegularPermissions() }

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var deniedPermissions by remember { mutableStateOf(emptyList<String>()) }

    val regularPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val denied = result.filterValues { !it }.keys.toList()
        deniedPermissions = denied

        if (denied.isEmpty()) {
            // All runtime permissions granted
            if (PermissionUtils.needsStorageManagementPermission()) {
                showStoragePermissionDialog = true
            } else {
                navigateToNext(navController, isLoggedIn, isRegistered)
            }
        } else {
            showPermissionDeniedDialog = true
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (PermissionUtils.isStorageManagementGranted()) {
            navigateToNext(navController, isLoggedIn, isRegistered)
        } else {
            showStoragePermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (PermissionUtils.allPermissionsGranted(context)) {
            navigateToNext(navController, isLoggedIn, isRegistered)
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
            Text("To provide full functionality, please grant the following permissions.")
            Spacer(modifier = Modifier.height(16.dp))

            regularPermissions.forEach {
                Text("• ${PermissionUtils.getPermissionDisplayName(it)}")
            }
            if (PermissionUtils.needsStorageManagementPermission()) {
                Text("• Complete Storage Access")
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

    // Regular Permission Denied Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            confirmButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                    regularPermissionsLauncher.launch(regularPermissions.toTypedArray())
                }) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showPermissionDeniedDialog = false
                }) {
                    Text("Cancel")
                }
            },
            title = { Text("Permissions Required") },
            text = {
                Column {
                    Text("The following permissions were denied:")
                    Spacer(modifier = Modifier.height(8.dp))
                    deniedPermissions.forEach {
                        Text("• ${PermissionUtils.getPermissionDisplayName(it)}")
                    }
                }
            }
        )
    }

    // Storage Permission Dialog
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
                    // You can choose to skip storage or re-prompt
                }) {
                    Text("Skip")
                }
            },
            title = { Text("Storage Access Needed") },
            text = {
                Text("Please grant 'Manage All Files' access from system settings.")
            }
        )
    }
}

// Navigation Logic
private fun navigateToNext(navController: NavController, isLoggedIn: Boolean?, isRegistered: Boolean?) {
    when {
        isLoggedIn == true && isRegistered == true -> {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.PERMISSIONS) { inclusive = true }
            }
        }
        isLoggedIn == true && isRegistered == false -> {
            navController.navigate(Routes.REGISTER_USER) {
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
