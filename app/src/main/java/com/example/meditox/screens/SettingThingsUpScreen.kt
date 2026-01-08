package com.example.meditox.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.OtpViewModel
import com.example.meditox.models.subscription.toSubscriptionDetails
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.DataStoreDebugger
import com.example.meditox.utils.EncryptedTokenManager
import com.example.meditox.utils.PermissionUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SettingThingsUpScreen(
    navController: NavController,
    viewModel: OtpViewModel,
    userId: String,
    isRegistered: Boolean,
    isBusinessRegistered: Boolean,
    accessToken: String,
    refreshToken: String
) {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFE8F5E9)
    val GreenDark = Color(0xFF005005)

    val shopDetailsResult = viewModel.shopDetailsResult.collectAsState().value
    val subscriptionDetailResult = viewModel.subscriptionDetailResult.collectAsState().value

    var currentStep by remember { mutableStateOf("Initializing...") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Save tokens first
    LaunchedEffect(PermissionUtils.allPermissionsGranted(context),
        isRegistered,
        isBusinessRegistered) {
        //if (!PermissionUtils.allPermissionsGranted(context)) return@LaunchedEffect
        try {
            Log.d("SettingThingsUp", "Saving tokens...")
            currentStep = "Saving authentication tokens..."
            EncryptedTokenManager.saveAccessAndRefreshToken(context, accessToken, refreshToken)
            Log.d("SettingThingsUp", "Tokens saved successfully")

            // Check permissions
            val hasAllPermissions = PermissionUtils.allPermissionsGranted(context)
            Log.d("SettingThingsUp", "Permissions check: $hasAllPermissions")

            if (!hasAllPermissions) {
                Log.d("SettingThingsUp", "Permissions not granted, navigating to permissions screen")
                navController.navigate(Routes.PERMISSIONS)
                return@LaunchedEffect
            }

            // Handle user not registered
            if (!isRegistered) {
                Log.d("SettingThingsUp", "User not registered, navigating to register screen")
                currentStep = "Redirecting to registration..."
                Toast.makeText(context, "Please complete your registration", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.REGISTER_USER) {
                    popUpTo(Routes.SETTING_THINGS_UP) { inclusive = true }
                }
                return@LaunchedEffect
            }

            // Handle business not registered
            if (!isBusinessRegistered) {
                Log.d("SettingThingsUp", "Business not registered, navigating to shop registration")
                currentStep = "Redirecting to shop registration..."
                Toast.makeText(context, "Please register your shop", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.REGISTER_SHOP) {
                    popUpTo(Routes.SETTING_THINGS_UP) { inclusive = true }
                }
                return@LaunchedEffect
            }

            // User and business are registered, fetch shop details
            Log.d("SettingThingsUp", "Fetching shop details for user: $userId")
            currentStep = "Loading shop details..."
            viewModel.fetchShopDetails(userId)

        } catch (e: Exception) {
            Log.e("SettingThingsUp", "Error during initialization: ${e.message}", e)
            hasError = true
            errorMessage = "Failed to initialize: ${e.message}"
        }
    }

    // Handle shop details result
    LaunchedEffect(shopDetailsResult) {
        when (val result = shopDetailsResult) {
            is ApiResult.Success -> {
                val response = result.data
                val body = response.body()

                Log.d("SettingThingsUp", "Shop details received: $body")

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    try {
                        // Save shop details to DataStore
                        currentStep = "Saving shop details..."
                        DataStoreManager.saveShopDetails(context, body.data)
                        Log.d("SettingThingsUp", "Shop details saved successfully")

                        // IMPORTANT: Set business registered AFTER saving shop details
                        DataStoreManager.setIsBusinessRegistered(context, true)
                        Log.d("SettingThingsUp", "Business registered flag set to true")

                        // Wait a bit to ensure DataStore write completes
                        delay(100)

                        // Debug: Check what was saved
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-AfterShopSave")

                        // Fetch subscription details
                        Log.d("SettingThingsUp", "Fetching subscription details for user: $userId")
                        currentStep = "Loading subscription details..."
                        viewModel.getSubscriptionDetails(userId)

                    } catch (e: Exception) {
                        Log.e("SettingThingsUp", "Error saving shop details: ${e.message}", e)
                        hasError = true
                        errorMessage = "Failed to save shop details: ${e.message}"
                    }
                } else {
                    Log.e("SettingThingsUp", "Failed to fetch shop details: ${body?.message}")
                    hasError = true
                    errorMessage = "Failed to load shop details. Please try again."
                }

                viewModel.resetShopDetailsState()
            }

            is ApiResult.Error -> {
                Log.e("SettingThingsUp", "Error fetching shop details: ${result.message}")
                hasError = true
                errorMessage = result.message ?: "Failed to load shop details"
                viewModel.resetShopDetailsState()
            }

            else -> {} // Loading or null
        }
    }

    // Handle subscription details result
    LaunchedEffect(subscriptionDetailResult) {
        when (val result = subscriptionDetailResult) {
            is ApiResult.Success -> {
                val response = result.data
                val body = response.body()

                Log.d("SettingThingsUp", "Subscription details received: $body")

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    try {
                        currentStep = "Saving subscription details..."

                        val subscriptionApiData = response.body()?.data

                        Log.d("SettingThingsUp", "Subscription API data: ${subscriptionApiData.toString()}")


                        if (subscriptionApiData != null) {
                            val subscriptionData = subscriptionApiData.toSubscriptionDetails()
                            val isActive = subscriptionData.isActive
                            if(isActive){
                            DataStoreManager.saveSubscriptionDetails(context, subscriptionData)
                            DataStoreManager.setBackendSyncStatus(context,true);
                            Log.d("SettingThingsUp", "Subscription details saved successfully")
                            }
                            else{
                                DataStoreManager.setBackendSyncStatus(context,false)
                            }
                            Log.d("SettingThingsUp", "Subscription details saved successfully")
                        } else {
                            Log.w("SettingThingsUp", "No subscription data available")
                        }

                        // All done, navigate to dashboard
                        currentStep = "Completing setup..."
                        Log.d("SettingThingsUp", "Setup complete, navigating to dashboard")

                        // Wait a bit to ensure all DataStore writes complete
                        delay(200)

                        // Verify data is saved before navigating
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-BeforeNavigate")

                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }

                    } catch (e: Exception) {
                        Log.e("SettingThingsUp", "Error saving subscription details: ${e.message}", e)
                        // Navigate to dashboard anyway as subscription is optional
                        Log.d("SettingThingsUp", "Navigating to dashboard despite subscription error")

                        // Wait a bit to ensure all DataStore writes complete
                        delay(200)

                        // Verify data is saved
                        DataStoreDebugger.logAllStates(context, "SettingThingsUp-ErrorCase")

                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    Log.w("SettingThingsUp", "Subscription not found or failed to fetch")
                    // Navigate to dashboard anyway as subscription might not exist yet
                    Log.d("SettingThingsUp", "Navigating to dashboard without subscription")

                    // Wait a bit to ensure all DataStore writes complete
                    delay(200)

                    // Verify data is saved
                    DataStoreDebugger.logAllStates(context, "SettingThingsUp-NoSubscription")

                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                viewModel.resetgetSubscriptionDetails()
            }

            is ApiResult.Error -> {
                Log.e("SettingThingsUp", "Error fetching subscription: ${result.message}")
                // Navigate to dashboard anyway as subscription might not exist yet
                Log.d("SettingThingsUp", "Navigating to dashboard despite subscription error")

                // Wait a bit to ensure all DataStore writes complete
                delay(200)

                // Verify data is saved
                DataStoreDebugger.logAllStates(context, "SettingThingsUp-SubscriptionError")

                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()

                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }

                viewModel.resetgetSubscriptionDetails()
            }

            else -> {} // Loading or null
        }
    }

    // UI
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasError) {
                // Error state
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenDark.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Back to Login")
                }

            } else {
                // Loading state
                CircularProgressIndicator(
                    color = GreenDark,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Setting things up...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GreenDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStep,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenDark.copy(alpha = 0.7f)
                )
            }
        }
    }
}