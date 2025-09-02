package com.example.meditox.models.otpVerication

import com.example.meditox.models.User

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null
)
