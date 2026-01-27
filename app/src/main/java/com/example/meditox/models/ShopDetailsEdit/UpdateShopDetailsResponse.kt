package com.example.meditox.models.ShopDetailsEdit

import com.example.meditox.models.ShopDetails
import com.example.meditox.utils.UUIDSerializer
import com.example.meditox.utils.LocalDateTimeKSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

// This matches what the backend actually sends - the shop data directly, not wrapped in another data field
@Serializable
data class UpdateShopDetailsResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
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
    val shopStatus: Boolean,
    
    // New fields
    val chemistId: Long? = null,
    val ownerName: String? = null,
    val stateCode: String? = null,
    val addressLine2: String? = null,
    val bankAccountNumber: String? = null,
    val bankIfscCode: String? = null,
    val bankName: String? = null,
    val upiId: String? = null,
    val isActive: Boolean? = true,
    
    @Serializable(with = LocalDateTimeKSerializer::class)
    val createdAt: LocalDateTime? = null,
    
    @Serializable(with = LocalDateTimeKSerializer::class)
    val meditoxUpdatedAt: LocalDateTime? = null
)