package com.example.meditox.models.chemist

data class StockSummaryDto(
    val current_stock: Int,
    val minimum_stock_level: Int,
    val maximum_stock_level: Int,
    val reorder_quantity: Int,
    val stock_status: StockStatus,
    val stock_value: Double? = null,
    val reserved_quantity: Int,
    val available_for_sale: Int
)
