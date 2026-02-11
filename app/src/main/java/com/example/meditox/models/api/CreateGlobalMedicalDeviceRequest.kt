package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateGlobalMedicalDeviceRequest(
    @SerializedName("product_name")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("model_number")
    val modelNumber: String,

    @SerializedName("device_type")
    val deviceType: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("reusable")
    val reusable: Boolean,

    @SerializedName("medical_device_class")
    val medicalDeviceClass: String,

    @SerializedName("certification")
    val certification: String?,

    @SerializedName("warranty_months")
    val warrantyMonths: Int?,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst_rate")
    val gstRate: BigDecimal,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
