package com.example.meditox.models.auth

import com.example.meditox.models.User

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null
)