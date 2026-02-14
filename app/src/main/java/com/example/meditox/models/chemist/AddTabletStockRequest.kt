package com.example.meditox.models.chemist

import java.math.BigDecimal

data class AddTabletStockRequest(
    val chemistProductId: Long,
    val wholesalerId: Long?,
    val batchNumber: String?,
    val numberOfStrips: Int,
    val purchasePricePerStrip: BigDecimal?,
    val sellingPricePerStrip: BigDecimal?,
    val mrpPerStrip: BigDecimal?,
    val expiryDate: String, // YYYY-MM-DD
    val mfgDate: String     // YYYY-MM-DD
)
