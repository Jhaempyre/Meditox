package com.example.meditox


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meditox.models.viewModel.AuthViewModel
import com.example.meditox.models.viewModel.OtpViewModel
import com.example.meditox.screens.Dashboard
import com.example.meditox.screens.LoginScreen
import com.example.meditox.screens.OtpScreen
import com.example.meditox.screens.PermissionsScreen
import com.example.meditox.screens.ShopRegisterScreen
import com.example.meditox.screens.LocationTrackingScreen
import com.example.meditox.screens.SplashScreen
import com.example.meditox.screens.subscription.SubscriptionScreen
import com.example.meditox.services.ApiClient
import com.example.meditox.services.AuthApiService
import com.example.meditox.ui.screens.RegisterUserScreen
import com.example.meditox.ui.theme.MeditoxTheme
import com.jakewharton.threetenabp.AndroidThreeTen
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.SubscriptionDataHolder
import com.example.meditox.utils.SubscriptionSyncManager
import com.example.meditox.models.subscription.SubscriptionDetails
import com.example.meditox.models.subscription.SubscriptionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object Routes{
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val OTP = "otp"
    const val DASHBOARD = "dashboard"
    const val REGISTER_USER = "register_user"
    const val REGISTER_SHOP = "register_shop"
    const val PERMISSIONS="permissions"
    const val LOCATION_TRACKING = "location_tracking"
    const val SUBSCRIPTION = "subscription"
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current //not needed as such now

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }
        composable(Routes.LOGIN) {
            val loginViewModel: AuthViewModel = viewModel()
            LoginScreen(modifier = Modifier, navController, loginViewModel)
        }
        composable(Routes.OTP) {
            val otpViewModel: OtpViewModel = viewModel()
            OtpScreen(modifier = Modifier, navController, otpViewModel)
        }
        composable(Routes.DASHBOARD) {
            Dashboard(modifier = Modifier, navController)
        }
        composable(Routes.REGISTER_USER) {
            RegisterUserScreen(modifier = Modifier, navController)
        }
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(navController)
        }
        composable(Routes.REGISTER_SHOP){
            ShopRegisterScreen(modifier = Modifier,navController)
        }
        composable(Routes.LOCATION_TRACKING) {
            LocationTrackingScreen(navController = navController)
        }
        composable(Routes.SUBSCRIPTION) {
            SubscriptionScreen(navController = navController)
        }
    }
}
    class MainActivity : ComponentActivity(), PaymentResultListener {
        private lateinit var apiService: AuthApiService
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            AndroidThreeTen.init(this)
            apiService = ApiClient.createUserApiService(this)
            enableEdgeToEdge()
            setContent {
                MeditoxTheme {
                    AppNavigation()
                }
            }
        }

        override fun onPaymentSuccess(razorpayPaymentID: String?) {
            Log.d("MainActivity", "Payment successful: $razorpayPaymentID")
            
            // Save subscription details to DataStore using real-time data
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get the actual subscription data from the holder
                    if (SubscriptionDataHolder.hasValidData()) {
                        val subscriptionResponse = SubscriptionDataHolder.getSubscriptionResponse()!!
                        val planId = SubscriptionDataHolder.getPlanId()!!
                        val planName = SubscriptionDataHolder.getPlanName()!!
                        val planPrice = SubscriptionDataHolder.getPlanPrice()!!
                        val planDuration = SubscriptionDataHolder.getPlanDuration()!!
                        
                        Log.d("MainActivity", "Using real subscription data:")
                        Log.d("MainActivity", "- Subscription ID: ${subscriptionResponse.id}")
                        Log.d("MainActivity", "- Plan ID: $planId")
                        Log.d("MainActivity", "- Plan Name: $planName")
                        Log.d("MainActivity", "- Plan Price: $planPrice")
                        Log.d("MainActivity", "- Plan Duration: $planDuration")
                        
                        val currentTime = System.currentTimeMillis()
                        
                        // Calculate end date based on plan duration
                        val endDate = when (planDuration.lowercase()) {
                            "month" -> currentTime + (30L * 24 * 60 * 60 * 1000) // 30 days
                            "year" -> currentTime + (365L * 24 * 60 * 60 * 1000) // 365 days
                            "week" -> currentTime + (7L * 24 * 60 * 60 * 1000) // 7 days
                            else -> currentTime + (30L * 24 * 60 * 60 * 1000) // Default to 30 days
                        }
                        
                        val subscriptionDetails = SubscriptionDetails(
                            subscriptionId = subscriptionResponse.id,
                            planId = planId,
                            planName = planName,
                            amount = planPrice,
                            currency = "INR",
                            status = "active",
                            startDate = currentTime,
                            endDate = endDate,
                            razorpayPaymentId = razorpayPaymentID ?: "",
                            isActive = true,
                            autoRenew = true,
                            createdAt = subscriptionResponse.createdAt * 1000, // Convert to milliseconds
                            lastUpdated = currentTime
                        )
                        
                        // Save to DataStore
                        DataStoreManager.saveSubscriptionDetails(this@MainActivity, subscriptionDetails)
                        
                        Log.d("MainActivity", "Real subscription details saved to DataStore:")
                        Log.d("MainActivity", "- Subscription ID: ${subscriptionDetails.subscriptionId}")
                        Log.d("MainActivity", "- Plan: ${subscriptionDetails.planName}")
                        Log.d("MainActivity", "- Amount: ${subscriptionDetails.amount}")
                        Log.d("MainActivity", "- Start Date: ${subscriptionDetails.startDate}")
                        Log.d("MainActivity", "- End Date: ${subscriptionDetails.endDate}")
                        Log.d("MainActivity", "- Payment ID: ${subscriptionDetails.razorpayPaymentId}")
                        
                        // Sync subscription details to backend
                        Log.d("MainActivity", "Starting backend sync for subscription...")
                        val syncSuccess = SubscriptionSyncManager.syncSubscriptionToBackend(
                            this@MainActivity,
                            subscriptionDetails
                        )
                        
                        if (syncSuccess) {
                            Log.d("MainActivity", "✅ Subscription successfully synced to backend")
                        } else {
                            Log.e("MainActivity", "❌ Failed to sync subscription to backend")
                            // You might want to implement retry logic or queue for later sync
                        }
                        
                        // Clear the temporary data after successful save and sync
                        SubscriptionDataHolder.clearData()
                        Log.d("MainActivity", "Cleared temporary subscription data")
                        
                    } else {
                        Log.e("MainActivity", "No valid subscription data found in holder")
                        // Fallback: Create minimal subscription record with payment ID
                        val currentTime = System.currentTimeMillis()
                        val fallbackDetails = SubscriptionDetails(
                            subscriptionId = "payment_${razorpayPaymentID}",
                            planId = "unknown",
                            planName = "Premium Plan",
                            amount = "₹299",
                            currency = "INR",
                            status = "active",
                            startDate = currentTime,
                            endDate = currentTime + (30L * 24 * 60 * 60 * 1000),
                            razorpayPaymentId = razorpayPaymentID ?: "",
                            isActive = true,
                            autoRenew = true,
                            createdAt = currentTime,
                            lastUpdated = currentTime
                        )
                        DataStoreManager.saveSubscriptionDetails(this@MainActivity, fallbackDetails)
                        Log.d("MainActivity", "Saved fallback subscription details")
                        
                        // Try to sync fallback details to backend as well
                        Log.d("MainActivity", "Attempting backend sync for fallback subscription...")
                        val fallbackSyncSuccess = SubscriptionSyncManager.syncSubscriptionToBackend(
                            this@MainActivity,
                            fallbackDetails
                        )
                        
                        if (fallbackSyncSuccess) {
                            Log.d("MainActivity", "✅ Fallback subscription synced to backend")
                        } else {
                            Log.e("MainActivity", "❌ Failed to sync fallback subscription to backend")
                        }
                    }
                    
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error saving subscription details: ${e.message}", e)
                }
            }
            
            // Show success message
            Toast.makeText(
                this, 
                "🎉 Payment successful! Welcome to Premium!",
                Toast.LENGTH_LONG
            ).show()
            
            Log.d("MainActivity", "Payment ID: $razorpayPaymentID")
            
            // TODO: Additional success handling:
            // 1. ✅ Update user subscription status in local storage - DONE
            // 2. Sync with backend to confirm payment
            // 3. Navigate to dashboard or success screen  
            // 4. Send analytics event
        }

        override fun onPaymentError(code: Int, response: String?) {
            Log.e("MainActivity", "Payment failed: Code=$code, Response=$response")
            
            val errorMessage = when (code) {
                Checkout.NETWORK_ERROR -> "Network error. Please check your internet connection."
                Checkout.INVALID_OPTIONS -> "Payment configuration error. Please try again."
                Checkout.PAYMENT_CANCELED -> "Payment was cancelled."
                Checkout.TLS_ERROR -> "Security error. Please update your app."
                else -> response ?: "Payment failed. Please try again."
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            
            // TODO: You can add additional error handling here:
            // 1. Log error for analytics
            // 2. Show retry option
            // 3. Reset subscription UI state
            // 4. Offer alternative payment methods
            
            Log.e("MainActivity", "Payment error details - Code: $code, Response: $response")
        }
    }




