package com.example.meditox.models

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: User? = null
)