package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalGeneralFmcgEntity

data class GlobalGeneralFmcgDto(
    val globalFmcgId: Long,
    val productName: String,
    val brand: String,
    val manufacturer: String,
    val categoryLabel: String,
    val baseUnit: String,
    val unitsPerPack: Int,
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
    fun toEntity(): GlobalGeneralFmcgEntity {
        return GlobalGeneralFmcgEntity(
            globalFmcgId = globalFmcgId,
            productName = productName,
            brand = brand,
            manufacturer = manufacturer,
            categoryLabel = categoryLabel,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
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
