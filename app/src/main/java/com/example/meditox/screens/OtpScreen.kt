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
                onValueChange = { otp = it.take(6) },
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
                        // Mark user as logged in
                        DataStoreManager.setIsLoggedIn(context, true)

                        val response = result.data
                        val body = response.body()

                        Log.d("OtpScreen", "OTP verification successful")
                        Log.d("OtpScreen", "Response body: $body")

                        // Extract tokens from headers
                        val accessToken = response.headers()["Authorization"]?.removePrefix("Bearer ")?.trim()
                        val refreshToken = response.headers()["X-Refresh-Token"]

                        if (accessToken == null || refreshToken == null) {
                            Toast.makeText(context, "Authentication tokens not received", Toast.LENGTH_SHORT).show()
                            viewModel.resetOtpVerificationState()
                            return@LaunchedEffect
                        }

                        Log.d("OtpScreen", "Tokens extracted successfully")

                        if (body?.data == null) {
                            // User not registered
                            Log.d("OtpScreen", "User not registered")
                            DataStoreManager.setIsRegistered(context, false)
                            Toast.makeText(context, "OTP verified. Please register.", Toast.LENGTH_SHORT).show()

                            navController.navigate(Routes.REGISTER_USER) {
                                popUpTo(Routes.OTP) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // User is registered
                            val user = body.data
                            val isBusinessRegistered = user.businessRegistered

                            Log.d("OtpScreen", "User registered. Business registered: $isBusinessRegistered")

                            // Save user data
                            DataStoreManager.setIsRegistered(context, true)
                            DataStoreManager.saveUserData(context, user)
                            Log.d("OtpScreen", "User data saved")

                            // Navigate to SettingThingsUpScreen with necessary data
                            navController.navigate(
                                Routes.settingThingsUp(
                                    userId = user.id.toString(),
                                    isRegistered = true,
                                    isBusinessRegistered = isBusinessRegistered,
                                    accessToken = accessToken,
                                    refreshToken = refreshToken
                                )
                            ) {
                                popUpTo(Routes.OTP) { inclusive = true }
                                launchSingleTop = true
                            }
                        }

                        // Reset state after navigation
                        viewModel.resetOtpVerificationState()
                    }

                    is ApiResult.Error -> {
                        Log.e("OtpScreen", "OTP verification failed: ${result.message}")
                        Toast.makeText(
                            context,
                            result.message ?: "Verification failed",
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