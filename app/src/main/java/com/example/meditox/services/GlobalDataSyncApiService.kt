package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.sync.GetGlobalDrugsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GlobalDataSyncApiService {
    
    @GET("api/v3/global/drugs")
    suspend fun getGlobalDrugs(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("drug_schedule") drugSchedule: String? = null,
        @Query("sort_by") sortBy: String = "brandName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<GetGlobalDrugsResponse>>

    @GET("api/v3/global/cosmetics")
    suspend fun getGlobalCosmetics(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50,
        @Query("verified") verified: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("manufacturer") manufacturer: String? = null,
        @Query("brand") brand: String? = null,
        @Query("sort_by") sortBy: String = "productName",
        @Query("sort_order") sortOrder: String = "asc"
    ): Response<ApiResponse<com.example.meditox.models.sync.GetGlobalCosmeticsResponse>>
}
