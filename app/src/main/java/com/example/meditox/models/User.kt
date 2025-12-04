package com.example.meditox.models

import com.example.meditox.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID
@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val abhaId : String,
    val phone : String,
    val name : String,
    val dob :List<Int?>,
    val gender : String,
    val bloodGroup : String,
    val allergies : String,
    val chronicConditions : String,
    val emergencyContact : EmergencyContact,
    val role : String,
    val businessRegistered : Boolean,
)
