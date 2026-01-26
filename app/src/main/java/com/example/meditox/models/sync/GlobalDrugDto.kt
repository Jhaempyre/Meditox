package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalDrugEntity

data class GlobalDrugDto(
    val globalDrugId: Long,
    val brandName: String,
    val genericName: String,
    val manufacturer: String,
    val systemOfMedicine: String,
    val dosageForm: String,
    val strengthValue: Double,
    val strengthUnit: String,
    val routeOfAdministration: String,
    val baseUnit: String,
    val unitsPerPack: Int,
    val looseSaleAllowed: Boolean,
    val drugSchedule: String,
    val prescriptionRequired: Boolean,
    val narcoticDrug: Boolean,
    val regulatoryAuthority: String,
    val hsnCode: String,
    val verified: Boolean,
    val createdByChemistId: Long,
    val createdAt: String,
    val active: Boolean,
    val imageUrl: String? = null,
    val description: String? = null,
    val advice: String? = null
) {
    // Convert DTO to Entity for Room database
    fun toEntity(): GlobalDrugEntity {
        return GlobalDrugEntity(
            globalDrugId = globalDrugId,
            brandName = brandName,
            genericName = genericName,
            manufacturer = manufacturer,
            systemOfMedicine = systemOfMedicine,
            dosageForm = dosageForm,
            strengthValue = strengthValue.toString(),
            strengthUnit = strengthUnit,
            routeOfAdministration = routeOfAdministration,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
            isLooseSaleAllowed = looseSaleAllowed,
            drugSchedule = drugSchedule,
            prescriptionRequired = prescriptionRequired,
            narcoticDrug = narcoticDrug,
            regulatoryAuthority = regulatoryAuthority,
            hsnCode = hsnCode,
            verified = verified,
            createdByChemistId = createdByChemistId,
            createdAt = createdAt,
            isActive = active,
            imageUrl = imageUrl,
            description = description,
            advice = advice
        )
    }
}
