package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_supplement")
data class GlobalSupplementEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_supplement_id")
    val globalSupplementId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "brand")
    val brand: String,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String,

    @ColumnInfo(name = "supplement_type")
    val supplementType: String,

    @ColumnInfo(name = "composition_summary")
    val compositionSummary: String,

    @ColumnInfo(name = "age_group")
    val ageGroup: String,

    @ColumnInfo(name = "base_unit")
    val baseUnit: String,

    @ColumnInfo(name = "units_per_pack")
    val unitsPerPack: Int,

    @ColumnInfo(name = "is_loose_sale_allowed")
    val isLooseSaleAllowed: Boolean,

    @ColumnInfo(name = "fssai_license")
    val fssaiLicense: String,

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
