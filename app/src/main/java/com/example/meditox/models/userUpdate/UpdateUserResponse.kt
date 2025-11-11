package com.example.meditox.models.userUpdate

import com.example.meditox.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserResponse(
    val message: String?,
    val user: User?,
    val updatedAt: String?
)