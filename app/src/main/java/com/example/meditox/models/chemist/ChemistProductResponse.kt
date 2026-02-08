package com.example.meditox.models.chemist

import com.google.gson.annotations.SerializedName

data class ChemistProductResponse(
    @SerializedName("chemist_product_id")
    val chemistProductId: Long,
    
    @SerializedName("chemist_id")
    val chemistId: Long,
    
    @SerializedName("product_category")
    val productCategory: String,
    
    @SerializedName("global_product_id")
    val globalProductId: Long,
    
    @SerializedName("product_details")
    val productDetails: ProductDetailsDto?,
    
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Int,
    
    @SerializedName("maximum_stock_level")
    val maximumStockLevel: Int,
    
    @SerializedName("reorder_quantity")
    val reorderQuantity: Int,
    
    @SerializedName("current_stock")
    val currentStock: Int?,
    
    @SerializedName("stock_status")
    val stockStatus: String?,
    
    @SerializedName("is_active")
    val isActive: Boolean,
    
    @SerializedName("created_at")
    val createdAt: String
)
