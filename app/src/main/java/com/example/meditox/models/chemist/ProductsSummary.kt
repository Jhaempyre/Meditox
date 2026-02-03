package com.example.meditox.models.chemist

data class ProductsSummary(
    val total_products: Int,
    val out_of_stock: Int,
    val low_stock: Int,
    val adequate_stock: Int,
    val overstocked: Int,
    val total_stock_value: Double? = null
)
