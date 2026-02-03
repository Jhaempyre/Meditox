package com.example.meditox.models.chemist

data class ProductDetailsDto(
    val brand_name: String? = null,
    val generic_name: String? = null,
    val manufacturer: String? = null,
    val dosage_form: String? = null,
    val strength: String? = null,
    val current_mrp: Double? = null,
    val gst_rate: Double? = null,
    val hsn_code: String? = null,
    val product_name: String? = null,
    val form: String? = null,
    val variant: String? = null,
    val device_type: String? = null,
    val supplement_type: String? = null,
    val material: String? = null,
    val category_label: String? = null
)
