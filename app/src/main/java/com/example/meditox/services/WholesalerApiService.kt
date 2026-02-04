package com.example.meditox.services

import com.example.meditox.models.ApiResponse
import com.example.meditox.models.wholesaler.GetWholesalersResponse
import retrofit2.Response
import retrofit2.http.GET
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
}
