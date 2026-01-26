package com.example.meditox.models.sync

import com.example.meditox.database.entity.GlobalMedicalDeviceEntity

data class GlobalMedicalDeviceDto(
    val globalDeviceId: Long,
    val productName: String,
    val brand: String,
    val manufacturer: String,
    val modelNumber: String,
    val deviceType: String,
    val baseUnit: String,
    val unitsPerPack: Int,
    val reusable: Boolean,
    val medicalDeviceClass: String,
    val certification: String,
    val warrantyMonths: Int,
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
    fun toEntity(): GlobalMedicalDeviceEntity {
        return GlobalMedicalDeviceEntity(
            globalDeviceId = globalDeviceId,
            productName = productName,
            brand = brand,
            manufacturer = manufacturer,
            modelNumber = modelNumber,
            deviceType = deviceType,
            baseUnit = baseUnit,
            unitsPerPack = unitsPerPack,
            reusable = reusable,
            medicalDeviceClass = medicalDeviceClass,
            certification = certification,
            warrantyMonths = warrantyMonths,
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
