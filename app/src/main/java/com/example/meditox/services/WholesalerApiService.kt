package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.wholesaler.CreateWholesalerRequest
import com.example.meditox.models.wholesaler.GetWholesalersResponse
import com.example.meditox.models.wholesaler.WholesalerResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WholesalerApiService {

    @GET("api/v3/wholesalers/chemist/{chemistId}")
    suspend fun getWholesalersByChemist(
        @Path("chemistId") chemistId: Long,
        @Query("state") state: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("pincode") pincode: String? = null,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<GetWholesalersResponse>>

    @POST("api/v3/wholesalers/chemist/{chemistId}/add")
    suspend fun createWholesaler(
        @Path("chemistId") chemistId: Long,
        @Body request: CreateWholesalerRequest
    ): Response<ApiResponse<WholesalerResponseDto>>

    @PUT("api/v3/wholesalers/{id}")
    suspend fun updateWholesaler(
        @Path("id") id: Long,
        @Body request: CreateWholesalerRequest
    ): Response<ApiResponse<WholesalerResponseDto>>

    @DELETE("api/v3/wholesalers/{id}")
    suspend fun deleteWholesaler(
        @Path("id") id: Long
    ): Response<ApiResponse<WholesalerResponseDto>>
}
