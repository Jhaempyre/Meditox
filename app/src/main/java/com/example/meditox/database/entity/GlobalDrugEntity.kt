package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "global_drugs")
data class GlobalDrugEntity(
    @PrimaryKey
    @ColumnInfo(name = "global_drug_id")
    val globalDrugId: Long,

    @ColumnInfo(name = "brand_name")
    val brandName: String,

    @ColumnInfo(name = "generic_name")
    val genericName: String,

    @ColumnInfo(name = "manufacturer")
    val manufacturer: String,

    @ColumnInfo(name = "system_of_medicine")
    val systemOfMedicine: String, // ALLOPATHY, AYURVEDA, HOMEOPATHY, UNANI

    @ColumnInfo(name = "dosage_form")
    val dosageForm: String, // TABLET, CAPSULE, SYRUP, INJECTION, OINTMENT, DROPS

    @ColumnInfo(name = "strength_value")
    val strengthValue: String, // Store as String to handle BigDecimal

    @ColumnInfo(name = "strength_unit")
    val strengthUnit: String,

    @ColumnInfo(name = "route_of_administration")
    val routeOfAdministration: String, // ORAL, TOPICAL, INTRAVENOUS, etc.

    @ColumnInfo(name = "base_unit")
    val baseUnit: String,

    @ColumnInfo(name = "units_per_pack")
    val unitsPerPack: Int,

    @ColumnInfo(name = "is_loose_sale_allowed")
    val isLooseSaleAllowed: Boolean,

    @ColumnInfo(name = "drug_schedule")
    val drugSchedule: String, // NONE, H, H1, X, C, C1

    @ColumnInfo(name = "prescription_required")
    val prescriptionRequired: Boolean,

    @ColumnInfo(name = "narcotic_drug")
    val narcoticDrug: Boolean,

    @ColumnInfo(name = "regulatory_authority")
    val regulatoryAuthority: String,

    @ColumnInfo(name = "hsn_code")
    val hsnCode: String,

    @ColumnInfo(name = "verified")
    val verified: Boolean,

    @ColumnInfo(name = "created_by_chemist_id")
    val createdByChemistId: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: String, // Store as ISO String

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "advice")
    val advice: String? = null
)
