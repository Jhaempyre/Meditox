package com.example.meditox.models.ShopDetailsEdit

import java.time.LocalDateTime
import java.util.UUID

data class UpdateShopDetailsRequest(
    val user_id: UUID,
    val shopName: String? = null,
    val licenseNumber: String? = null, // Drug license number
    val gstNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pinCode: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val shopStatus: Boolean,

)