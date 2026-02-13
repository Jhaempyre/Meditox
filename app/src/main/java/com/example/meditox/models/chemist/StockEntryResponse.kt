package com.example.meditox.models.chemist

import com.example.meditox.database.entity.ChemistBatchStockEntity

data class StockEntryResponse(
    val batchStockId: Long,
    val chemistProductId: Long,
    val productName: String,
    val category: ProductCategory,
    val wholesalerId: Long? = null,
    val wholesalerName: String? = null,
    val batchNumber: String? = null,
    val serialNumber: String? = null,
    val quantityInStock: Int,
    val quantityReserved: Int,
    val quantityDamaged: Int,
    val purchaseRate: Double? = null,
    val sellingRate: Double? = null,
    val mrp: Double? = null,
    val manufacturingDate: String? = null,
    val expiryDate: String? = null,
    val receivedDate: String? = null,
    val storageLocation: String? = null,
    val isExpired: Boolean? = null,
    val isNearExpiry: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toEntity(chemistId: Long): ChemistBatchStockEntity {
        return ChemistBatchStockEntity(
            batchStockId = batchStockId,
            chemistId = chemistId,
            chemistProductId = chemistProductId,
            productName = productName,
            productCategory = category.name,
            wholesalerId = wholesalerId,
            wholesalerName = wholesalerName,
            batchNumber = batchNumber,
            serialNumber = serialNumber,
            quantityInStock = quantityInStock,
            quantityReserved = quantityReserved,
            quantityDamaged = quantityDamaged,
            purchaseRate = purchaseRate,
            sellingRate = sellingRate,
            mrp = mrp,
            manufacturingDate = manufacturingDate,
            expiryDate = expiryDate,
            receivedDate = receivedDate,
            storageLocation = storageLocation,
            isExpired = isExpired,
            isNearExpiry = isNearExpiry,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
