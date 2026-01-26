package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalSupplementEntity

data class GlobalSupplementDto(
    val globalSupplementId: Long,
    val productName: String,
    val brand: String,
    val manufacturer: String,
    val supplementType: String,
    val compositionSummary: String,
    val ageGroup: String,
    val baseUnit: String,
    val unitsPerPack: Int,
    val isLooseSaleAllowed: Boolean,
    val fssaiLicense: String,
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
    fun toEntity(): GlobalSupplementEntity {
        return GlobalSupplementEntity(
            globalSupplementId = globalSupplementId,
            productName = productName,
            brand = brand,
            manufacturer = manufacturer,
            supplementType = supplementType,
            compositionSummary = compositionSummary,
            ageGroup = ageGroup,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
            isLooseSaleAllowed = isLooseSaleAllowed,
            fssaiLicense = fssaiLicense,
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
