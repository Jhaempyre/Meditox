package com.example.meditox.models.userRegistration

import com.example.meditox.models.EmergencyContact
import java.time.LocalDateTime

data class UserRegistrationRequest(
    val phone: String,
    val name: String,
    val dob: LocalDateTime,
    val gender: String,
    val bloodGroup: String,
    val allergies: String,
    val chronicConditions: String,
    val emergencyContact: EmergencyContact,
    val role: String
)
