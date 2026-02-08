package com.example.meditox.models.GlobalChemist

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateGlobalCosmeticRequest(
    @SerializedName("product_name")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("form")
    val form: String,

    @SerializedName("variant")
    val variant: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("intended_use")
    val intendedUse: String,

    @SerializedName("skin_type")
    val skinType: String,

    @SerializedName("cosmetic_license_number")
    val cosmeticLicenseNumber: String,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst_rate")
    val gstRate: BigDecimal,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
