package com.example.meditox.models.GlobalChemist

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class GlobalCosmeticApiResponse(
    @SerializedName("globalCosmeticId")
    val globalCosmeticId: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("form")
    val form: String,

    @SerializedName("variant")
    val variant: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

    @SerializedName("intendedUse")
    val intendedUse: String,

    @SerializedName("skinType")
    val skinType: String,

    @SerializedName("cosmeticLicenseNumber")
    val cosmeticLicenseNumber: String,

    @SerializedName("hsnCode")
    val hsnCode: String,

    @SerializedName("gstRate")
    val gstRate: BigDecimal,

    @SerializedName("verified")
    val verified: Boolean,

    @SerializedName("createdByChemistId")
    val createdByChemistId: Long,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("active")
    val isActive: Boolean
)
