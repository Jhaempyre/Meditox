package com.example.meditox.screens

import android.Manifest
import android.os.Build
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

@Composable
fun PermissionsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SplashViewModel = viewModel()
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value

    // List of all required permissions
    val permissions = remember {
        mutableStateListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            // Permissions granted, navigate to Dashboard
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(Routes.PERMISSIONS) { inclusive = true }
            }
        } else {
            // Permissions denied, decide what to do based on login and registration status
            when {
                // If user is logged in but not registered, navigate to Register User screen
                isLoggedIn == true && isRegistered == false -> {
                    navController.navigate(Routes.REGISTER_USER) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }

                // If user is logged in and registered, navigate to Dashboard
                isLoggedIn == true && isRegistered == true -> {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }

                // If user is not logged in, handle the logic accordingly (optional case)
                isLoggedIn == false -> {
                    // Optionally, navigate to login or show an alert, etc.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text("We need your permissions", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("To provide you with full functionality, please grant these permissions.")
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
}
