package com.example.meditox.models.chemist

import java.math.BigDecimal

data class AddStockResponse(
    val batchStockId: Long,
    val chemistProductId: Long,
    val wholesalerId: Long?,
    val wholesalerName: String?,
    val productName: String,
    val batchNumber: String?,
    val quantityAdded: Int,
    val totalStock: Int,
    val purchasePrice: BigDecimal?,
    val expiryDate: String,
    val addedAt: String
)

