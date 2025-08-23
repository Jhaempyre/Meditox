package com.example.meditox.models.userRegistration

import com.example.meditox.models.EmergencyContact
import java.time.LocalDateTime
import java.util.UUID

data class UserRegistrationResponse(
    val id : UUID,
    val abhaId : String,
    val phone :String ,
    val name : String,
    val dob :LocalDateTime,
    val gender : String,
    val bloodGroup : String,
    val allergies : String,
    val chronicConditions : String,
    val emergencyContact : EmergencyContact,
    val role : String,
)
