package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_surgical_consumable")
data class GlobalSurgicalConsumableEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_surgical_id")
    val globalSurgicalId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "brand")
    val brand: String,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String,

    @ColumnInfo(name = "material")
    val material: String,

    @ColumnInfo(name = "base_unit")
    val baseUnit: String,

    @ColumnInfo(name = "units_per_pack")
    val unitsPerPack: Int,

    @ColumnInfo(name = "sterile")
    val sterile: Boolean,

    @ColumnInfo(name = "disposable")
    val disposable: Boolean,

    @ColumnInfo(name = "intended_use")
    val intendedUse: String,

    @ColumnInfo(name = "size_specification")
    val sizeSpecification: String,

    @ColumnInfo(name = "hsn_code")
    val hsnCode: String,

    @ColumnInfo(name = "gst_rate")
    val gstRate: String, // Stored as String for BigDecimal

    @ColumnInfo(name = "verified")
    val verified: Boolean,

    @ColumnInfo(name = "created_by_chemist_id")
    val createdByChemistId: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: String, // Stored as ISO String

    @ColumnInfo(name = "updated_at")
    val updatedAt: String?, // Stored as ISO String

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "advice")
    val advice: String? = null
)
