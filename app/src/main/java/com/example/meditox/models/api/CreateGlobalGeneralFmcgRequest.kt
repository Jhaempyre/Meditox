package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateGlobalGeneralFmcgRequest(
    @SerializedName("product_name")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("category_label")
    val categoryLabel: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst_rate")
    val gstRate: BigDecimal,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
