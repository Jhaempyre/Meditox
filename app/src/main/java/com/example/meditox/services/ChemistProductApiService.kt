package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.chemist.AddProductToCatalogRequest
import com.example.meditox.models.chemist.ChemistProductResponse
import com.example.meditox.models.chemist.GetStockListResponse
import com.example.meditox.models.chemist.GetProductsResponse
import com.example.meditox.models.chemist.ProductCategory
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChemistProductApiService {

    @GET("api/v3/chemist/{chemistId}/products")
    suspend fun getChemistProducts(
        @Path("chemistId") chemistId: Long,
        @Query("category") category: ProductCategory? = null,
        @Query("stock_status") stockStatus: String? = null,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String = "stock_status"
    ): Response<ApiResponse<GetProductsResponse>>

    @POST("api/v3/chemist/{chemistId}/products")
    suspend fun addProductToCatalog(
        @Path("chemistId") chemistId: Long,
        @Body request: AddProductToCatalogRequest
    ): Response<ApiResponse<ChemistProductResponse>>

    @GET("api/v3/chemist/{chemistId}/stock")
    suspend fun getChemistStock(
        @Path("chemistId") chemistId: Long,
        @Query("category") category: ProductCategory? = null,
        @Query("stock_status") stockStatus: String? = null,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("sort_by") sortBy: String = "expiry_date",
        @Query("sort_dir") sortDir: String = "asc"
    ): Response<ApiResponse<GetStockListResponse>>
}
