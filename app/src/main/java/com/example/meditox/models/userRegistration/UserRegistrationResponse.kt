package com.example.meditox.models.userRegistration

import com.example.meditox.models.EmergencyContact
import com.example.meditox.models.User
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID
@Serializable
data class UserRegistrationResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)
