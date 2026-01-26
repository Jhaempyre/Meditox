package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalSurgicalConsumableEntity

data class GlobalSurgicalConsumableDto(
    val globalSurgicalId: Long,
    val productName: String,
    val brand: String,
    val manufacturer: String,
    val material: String,
    val baseUnit: String,
    val unitsPerPack: Int,
    val sterile: Boolean,
    val disposable: Boolean,
    val intendedUse: String,
    val sizeSpecification: String,
    val hsnCode: String,
    val gstRate: Double,
    val verified: Boolean,
    val createdByChemistId: Long,
    val createdAt: String,
    val isActive: Boolean,
    val imageUrl: String? = null,
    val description: String? = null,
    val advice: String? = null
) {
    fun toEntity(): GlobalSurgicalConsumableEntity {
        return GlobalSurgicalConsumableEntity(
            globalSurgicalId = globalSurgicalId,
            productName = productName,
            brand = brand,
            manufacturer = manufacturer,
            material = material,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
            sterile = sterile,
            disposable = disposable,
            intendedUse = intendedUse,
            sizeSpecification = sizeSpecification,
            hsnCode = hsnCode,
            gstRate = gstRate.toString(),
            verified = verified,
            createdByChemistId = createdByChemistId,
            createdAt = createdAt,
            updatedAt = null,
            isActive = isActive,
            imageUrl = imageUrl,
            description = description,
            advice = advice
        )
    }
}
