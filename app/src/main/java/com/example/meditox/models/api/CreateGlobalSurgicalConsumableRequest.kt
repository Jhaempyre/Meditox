package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateGlobalSurgicalConsumableRequest(
    @SerializedName("product_name")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("material")
    val material: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("sterile")
    val sterile: Boolean,

    @SerializedName("disposable")
    val disposable: Boolean,

    @SerializedName("intended_use")
    val intendedUse: String,

    @SerializedName("size_specification")
    val sizeSpecification: String,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst_rate")
    val gstRate: BigDecimal,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
