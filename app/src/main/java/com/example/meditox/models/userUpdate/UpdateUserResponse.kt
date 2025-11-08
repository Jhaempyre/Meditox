package com.example.meditox.models.userUpdate

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserResponse(
    val message: String,
    val userId: String,
    val updatedAt: String
)