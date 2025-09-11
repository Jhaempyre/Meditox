package com.example.meditox.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.meditox.Routes
import com.example.meditox.models.viewModel.OtpViewModel
import com.example.meditox.utils.ApiResult
import com.example.meditox.utils.DataStoreManager
import com.example.meditox.utils.EncryptedTokenManager
import com.example.meditox.utils.PermissionUtils

@Composable
fun OtpScreen(modifier: Modifier, navController: NavController, viewModel: OtpViewModel) {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    val backgroundColor = Color(0xFFE8F5E9)
    val GreenDark = Color(0xFF005005)
    val phoneNumber = viewModel.phoneNumber.collectAsState().value
    val otpResult = viewModel.otpResult.collectAsState().value

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


            Text(
                text = "Enter the OTP",
                style = MaterialTheme.typography.displayMedium,
                color = GreenDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.take(6) }, // OTP limit
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "We've sent an OTP to your whatsapp. Only valid for 2 minutes",
                style = MaterialTheme.typography.labelSmall,
                color = GreenDark.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(22.dp))

            // Show loading indicator when verifying
            when (otpResult) {
                is ApiResult.Loading -> {
                    CircularProgressIndicator(color = GreenDark)
                }
                else -> {
                    Button(
                        onClick = {
                            if (otp.length == 6) {
                                viewModel.verifyOtp(otp)
                            } else {
                                Toast.makeText(context, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Verify", color = Color.White)
                    }
                }
            }

            // Handle OTP verification result
            LaunchedEffect(otpResult) {
                when (val result = otpResult) {
                    is ApiResult.Success -> {
                        // First, mark user as logged in
                        DataStoreManager.setIsLoggedIn(context, true)

                        val response = result.data
                        val body = response.body()

                        Log.d("API_RESPONSE", "Success: $response")
                        Log.d("API_RESPONSE", "Body: $body")
                        Log.d("API_RESPONSE", "Data field: ${body?.data}")

                        // Check if permissions are granted
                        // All permissions granted, proceed based on registration status

                        if (body?.data == null) {
                            // User not registered
                            DataStoreManager.setIsRegistered(context, false)
                            Toast.makeText(
                                context,
                                "OTP verified. Please register.",
                                Toast.LENGTH_SHORT
                            ).show()
                            val hasAllPermissions = PermissionUtils.allPermissionsGranted(context)
                            if (!hasAllPermissions) {
                                // Navigate to permission screen first
                                navController.navigate(Routes.PERMISSIONS) {
                                    popUpTo(Routes.OTP) { inclusive = true }
                                }
                            }
                            navController.navigate(Routes.REGISTER_USER) {
                                popUpTo(Routes.OTP) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // User is registered
                            Log.d("API_RESPONSE", " have landed here")
                            DataStoreManager.setIsRegistered(context, true)
                            Log.d("API_RESPONSE", " have also landed here")
                            val user = body.data
                            val isBusinessRegistered= user.isBusinessRegistered
                            Log.d("inOtp", " starting to save")
                            DataStoreManager.saveUserData(context, user)
                            Log.d("inOtp", " i guess saving succeded")
                            val hasAllPermissions = PermissionUtils.allPermissionsGranted(context)
                            Log.d("permissionresponse", "$hasAllPermissions")


                            val accessToken = response.headers()["Authorization"]?.removePrefix("Bearer ")?.trim()
                            val refreshToken = response.headers()["X-Refresh-Token"]

                            if(accessToken != null && refreshToken != null){
                                Log.d("TOKEN_DEBUG", "Access: $accessToken, Refresh: $refreshToken")
                                EncryptedTokenManager.saveAccessAndRefreshToken(context, accessToken, refreshToken)
                            }else{
                                Toast.makeText(context,"tokens not available",Toast.LENGTH_SHORT).show()
                            }
                            if(!isBusinessRegistered){
                                if (!hasAllPermissions) {
                                    Log.d(
                                        "API_RESPONSE",
                                        "checking permission... about to navigate"
                                    )
                                    navController.navigate(Routes.PERMISSIONS) {
                                        popUpTo(Routes.OTP) { inclusive = true }
                                    }
                                }
                                Toast.makeText(context, "Please Register your Shop Now", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.REGISTER_SHOP) {
                                    popUpTo(Routes.OTP) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            DataStoreManager.setIsBusinessRegistered(context,true);
                            Log.d("API_RESPONSE", "this will be printed") // Likely will show
                            if (!hasAllPermissions) {
                                Log.d("API_RESPONSE", "checking permission... about to navigate")
                                navController.navigate(Routes.PERMISSIONS) {
                                    popUpTo(Routes.OTP) { inclusive = true }
                                }
                                Log.d("API_RESPONSE", "this won't be printed") // Likely won't show
                                return@LaunchedEffect
                            }
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.OTP) { inclusive = true }
                                launchSingleTop = true
                            }
                        }


                        // Reset state after navigation
                        viewModel.resetOtpVerificationState()

                    }
                    is ApiResult.Error -> {
                        //SPECIFIC Error messgage dena hae aur fir sab screen mae jaa jaa kr dekhna hae aur dena hae jo bhi erro hae
                        Toast.makeText(
                            context,
                            result.message ?: "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetOtpVerificationState()
                    }

                    else -> {} // Loading or null - no action needed
                }
            }
        }
    }
}