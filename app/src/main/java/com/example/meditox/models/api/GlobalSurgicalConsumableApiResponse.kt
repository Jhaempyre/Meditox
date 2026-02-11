package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName

data class GlobalSurgicalConsumableApiResponse(
    @SerializedName("globalSurgicalId")
    val globalSurgicalId: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("material")
    val material: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

    @SerializedName("sterile")
    val sterile: Boolean,

    @SerializedName("disposable")
    val disposable: Boolean,

    @SerializedName("intendedUse")
    val intendedUse: String,

    @SerializedName("sizeSpecification")
    val sizeSpecification: String,

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
