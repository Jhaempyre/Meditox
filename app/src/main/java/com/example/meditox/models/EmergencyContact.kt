package com.example.meditox.models

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val phone:String,
    val name:String,
    val relationship:String
)
