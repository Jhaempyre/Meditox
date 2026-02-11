package com.example.meditox.models.GlobalDrug

import com.google.gson.annotations.SerializedName

data class CreateGlobalDrugRequest(
    @SerializedName("brand_name")
    val brandName: String,

    @SerializedName("generic_name")
    val genericName: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("system_of_medicine")
    val systemOfMedicine: String,

    @SerializedName("dosage_form")
    val dosageForm: String,

    @SerializedName("strength_value")
    val strengthValue: String,

    @SerializedName("strength_unit")
    val strengthUnit: String,

    @SerializedName("route_of_administration")
    val routeOfAdministration: String,

    @SerializedName("base_unit")
    val baseUnit: String,

    @SerializedName("units_per_pack")
    val unitsPerPack: Int,

    @SerializedName("is_loose_sale_allowed")
    val looseSaleAllowed: Boolean,

    @SerializedName("drug_schedule")
    val drugSchedule: String,

    @SerializedName("prescription_required")
    val prescriptionRequired: Boolean,

    @SerializedName("narcotic_drug")
    val narcoticDrug: Boolean,

    @SerializedName("regulatory_authority")
    val regulatoryAuthority: String,

    @SerializedName("hsn_code")
    val hsnCode: String,

    @SerializedName("gst")
    val gst: Double,

    @SerializedName("created_by_chemist_id")
    val createdByChemistId: Long
)
