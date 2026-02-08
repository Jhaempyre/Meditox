package com.example.meditox.models.chemist

import com.google.gson.annotations.SerializedName

data class ProductDetailsDto(
    @SerializedName("brand_name")
    val brandName: String? = null,
    
    @SerializedName("generic_name")
    val genericName: String? = null,
    
    @SerializedName("manufacturer")
    val manufacturer: String? = null,
    
    @SerializedName("dosage_form")
    val dosageForm: String? = null,
    
    @SerializedName("strength")
    val strength: String? = null,
    
    @SerializedName("current_mrp")
    val currentMrp: Double? = null,
    
    @SerializedName("gst_rate")
    val gstRate: Double? = null,
    
    @SerializedName("hsn_code")
    val hsnCode: String? = null,
    
    @SerializedName("product_name")
    val productName: String? = null,
    
    @SerializedName("form")
    val form: String? = null,
    
    @SerializedName("variant")
    val variant: String? = null,
    
    @SerializedName("device_type")
    val deviceType: String? = null,
    
    @SerializedName("supplement_type")
    val supplementType: String? = null,
    
    @SerializedName("material")
    val material: String? = null,
    
    @SerializedName("category_label")
    val categoryLabel: String? = null
)
