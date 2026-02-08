package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName

data class GlobalGeneralFmcgApiResponse(
    @SerializedName("globalFmcgId")
    val globalFmcgId: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("categoryLabel")
    val categoryLabel: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

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
