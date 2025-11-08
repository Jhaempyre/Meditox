package com.example.meditox.models.userUpdate

import com.example.meditox.models.EmergencyContact
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val name: String,
    val abhaId: String,
    val gender: String,
    val bloodGroup: String,
    val allergies: String,
    val chronicConditions: String,
    val emergencyContact: EmergencyContact
)