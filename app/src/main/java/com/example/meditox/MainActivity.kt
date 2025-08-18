package com.example.meditox


import android.os.Bundle
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
import com.example.meditox.screens.RegisterUser
import com.example.meditox.screens.SplashScreen
import com.example.meditox.ui.theme.MeditoxTheme


object Routes{
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val OTP = "otp"
    const val DASHBOARD = "dashboard"
    const val REGISTER_USER = "register_user"
    const val REGISTER_SHOP = "register_shop"

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
            RegisterUser(modifier = Modifier, navController)
        }
    }
}
    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
                MeditoxTheme {
                    AppNavigation()
                }
            }
        }
    }




