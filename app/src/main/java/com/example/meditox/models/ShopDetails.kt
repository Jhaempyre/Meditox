package com.example.meditox.models

import com.example.meditox.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ShopDetails(
    @Serializable(with = UUIDSerializer::class)
    val id : UUID,
    val shopName: String,
    val licenseNumber: String,
    val gstNumber: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val contactPhone: String,
    val contactEmail: String,
    val latitude: String,
    val longitude: String,
    val shopStatus : Boolean,
    val createdAt : List<Int?>, //TODO: //this we will change for editscreen afterward
)
