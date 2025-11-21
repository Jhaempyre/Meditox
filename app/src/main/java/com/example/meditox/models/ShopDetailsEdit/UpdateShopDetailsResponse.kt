package com.example.meditox.models.ShopDetailsEdit

import com.example.meditox.models.ShopDetails

data class UpdateShopDetailsResponse(
    val data: ShopDetails?,
    val message: String,
    val success: Boolean
)