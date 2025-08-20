package com.example.meditox.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.SplashViewModel
import com.example.meditox.utils.PermissionUtils
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: SplashViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isRegistered by viewModel.isRegistered.collectAsState()

    // UI (with background and centered content)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE8F5E9) // Light green background (your theme)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MediTox",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF005005)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color(0xFF005005))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading your dose of care...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF005005).copy(alpha = 0.8f)
                )
            }
        }
    }

    // Logic
    LaunchedEffect(isLoggedIn, isRegistered) {
        if (isLoggedIn == null || isRegistered == null) return@LaunchedEffect

        delay(1500) // Optional: give a little time to show the splash screen

        when {
            isLoggedIn == true -> {
                if (PermissionUtils.allPermissionsGranted(context)) {
                    if (isRegistered == true) {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(Routes.REGISTER_USER) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    navController.navigate(Routes.PERMISSIONS) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            else -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}
