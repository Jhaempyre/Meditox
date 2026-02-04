package com.example.meditox.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wholesaler")
data class WholesalerEntity(
    @PrimaryKey
    @ColumnInfo(name = "wholesaler_id")
    val wholesalerId: Long,

    @ColumnInfo(name = "wholesaler_name")
    val wholesalerName: String,

    @ColumnInfo(name = "contact_person")
    val contactPerson: String? = null,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "gstin")
    val gstin: String? = null,

    @ColumnInfo(name = "drug_license_number")
    val drugLicenseNumber: String? = null,

    @ColumnInfo(name = "state_code")
    val stateCode: String? = null,

    @ColumnInfo(name = "address_line1")
    val addressLine1: String? = null,

    @ColumnInfo(name = "address_line2")
    val addressLine2: String? = null,

    @ColumnInfo(name = "city")
    val city: String? = null,

    @ColumnInfo(name = "state")
    val state: String? = null,

    @ColumnInfo(name = "pincode")
    val pincode: String? = null,

    @ColumnInfo(name = "credit_days")
    val creditDays: Int? = null,

    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: String? = null, // Stored as ISO String

    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null, // Stored as ISO String

    @ColumnInfo(name = "added_by_chemist_id")
    val addedByChemistId: Long? = null
)
