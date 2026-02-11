package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName

data class GlobalSupplementApiResponse(
    @SerializedName("globalSupplementId")
    val globalSupplementId: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("supplementType")
    val supplementType: String,

    @SerializedName("compositionSummary")
    val compositionSummary: String,

    @SerializedName("ageGroup")
    val ageGroup: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

    @SerializedName("looseSaleAllowed")
    val isLooseSaleAllowed: Boolean,

    @SerializedName("fssaiLicense")
    val fssaiLicense: String?,

    @SerializedName("hsnCode")
    val hsnCode: String,

    @SerializedName("gstRate")
    val gstRate: Double,

    @SerializedName("verified")
    val verified: Boolean,

    @SerializedName("createdByChemistId")
    val createdByChemistId: Long,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("active")
    val isActive: Boolean
)
