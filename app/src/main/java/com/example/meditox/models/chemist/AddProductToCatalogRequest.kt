package com.example.meditox.models.chemist

import com.google.gson.annotations.SerializedName

data class AddProductToCatalogRequest(
    @SerializedName("product_category")
    val productCategory: String,
    
    @SerializedName("global_product_id")
    val globalProductId: Long,
    
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Int,
    
    @SerializedName("maximum_stock_level")
    val maximumStockLevel: Int,
    
    @SerializedName("reorder_quantity")
    val reorderQuantity: Int
)
