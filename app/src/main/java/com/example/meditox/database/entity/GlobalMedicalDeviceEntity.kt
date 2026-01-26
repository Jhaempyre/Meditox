package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_medical_device")
data class GlobalMedicalDeviceEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_device_id")
    val globalDeviceId: Long,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "brand")
    val brand: String,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String,

    @ColumnInfo(name = "model_number")
    val modelNumber: String,

    @ColumnInfo(name = "device_type")
    val deviceType: String,

    @ColumnInfo(name = "base_unit")
    val baseUnit: String,

    @ColumnInfo(name = "units_per_pack")
    val unitsPerPack: Int,

    @ColumnInfo(name = "reusable")
    val reusable: Boolean,

    @ColumnInfo(name = "medical_device_class")
    val medicalDeviceClass: String,

    @ColumnInfo(name = "certification")
    val certification: String,

    @ColumnInfo(name = "warranty_months")
    val warrantyMonths: Int,

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
