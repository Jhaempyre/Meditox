package com.example.meditox.models.chemist

import com.example.meditox.database.entity.ChemistProductMasterEntity

data class ChemistProductListItem(
    val chemist_product_id: Long,
    val product_category: ProductCategory,
    val global_product_id: Long,
    val product_details: ProductDetailsDto? = null,
    val stock_summary: StockSummaryDto,
    val is_active: Boolean,
    val updated_at: String? = null
) {
    fun toEntity(chemistId: Long): ChemistProductMasterEntity {
        return ChemistProductMasterEntity(
            chemistProductId = chemist_product_id,
            chemistId = chemistId,
            productCategory = product_category.name,
            globalProductId = global_product_id,
            minimumStockLevel = stock_summary.minimum_stock_level,
            maximumStockLevel = stock_summary.maximum_stock_level,
            reorderQuantity = stock_summary.reorder_quantity,
            isActive = is_active,
            updatedAt = updated_at
        )
    }
}
