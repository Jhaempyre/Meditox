package com.example.meditox.models

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)