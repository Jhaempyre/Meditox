package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chemist_batch_stock")
data class ChemistBatchStockEntity(
    @PrimaryKey
    @ColumnInfo(name = "batch_stock_id")
    val batchStockId: Long,

    @ColumnInfo(name = "chemist_id")
    val chemistId: Long,

    @ColumnInfo(name = "chemist_product_id")
    val chemistProductId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "product_category")
    val productCategory: String,

    @ColumnInfo(name = "wholesaler_id")
    val wholesalerId: Long? = null,

    @ColumnInfo(name = "wholesaler_name")
    val wholesalerName: String? = null,

    @ColumnInfo(name = "batch_number")
    val batchNumber: String? = null,

    @ColumnInfo(name = "serial_number")
    val serialNumber: String? = null,

    @ColumnInfo(name = "quantity_in_stock")
    val quantityInStock: Int,

    @ColumnInfo(name = "quantity_reserved")
    val quantityReserved: Int,

    @ColumnInfo(name = "quantity_damaged")
    val quantityDamaged: Int,

    @ColumnInfo(name = "purchase_rate")
    val purchaseRate: Double? = null,

    @ColumnInfo(name = "selling_rate")
    val sellingRate: Double? = null,

    @ColumnInfo(name = "mrp")
    val mrp: Double? = null,

    @ColumnInfo(name = "manufacturing_date")
    val manufacturingDate: String? = null,

    @ColumnInfo(name = "expiry_date")
    val expiryDate: String? = null,

    @ColumnInfo(name = "received_date")
    val receivedDate: String? = null,

    @ColumnInfo(name = "storage_location")
    val storageLocation: String? = null,

    @ColumnInfo(name = "is_expired")
    val isExpired: Boolean? = null,

    @ColumnInfo(name = "is_near_expiry")
    val isNearExpiry: Boolean? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)
