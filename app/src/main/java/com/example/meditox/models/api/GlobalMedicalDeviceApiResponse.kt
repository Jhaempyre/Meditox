package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName

data class GlobalMedicalDeviceApiResponse(
    @SerializedName("globalDeviceId")
    val globalDeviceId: Long,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("modelNumber")
    val modelNumber: String,

    @SerializedName("deviceType")
    val deviceType: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

    @SerializedName("reusable")
    val reusable: Boolean,

    @SerializedName("medicalDeviceClass")
    val medicalDeviceClass: String,

    @SerializedName("certification")
    val certification: String?,

    @SerializedName("warrantyMonths")
    val warrantyMonths: Int?,

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
