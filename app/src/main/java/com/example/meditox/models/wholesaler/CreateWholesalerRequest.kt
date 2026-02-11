package com.example.meditox.models.wholesaler

import com.google.gson.annotations.SerializedName

data class CreateWholesalerRequest(
    @SerializedName("wholesaler_name")
    val wholesalerName: String,

    @SerializedName("contact_person")
    val contactPerson: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("gstin")
    val gstin: String,

    @SerializedName("drug_license_number")
    val drugLicenseNumber: String,

    @SerializedName("state_code")
    val stateCode: String,

    @SerializedName("address_line1")
    val addressLine1: String,

    @SerializedName("address_line2")
    val addressLine2: String? = null,

    @SerializedName("city")
    val city: String,

    @SerializedName("state")
    val state: String,

    @SerializedName("pincode")
    val pincode: String,

    @SerializedName("credit_days")
    val creditDays: Int,

    @SerializedName("credit_limit")
    val creditLimit: Double
)
