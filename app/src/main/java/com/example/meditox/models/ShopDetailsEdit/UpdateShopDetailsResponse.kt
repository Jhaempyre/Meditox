package com.example.meditox.models.ShopDetailsEdit

import com.example.meditox.models.ShopDetails
import com.example.meditox.utils.UUIDSerializer
import kotlinx.serialization.Serializable
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
    val shopStatus: Boolean
)