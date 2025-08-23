package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.auth.AuthRequest
import com.example.meditox.models.auth.AuthResponse
import com.example.meditox.models.otpVerication.VerifyOtpRequest
import com.example.meditox.models.otpVerication.VerifyOtpResponse
import com.example.meditox.models.userRegistration.UserRegistrationRequest
import com.example.meditox.models.userRegistration.UserRegistrationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/sendOtp")
    suspend fun sendOtp(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("/api/auth/verifyOtp")
    suspend fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("/api/v1/register")
    suspend fun registerUser(@Body userRegistrationRequest: UserRegistrationRequest): Response<ApiResponse<UserRegistrationResponse>>
}