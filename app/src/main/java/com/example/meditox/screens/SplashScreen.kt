package com.example.meditox.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.SplashViewModel
import com.example.meditox.utils.PermissionUtils
import kotlinx.coroutines.delay
@Composable
fun SplashScreen(navController: NavController) {
    val viewModel: SplashViewModel = viewModel()
    val context = LocalContext.current
    val isLoggedIn = viewModel.isLoggedIn.collectAsState().value
    val isRegistered = viewModel.isRegistered.collectAsState().value

    LaunchedEffect(isLoggedIn, isRegistered) {
        if (isLoggedIn == null || isRegistered == null) return@LaunchedEffect

        delay(1000) // Optional delay

        when {
            // ❌ User not logged in → just go to login screen (no permission check here)
            !isLoggedIn -> {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }

            // ✅ User logged in but not registered → go to permission screen
            isLoggedIn && !isRegistered -> {
                navController.navigate(Routes.PERMISSIONS) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }

            // ✅✅ User logged in and registered → also go to permission screen
            isLoggedIn && isRegistered -> {
                navController.navigate(Routes.PERMISSIONS) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

    // Simple Splash UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("yaha lagega pyaar ka tadkaa😂")
        CircularProgressIndicator()
    }
}
