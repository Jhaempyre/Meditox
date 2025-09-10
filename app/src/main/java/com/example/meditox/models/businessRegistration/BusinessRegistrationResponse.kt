package com.example.meditox.models.businessRegistration

import com.example.meditox.models.ShopDetails

data class BusinessRegistrationResponse (
    val success: Boolean,
    val message: String,
    val data: ShopDetails? = null //will change to shop details .
)