package com.example.meditox.services

import com.example.meditox.models.AuthRequest
import com.example.meditox.models.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {
    @POST("/auth/send_otp")
    suspend fun SendOtp(@Body authRequest: AuthRequest): Response<AuthResponse>
}