package com.example.meditox.models.chemist

import java.math.BigDecimal

data class AddStockRequest(
    val chemistProductId: Long,
    val wholesalerId: Long?,
    val batchNumber: String?,
    val quantity: Int,
    val purchasePrice: BigDecimal?,
    val expiryDate: String, // YYYY-MM-DD
    val mfgDate: String, // YYYY-MM-DD
    val mrp: BigDecimal?,
    val sellingPrice: BigDecimal?
)
