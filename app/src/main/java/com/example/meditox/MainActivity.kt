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
            
            // Show success message
            Toast.makeText(
                this, 
                "🎉 Payment successful! Welcome to Premium!", 
                Toast.LENGTH_LONG
            ).show()
            
            // TODO: You can add additional success handling here:
            // 1. Update user subscription status in local storage
            // 2. Sync with backend to confirm payment
            // 3. Navigate to dashboard or success screen
            // 4. Send analytics event
            
            Log.d("MainActivity", "Payment ID: $razorpayPaymentID")
            
            // Example: Navigate to dashboard after successful payment
            // You might want to pass payment details or refresh user subscription status
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




