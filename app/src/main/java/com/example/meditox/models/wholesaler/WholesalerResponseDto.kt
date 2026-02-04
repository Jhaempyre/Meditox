package com.example.meditox.models.wholesaler

import com.example.meditox.database.entity.WholesalerEntity

data class WholesalerResponseDto(
    val wholesaler_id: Long,
    val wholesaler_name: String,
    val contact_person: String? = null,
    val phone_number: String? = null,
    val email: String? = null,
    val gstin: String? = null,
    val drug_license_number: String? = null,
    val state_code: String? = null,
    val address_line1: String? = null,
    val address_line2: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val credit_days: Int? = null,
    val credit_limit: Double? = null,
    val is_active: Boolean? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val added_by_chemist_id: Long? = null
) {
    fun toEntity(): WholesalerEntity {
        return WholesalerEntity(
            wholesalerId = wholesaler_id,
            wholesalerName = wholesaler_name,
            contactPerson = contact_person,
            phoneNumber = phone_number,
            email = email,
            gstin = gstin,
            drugLicenseNumber = drug_license_number,
            stateCode = state_code,
            addressLine1 = address_line1,
            addressLine2 = address_line2,
            city = city,
            state = state,
            pincode = pincode,
            creditDays = credit_days,
            creditLimit = credit_limit,
            isActive = is_active,
            createdAt = created_at,
            updatedAt = updated_at,
            addedByChemistId = added_by_chemist_id
        )
    }
}
