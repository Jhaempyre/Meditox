package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalCosmeticEntity

data class GlobalCosmeticDto(
    val globalCosmeticId: Long,
    val productName: String,
    val brand: String,
    val manufacturer: String,
    val form: String,
    val variant: String,
    val baseUnit: String,
    val unitsPerPack: Int,
    val intendedUse: String,
    val skinType: String,
    val cosmeticLicenseNumber: String,
    val hsnCode: String,
    val gstRate: Double,
    val verified: Boolean,
    val createdByChemistId: Long,
    val createdAt: String,
    val updatedAt: String? = null,
    val isActive: Boolean,
    val imageUrl: String? = null,
    val description: String? = null,
    val advice: String? = null
) {
    fun toEntity(): GlobalCosmeticEntity {
        return GlobalCosmeticEntity(
            globalCosmeticId = globalCosmeticId,
            productName = productName,
            brand = brand,
            manufacturer = manufacturer,
            form = form,
            variant = variant,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
            intendedUse = intendedUse,
            skinType = skinType,
            cosmeticLicenseNumber = cosmeticLicenseNumber,
            hsnCode = hsnCode,
            gstRate = gstRate.toString(),
            verified = verified,
            createdByChemistId = createdByChemistId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
            imageUrl = imageUrl,
            description = description,
            advice = advice
        )
    }
}
