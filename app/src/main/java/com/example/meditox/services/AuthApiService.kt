package com.example.meditox.services

import com.example.meditox.models.AuthRequest
import com.example.meditox.models.AuthResponse
import com.example.meditox.models.VerifyOtpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/sendOtp")
    suspend fun sendOtp(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("/api/auth/verifyOtp")
    suspend fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest): Response<AuthResponse>
}