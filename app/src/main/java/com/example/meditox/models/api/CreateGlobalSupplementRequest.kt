package com.example.meditox.models.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CreateGlobalSupplementRequest(
    @SerializedName("product_name")
    val productName: String,

    @SerializedName("brand")
    val brand: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("supplement_type")
    val supplementType: String,

    @SerializedName("composition_summary")
    val compositionSummary: String,

    @SerializedName("age_group")
    val ageGroup: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("is_loose_sale_allowed")
    val isLooseSaleAllowed: Boolean,

    @SerializedName("fssai_license")
    val fssaiLicense: String?,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst_rate")
    val gstRate: BigDecimal,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
