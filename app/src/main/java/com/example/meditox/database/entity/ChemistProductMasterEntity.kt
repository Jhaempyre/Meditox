package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "chemist_product_master")
data class ChemistProductMasterEntity(

    @PrimaryKey
    @ColumnInfo(name = "chemist_product_id")
    val chemistProductId: Long,

    @ColumnInfo(name = "chemist_id")
    val chemistId: Long,

    @ColumnInfo(name = "product_category")
    val productCategory: String, // EnumType.STRING

    @ColumnInfo(name = "global_product_id")
    val globalProductId: Long,

    @ColumnInfo(name = "minimum_stock_level")
    val minimumStockLevel: Int,

    @ColumnInfo(name = "maximum_stock_level")
    val maximumStockLevel: Int,

    @ColumnInfo(name = "reorder_quantity")
    val reorderQuantity: Int,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? // Stored as ISO String
)
