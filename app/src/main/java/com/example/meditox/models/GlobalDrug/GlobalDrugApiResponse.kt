package com.example.meditox.models.GlobalDrug

import com.google.gson.annotations.SerializedName

data class GlobalDrugApiResponse(
    @SerializedName("globalDrugId")
    val globalDrugId: Long,

    @SerializedName("brandName")
    val brandName: String,

    @SerializedName("genericName")
    val genericName: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("systemOfMedicine")
    val systemOfMedicine: String,

    @SerializedName("dosageForm")
    val dosageForm: String,

    @SerializedName("strengthValue")
    val strengthValue: String,

    @SerializedName("strengthUnit")
    val strengthUnit: String,

    @SerializedName("routeOfAdministration")
    val routeOfAdministration: String,

    @SerializedName("baseUnit")
    val baseUnit: String,

    @SerializedName("unitsPerPack")
    val unitsPerPack: Int,

    @SerializedName("looseSaleAllowed")
    val looseSaleAllowed: Boolean,

    @SerializedName("drugSchedule")
    val drugSchedule: String,

    @SerializedName("prescriptionRequired")
    val prescriptionRequired: Boolean,

    @SerializedName("narcoticDrug")
    val narcoticDrug: Boolean,

    @SerializedName("regulatoryAuthority")
    val regulatoryAuthority: String,

    @SerializedName("hsnCode")
    val hsnCode: String,

    @SerializedName("verified")
    val verified: Boolean,

    @SerializedName("createdByChemistId")
    val createdByChemistId: Long,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("isActive")
    val isActive: Boolean
)
